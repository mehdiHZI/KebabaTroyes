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
     * Clôture journalière manuelle (provisoire) : recalcule et écrase la Z du jour.
     * Refusée si une Z automatique définitive existe déjà.
     */
    suspend fun createManualDailyClosure(
        records: List<HistoryRecord>,
        businessDay: String = currentBusinessDay(),
        closedAtEpochMillis: Long = System.currentTimeMillis()
    ): CashClosure {
        val existing = cashClosureDao.getForBusinessDay(businessDay, CashClosureType.DAILY_Z.name)
        check(existing == null || !existing.isFinal) {
            "Cette journee est deja cloturee definitivement (ticket Z automatique)."
        }
        return upsertDailyClosure(
            records = records,
            businessDay = businessDay,
            closedAtEpochMillis = closedAtEpochMillis,
            isFinal = false
        )
    }

    /** Conservé pour compatibilité : équivalent à une clôture manuelle. */
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
            .map { it.businessDay }
            .distinct()
            .filter { it < currentBusinessDay }
            .sorted()

        val createdDailyClosures = eligibleBusinessDays.mapNotNull { businessDay ->
            val existing = cashClosureDao.getForBusinessDay(businessDay, CashClosureType.DAILY_Z.name)
            when {
                existing?.isFinal == true -> null
                else -> upsertDailyClosure(
                    records = records,
                    businessDay = businessDay,
                    closedAtEpochMillis = nowEpochMillis,
                    isFinal = true
                )
            }
        }

        val dailyClosures = (cashClosureDao.getAll().map { it.toDomain() } + createdDailyClosures)
            .filter { it.type == CashClosureType.DAILY_Z }
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
                    val closure = dailyClosures.buildMonthlyCashClosure(
                        periodMonth = periodMonth,
                        closedAtEpochMillis = nowEpochMillis
                    )
                    cashClosureDao.insert(closure.toEntity())
                    writeArchiveJson(closure)
                    closure
                }
            }

        val refreshedDailyClosures = (dailyClosures + createdMonthlyClosures)
            .filter { it.type == CashClosureType.DAILY_Z }

        val createdYearlyClosures = refreshedDailyClosures
            .map { it.businessDay.take(4) }
            .distinct()
            .filter { it < currentYear }
            .sorted()
            .mapNotNull { periodYear ->
                if (cashClosureDao.getForBusinessDay(periodYear, CashClosureType.YEARLY_Z.name) != null) {
                    null
                } else {
                    val closure = refreshedDailyClosures.buildYearlyCashClosure(
                        periodYear = periodYear,
                        closedAtEpochMillis = nowEpochMillis
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

    private suspend fun upsertDailyClosure(
        records: List<HistoryRecord>,
        businessDay: String,
        closedAtEpochMillis: Long,
        isFinal: Boolean
    ): CashClosure {
        val closure = records.buildDailyCashClosure(
            businessDay = businessDay,
            closedAtEpochMillis = closedAtEpochMillis,
            isFinal = isFinal
        )
        // Supprime l'éventuelle ancienne ligne (id différent) puis remplace via id stable.
        cashClosureDao.deleteForBusinessDay(businessDay, CashClosureType.DAILY_Z.name)
        cashClosureDao.upsert(closure.toEntity())
        writeArchiveJson(closure)
        return closure
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
            isFinal = isFinal
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
            isFinal = isFinal
        )
}
