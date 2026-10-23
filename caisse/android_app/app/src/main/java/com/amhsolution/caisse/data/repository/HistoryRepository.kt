package com.amhsolution.caisse.data.repository

import com.amhsolution.caisse.data.local.CaisseDatabase
import com.amhsolution.caisse.data.local.entity.HistoryEntryEntity
import com.amhsolution.caisse.data.local.toDeliveryInfoOrNull
import com.amhsolution.caisse.data.local.toJsonLines
import com.amhsolution.caisse.data.local.toJsonPayments
import com.amhsolution.caisse.data.local.toOrderLineSnapshots
import com.amhsolution.caisse.data.local.toPaymentRecords
import com.amhsolution.caisse.data.local.toJson
import com.amhsolution.caisse.domain.history.businessDayFromEpochMillis
import com.amhsolution.caisse.domain.history.currentBusinessDay
import com.amhsolution.caisse.domain.history.toCancelledHistoryRecord
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
    database: CaisseDatabase
) {
    private val historyDao = database.historyDao()
    private val ticketCounterDao = database.ticketCounterDao()

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
        val record = HistoryRecord(
            id = id,
            ticketNumber = ticketNumber,
            businessDay = businessDay,
            paidAtEpochMillis = paidAtEpochMillis,
            orderType = orderType,
            lines = lines,
            totalCentimes = totalCentimes,
            payments = payments,
            deliveryInfo = deliveryInfo
        )
        historyDao.insert(record.toEntity())
        return record
    }

    suspend fun insertCancelledOrder(
        order: OrderTicket,
        cancelledAtEpochMillis: Long = System.currentTimeMillis()
    ): HistoryRecord {
        val record = order.toCancelledHistoryRecord(cancelledAtEpochMillis)
        historyDao.insertIfAbsent(record.toEntity())
        return historyDao.getById(record.id)?.toDomain() ?: record
    }

    suspend fun updatePayments(id: String, payments: List<PaymentRecord>) {
        historyDao.updatePaymentsJson(id, payments.toJsonPayments())
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
            deliveryInfoJson = deliveryInfo?.toJson()
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
            deliveryInfo = deliveryInfoJson?.toDeliveryInfoOrNull()
        )
}
