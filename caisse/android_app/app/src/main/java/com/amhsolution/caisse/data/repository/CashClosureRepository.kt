package com.amhsolution.caisse.data.repository

import com.amhsolution.caisse.data.local.CaisseDatabase
import com.amhsolution.caisse.data.local.entity.CashClosureEntity
import com.amhsolution.caisse.data.local.toArchiveJsonString
import com.amhsolution.caisse.data.local.toOrderTypeTotals
import com.amhsolution.caisse.data.local.toOrderTypeTotalsJson
import com.amhsolution.caisse.data.local.toPaymentCounts
import com.amhsolution.caisse.data.local.toPaymentCountsJson
import com.amhsolution.caisse.data.local.toPaymentTotals
import com.amhsolution.caisse.data.local.toPaymentTotalsJson
import com.amhsolution.caisse.domain.closure.CashClosure
import com.amhsolution.caisse.domain.closure.CashClosureType
import com.amhsolution.caisse.domain.closure.buildDailyCashClosure
import com.amhsolution.caisse.domain.closure.buildMonthlyCashClosure
import com.amhsolution.caisse.domain.closure.buildYearlyCashClosure
import com.amhsolution.caisse.domain.history.currentBusinessDay
import com.amhsolution.caisse.domain.integrity.INTEGRITY_GENESIS
import com.amhsolution.caisse.domain.integrity.TicketIntegrity
import com.amhsolution.caisse.domain.model.HistoryRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.time.ZoneId

