package com.amhsolution.caisse.data.repository

import com.amhsolution.caisse.data.integrity.IntegrityHmac
import com.amhsolution.caisse.data.local.CaisseDatabase
import com.amhsolution.caisse.data.local.entity.HistoryEntryEntity
import com.amhsolution.caisse.data.local.entity.PerpetualTotalsEntity
import com.amhsolution.caisse.data.local.toDeliveryInfoOrNull
import com.amhsolution.caisse.data.local.toJson
import com.amhsolution.caisse.data.local.toJsonLines
import com.amhsolution.caisse.data.local.toJsonPayments
import com.amhsolution.caisse.data.local.toOrderLineSnapshots
import com.amhsolution.caisse.data.local.toPaymentRecords
import com.amhsolution.caisse.domain.history.businessDayFromEpochMillis
import com.amhsolution.caisse.domain.history.currentBusinessDay
import com.amhsolution.caisse.domain.history.toCancelledHistoryRecord
import com.amhsolution.caisse.domain.history.toCreditNote
import com.amhsolution.caisse.domain.integrity.INTEGRITY_GENESIS
import com.amhsolution.caisse.domain.integrity.TicketIntegrity
import com.amhsolution.caisse.domain.model.DeliveryInfo
import com.amhsolution.caisse.domain.model.HistoryRecord
import com.amhsolution.caisse.domain.model.OrderLineSnapshot
import com.amhsolution.caisse.domain.model.OrderTicket
import com.amhsolution.caisse.domain.model.OrderType
import com.amhsolution.caisse.domain.model.PaymentRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.util.UUID