class CashClosureRepository(
    database: CaisseDatabase,
    private val closureDirectory: File? = null
) {
    constructor(database: CaisseDatabase) : this(database, null)

    private val cashClosureDao = database.cashClosureDao()

    fun observeClosures(): Flow<List<CashClosure>> =
        cashClosureDao.observeAll()
            .map { closures -> closures.map { it.toDomain() } }
            .flowOn(Dispatchers.Default)

    /**
     * Rapport X provisoire : recalculable, n'écrase jamais une Z, ne verrouille pas les tickets.
     */
    suspend fun createRapportX(
        records: List<HistoryRecord>,
        businessDay: String = currentBusinessDay(),
        closedAtEpochMillis: Long = System.currentTimeMillis()
    ): CashClosure {
        val closure = records.buildDailyCashClosure(
            businessDay = businessDay,
            closedAtEpochMillis = closedAtEpochMillis,
            isFinal = false,
            type = CashClosureType.DAILY_X
        )
        cashClosureDao.deleteProvisionalForBusinessDay(businessDay, CashClosureType.DAILY_X.name)
        cashClosureDao.upsert(sealClosure(closure).toEntity())
        val sealed = cashClosureDao.getForBusinessDay(businessDay, CashClosureType.DAILY_X.name)!!.toDomain()
        writeArchiveJson(sealed)
        return sealed
    }

    /**
     * Clôture Z définitive du jour : une seule par businessDay, non écrasable.
     * Après Z, seules les contre-opérations (avoirs) sont autorisées.
     */
    suspend fun createFinalDailyZ(
        records: List<HistoryRecord>,
        businessDay: String = currentBusinessDay(),
        closedAtEpochMillis: Long = System.currentTimeMillis()
    ): CashClosure {
        val existing = cashClosureDao.getForBusinessDay(businessDay, CashClosureType.DAILY_Z.name)
        check(existing == null || !existing.isFinal) {
            "Cette journee est deja cloturee definitivement (ticket Z)."
        }
        val closure = records.buildDailyCashClosure(
            businessDay = businessDay,
            closedAtEpochMillis = closedAtEpochMillis,
            isFinal = true,
            type = CashClosureType.DAILY_Z,
            id = "daily-z-$businessDay"
        )
        val sealed = sealClosure(closure)
        cashClosureDao.insert(sealed.toEntity())
        writeArchiveJson(sealed)
        return sealed
    }

    /**
     * Compatibilité UI stats : génère un rapport X (ne fige plus le jour).
     * Pour une Z définitive manuelle, utiliser [createFinalDailyZ].
     */
    suspend fun createManualDailyClosure(
        records: List<HistoryRecord>,
        businessDay: String = currentBusinessDay(),
        closedAtEpochMillis: Long = System.currentTimeMillis()
    ): CashClosure =
        createRapportX(
            records = records,
            businessDay = businessDay,
            closedAtEpochMillis = closedAtEpochMillis
        )

    /** Conservé pour compatibilité. */
    suspend fun createDailyClosure(
        records: List<HistoryRecord>,
        businessDay: String = currentBusinessDay(),
        closedAtEpochMillis: Long = System.currentTimeMillis()
    ): CashClosure =
        createManualDailyClosure(
            records = records,
            businessDay = businessDay,
            closedAtEpochMillis = closedAtEpochMillis
        )

    suspend fun createAutomaticClosures(
        records: List<HistoryRecord>,
        nowEpochMillis: Long = System.currentTimeMillis(),
        zoneId: ZoneId = ZoneId.systemDefault()
    ): List<CashClosure> {
        val currentBusinessDay = currentBusinessDay(zoneId)
        val currentMonth = currentBusinessDay.take(7)
        val currentYear = currentBusinessDay.take(4)
        val eligibleBusinessDays = records
            .filterNot { it.isSimulation }
            .map { it.businessDay }
            .distinct()
            .filter { it < currentBusinessDay }
            .sorted()

        val createdDailyClosures = eligibleBusinessDays.mapNotNull { businessDay ->
            val existing = cashClosureDao.getForBusinessDay(businessDay, CashClosureType.DAILY_Z.name)
            when {
                existing?.isFinal == true -> null
                existing != null -> null
                else -> createFinalDailyZ(
                    records = records,
                    businessDay = businessDay,
                    closedAtEpochMillis = nowEpochMillis
                )
            }
        }

        val dailyClosures = (cashClosureDao.getAll().map { it.toDomain() } + createdDailyClosures)
            .filter { it.type == CashClosureType.DAILY_Z && it.isFinal }
            .distinctBy { it.businessDay }

        val createdMonthlyClosures = dailyClosures
            .map { it.businessDay.take(7) }
            .distinct()
            .filter { it < currentMonth }
            .sorted()
            .mapNotNull { periodMonth ->
                if (cashClosureDao.getForBusinessDay(periodMonth, CashClosureType.MONTHLY_Z.name) != null) {
                    null
                } else {
                    val closure = sealClosure(
                        dailyClosures.buildMonthlyCashClosure(
                            periodMonth = periodMonth,
                            closedAtEpochMillis = nowEpochMillis
                        )
                    )
                    cashClosureDao.insert(closure.toEntity())
                    writeArchiveJson(closure)
                    closure
                }
            }

        val refreshedDailyClosures = cashClosureDao.getAll().map { it.toDomain() }
            .filter { it.type == CashClosureType.DAILY_Z && it.isFinal }

        val createdYearlyClosures = refreshedDailyClosures
            .map { it.businessDay.take(4) }
            .distinct()
            .filter { it < currentYear }
            .sorted()
            .mapNotNull { periodYear ->
                if (cashClosureDao.getForBusinessDay(periodYear, CashClosureType.YEARLY_Z.name) != null) {
                    null
                } else {
                    val closure = sealClosure(
                        refreshedDailyClosures.buildYearlyCashClosure(
                            periodYear = periodYear,
                            closedAtEpochMillis = nowEpochMillis
                        )
                    )
                    cashClosureDao.insert(closure.toEntity())
                    writeArchiveJson(closure)
                    closure
                }
            }

        return createdDailyClosures + createdMonthlyClosures + createdYearlyClosures
    }

    suspend fun ensureArchiveFiles(closures: List<CashClosure>) {
        closures.forEach { closure -> writeArchiveJson(closure) }
    }

    fun archiveRelativePath(closure: CashClosure): String {
        val year = closure.businessDay.take(4)
        return when (closure.type) {
            CashClosureType.DAILY_X -> {
                val month = closure.businessDay.substring(5, 7)
                "cloture/$year/$month/${closure.businessDay}-rapport-x.json"
            }
            CashClosureType.DAILY_Z -> {
                val month = closure.businessDay.substring(5, 7)
                "cloture/$year/$month/${closure.businessDay}-ticket-z.json"
            }
            CashClosureType.MONTHLY_Z -> {
                val month = closure.businessDay.substring(5, 7)
                "cloture/$year/$month/${closure.businessDay}-ticket-z-mensuel.json"
            }
            CashClosureType.YEARLY_Z -> {
                "cloture/$year/${closure.businessDay}-ticket-z-annuel.json"
            }
        }
    }

    fun pdfRelativePath(closure: CashClosure): String =
        archiveRelativePath(closure).removeSuffix(".json") + ".pdf"

    private suspend fun sealClosure(closure: CashClosure): CashClosure {
        val previous = cashClosureDao.getLatestFinalOfType(closure.type.name)
            ?: cashClosureDao.getLatest()
        val previousHash = previous?.closureHash?.takeIf { it.isNotBlank() } ?: INTEGRITY_GENESIS
        val payload = listOf(
            closure.type.name,
            closure.businessDay,
            closure.closedAtEpochMillis.toString(),
            closure.ticketCount.toString(),
            closure.totalRevenueCentimes.toString(),
            closure.isFinal.toString()
        ).joinToString("|")
        val hash = TicketIntegrity.hash(previousHash, payload)
        return closure.copy(previousClosureHash = previousHash, closureHash = hash)
    }

    private suspend fun writeArchiveJson(closure: CashClosure) {
        val root = closureDirectory ?: return
        withContext(Dispatchers.IO) {
            val file = File(root, archiveRelativePath(closure).removePrefix("cloture/"))
            file.parentFile?.mkdirs()
            file.writeText(closure.toArchiveJsonString(), Charsets.UTF_8)
        }
    }

    private fun CashClosureEntity.toDomain(): CashClosure =
        CashClosure(
            id = id,
            type = CashClosureType.valueOf(type),
            businessDay = businessDay,
            closedAtEpochMillis = closedAtEpochMillis,
            ticketCount = ticketCount,
            firstTicketNumber = firstTicketNumber,
            lastTicketNumber = lastTicketNumber,
            totalRevenueCentimes = totalRevenueCentimes,
            itemCount = itemCount,
            averageBasketCentimes = averageBasketCentimes,
            paymentTotalsCentimes = paymentTotalsJson.toPaymentTotals(),
            paymentCounts = paymentCountsJson.toPaymentCounts(),
            orderTypeTotalsCentimes = orderTypeTotalsJson.toOrderTypeTotals(),
            isFinal = isFinal,
            closureHash = closureHash,
            previousClosureHash = previousClosureHash
        )

    private fun CashClosure.toEntity(): CashClosureEntity =
        CashClosureEntity(
            id = id,
            type = type.name,
            businessDay = businessDay,
            closedAtEpochMillis = closedAtEpochMillis,
            ticketCount = ticketCount,
            firstTicketNumber = firstTicketNumber,
            lastTicketNumber = lastTicketNumber,
            totalRevenueCentimes = totalRevenueCentimes,
            itemCount = itemCount,
            averageBasketCentimes = averageBasketCentimes,
            paymentTotalsJson = paymentTotalsCentimes.toPaymentTotalsJson(),
            paymentCountsJson = paymentCounts.toPaymentCountsJson(),
            orderTypeTotalsJson = orderTypeTotalsCentimes.toOrderTypeTotalsJson(),
            isFinal = isFinal,
            closureHash = closureHash,
            previousClosureHash = previousClosureHash
        )
}