class HistoryRepository(
    database: CaisseDatabase,
    private val isSimulationMode: () -> Boolean = { false },
    private val registerId: String = "caisse-1"
) {
    private val historyDao = database.historyDao()
    private val ticketCounterDao = database.ticketCounterDao()
    private val perpetualTotalsDao = database.perpetualTotalsDao()

    fun observeHistory(): Flow<List<HistoryRecord>> =
        historyDao.observeAll()
            .map { entries -> entries.map { it.toDomain() } }
            .flowOn(Dispatchers.Default)

    suspend fun getHistoryRecord(id: String): HistoryRecord? =
        historyDao.getById(id)?.toDomain()

    suspend fun allocateTicketNumber(): Int =
        ticketCounterDao.allocateTicketNumber(currentBusinessDay())

    suspend fun insertPaidOrder(
        ticketNumber: Int,
        orderType: OrderType,
        lines: List<OrderLineSnapshot>,
        totalCentimes: Int,
        payments: List<PaymentRecord>,
        deliveryInfo: DeliveryInfo?,
        paidAtEpochMillis: Long = System.currentTimeMillis()
    ): HistoryRecord {
        val businessDay = businessDayFromEpochMillis(paidAtEpochMillis)
        val id = UUID.randomUUID().toString()
        val draft = HistoryRecord(
            id = id,
            ticketNumber = ticketNumber,
            businessDay = businessDay,
            paidAtEpochMillis = paidAtEpochMillis,
            orderType = orderType,
            lines = lines,
            totalCentimes = totalCentimes,
            payments = payments,
            deliveryInfo = deliveryInfo,
            isSimulation = isSimulationMode(),
            registerId = registerId
        )
        return persistChained(draft)
    }

    /**
     * Annulation avant encaissement (commande en cours) — ticket d'audit distinct.
     * Ne réécrit jamais un ticket déjà validé. Idempotent sur l'id cancelled-*.
     */
    suspend fun insertCancelledOrder(
        order: OrderTicket,
        cancelledAtEpochMillis: Long = System.currentTimeMillis()
    ): HistoryRecord {
        val draft = order.toCancelledHistoryRecord(cancelledAtEpochMillis).copy(
            isSimulation = isSimulationMode(),
            registerId = registerId
        )
        historyDao.getById(draft.id)?.toDomain()?.let { return it }
        return persistChained(draft)
    }

    /**
     * Émet un avoir / contre-opération liée au ticket d'origine (nouvelle ligne).
     * Interdit si un avoir existe déjà ou si le ticket est une correction / simulé mal lié.
     */
    suspend fun insertCreditNote(originalTicketId: String): HistoryRecord {
        val original = historyDao.getById(originalTicketId)?.toDomain()
            ?: error("Ticket introuvable.")
        check(!original.isCorrection) { "Impossible d'émettre un avoir sur une contre-operation." }
        check(!original.isCancelled) { "Ticket legacy annule — pas d'avoir." }
        check(historyDao.countCorrectionsFor(originalTicketId) == 0) {
            "Un avoir a deja ete emis pour ce ticket."
        }
        val ticketNumber = allocateTicketNumber()
        val draft = original.toCreditNote(ticketNumber = ticketNumber).copy(
            isSimulation = original.isSimulation || isSimulationMode(),
            registerId = registerId
        )
        return persistChained(draft)
    }

    /**
     * Interdit — toute réécriture d'un ticket validé casse l'inaltérabilité ISCA.
     * Utiliser [insertCreditNote].
     */
    @Deprecated("ISCA: ne jamais réécrire un ticket validé. Utiliser insertCreditNote.")
    suspend fun updatePayments(id: String, payments: List<PaymentRecord>) {
        error(
            "Reecriture de ticket interdite (inalterabilite ISCA). " +
                "Emettre un avoir via insertCreditNote(id=$id). payments=${payments.size}"
        )
    }

    private suspend fun persistChained(draft: HistoryRecord): HistoryRecord {
        val previous = historyDao.getLatestChained()
        val previousHash = previous?.integrityHash?.takeIf { it.isNotBlank() } ?: INTEGRITY_GENESIS
        val chainHash = TicketIntegrity.hashForRecord(draft, previousHash)
        val sealedHash = IntegrityHmac.compute(chainHash)
        val sealed = draft.copy(
            previousIntegrityHash = previousHash,
            integrityHash = sealedHash
        )
        historyDao.insert(sealed.toEntity())
        if (!sealed.isSimulation) {
            applyPerpetualTotals(sealed)
        }
        return sealed
    }

    private suspend fun applyPerpetualTotals(record: HistoryRecord) {
        val current = perpetualTotalsDao.get() ?: PerpetualTotalsEntity()
        val next = current.copy(
            totalRevenueCentimes = current.totalRevenueCentimes + record.effectiveTotalCentimes,
            ticketCount = current.ticketCount + if (record.isCorrection) 0 else 1,
            correctionCount = current.correctionCount + if (record.isCorrection) 1 else 0,
            lastUpdatedEpochMillis = record.paidAtEpochMillis,
            integrityHash = TicketIntegrity.sha256Hex(
                "${current.integrityHash}|${record.integrityHash}|${record.effectiveTotalCentimes}"
            )
        )
        perpetualTotalsDao.upsert(next)
    }

    private fun HistoryRecord.toEntity(): HistoryEntryEntity =
        HistoryEntryEntity(
            id = id,
            ticketNumber = ticketNumber,
            businessDay = businessDay,
            paidAtEpochMillis = paidAtEpochMillis,
            orderType = orderType.name,
            linesJson = lines.toJsonLines(),
            totalCentimes = totalCentimes,
            paymentsJson = payments.toJsonPayments(),
            deliveryInfoJson = deliveryInfo?.toJson(),
            linkedTicketId = linkedTicketId,
            isCorrection = isCorrection,
            integrityHash = integrityHash,
            previousIntegrityHash = previousIntegrityHash,
            isSimulation = isSimulation,
            registerId = registerId
        )

    private fun HistoryEntryEntity.toDomain(): HistoryRecord =
        HistoryRecord(
            id = id,
            ticketNumber = ticketNumber,
            businessDay = businessDay,
            paidAtEpochMillis = paidAtEpochMillis,
            orderType = OrderType.valueOf(orderType),
            lines = linesJson.toOrderLineSnapshots(),
            totalCentimes = totalCentimes,
            payments = paymentsJson.toPaymentRecords(),
            deliveryInfo = deliveryInfoJson?.toDeliveryInfoOrNull(),
            linkedTicketId = linkedTicketId,
            isCorrection = isCorrection,
            integrityHash = integrityHash,
            previousIntegrityHash = previousIntegrityHash,
            isSimulation = isSimulation,
            registerId = registerId
        )
}
