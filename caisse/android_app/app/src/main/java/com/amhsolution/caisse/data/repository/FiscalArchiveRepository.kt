package com.amhsolution.caisse.data.repository

import com.amhsolution.caisse.data.local.CaisseDatabase
import com.amhsolution.caisse.data.local.entity.FiscalArchiveEntity
import com.amhsolution.caisse.domain.integrity.TicketIntegrity
import com.amhsolution.caisse.domain.model.HistoryRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

data class FiscalArchiveSummary(
    val id: String,
    val createdAtEpochMillis: Long,
    val periodFrom: String,
    val periodTo: String,
    val archiveHash: String,
    val relativePath: String,
    val ticketCount: Int
)

/**
 * Archive fiscale figée (JSON + empreintes). Purge des tickets uniquement après archive complète.
 */
class FiscalArchiveRepository(
    database: CaisseDatabase,
    private val archiveRoot: File
) {
    private val fiscalArchiveDao = database.fiscalArchiveDao()

    fun observeArchives(): Flow<List<FiscalArchiveSummary>> =
        fiscalArchiveDao.observeAll()
            .map { list -> list.map { it.toSummary() } }
            .flowOn(Dispatchers.Default)

    suspend fun createArchive(
        records: List<HistoryRecord>,
        periodFrom: String,
        periodTo: String,
        createdAtEpochMillis: Long = System.currentTimeMillis()
    ): FiscalArchiveSummary = withContext(Dispatchers.IO) {
        val production = records
            .filterNot { it.isSimulation }
            .filter { it.businessDay in periodFrom..periodTo }
            .sortedWith(compareBy({ it.businessDay }, { it.ticketNumber }, { it.paidAtEpochMillis }))

        require(production.isNotEmpty()) { "Aucune donnee de production a archiver sur la periode." }

        val ticketsJson = JSONArray()
        production.forEach { record ->
            ticketsJson.put(
                JSONObject()
                    .put("id", record.id)
                    .put("ticketNumber", record.ticketNumber)
                    .put("businessDay", record.businessDay)
                    .put("paidAtEpochMillis", record.paidAtEpochMillis)
                    .put("orderType", record.orderType.name)
                    .put("totalCentimes", record.totalCentimes)
                    .put("isCorrection", record.isCorrection)
                    .put("linkedTicketId", record.linkedTicketId)
                    .put("integrityHash", record.integrityHash)
                    .put("previousIntegrityHash", record.previousIntegrityHash)
                    .put("registerId", record.registerId)
            )
        }

        val body = JSONObject()
            .put("format", "kebaba-fiscal-archive-v1")
            .put("createdAtEpochMillis", createdAtEpochMillis)
            .put("periodFrom", periodFrom)
            .put("periodTo", periodTo)
            .put("ticketCount", production.size)
            .put("notice", "Voir caisse/docs/conformite/notice-archive.md")
            .put("tickets", ticketsJson)

        val archiveHash = TicketIntegrity.sha256Hex(body.toString())
        body.put("archiveHash", archiveHash)

        val id = UUID.randomUUID().toString()
        val relativePath = "fiscal/${periodFrom}_to_${periodTo}_$id.json"
        val file = File(archiveRoot, relativePath)
        file.parentFile?.mkdirs()
        file.writeText(body.toString(2), Charsets.UTF_8)

        val noticeFile = File(archiveRoot, "fiscal/NOTICE_ARCHIVE.txt")
        if (!noticeFile.exists()) {
            noticeFile.writeText(
                """
                Archive fiscale Kebaba Caisse
                Format ouvert JSON (kebaba-fiscal-archive-v1).
                Contient le détail des tickets de production, empreintes d'intégrité et période.
                Notice complète : caisse/docs/conformite/notice-archive.md
                """.trimIndent(),
                Charsets.UTF_8
            )
        }

        val entity = FiscalArchiveEntity(
            id = id,
            createdAtEpochMillis = createdAtEpochMillis,
            periodFrom = periodFrom,
            periodTo = periodTo,
            archiveHash = archiveHash,
            relativePath = relativePath,
            ticketCount = production.size,
            noticeIncluded = true
        )
        fiscalArchiveDao.insert(entity)
        entity.toSummary()
    }

    /**
     * Purge autorisée uniquement si une archive couvre déjà toute la période demandée.
     * Ne supprime jamais les totaux perpétuels.
     */
    suspend fun purgeProductionTicketsIfArchived(
        periodFrom: String,
        periodTo: String
    ): Int = withContext(Dispatchers.IO) {
        val archives = fiscalArchiveDao.getAll()
        val covered = archives.any { archive ->
            archive.periodFrom <= periodFrom && archive.periodTo >= periodTo
        }
        check(covered) {
            "Purge interdite : generer une archive fiscale complete avant toute purge."
        }
        // Purge non destructive ici : journal seulement (Room DELETE à brancher après validation métier).
        0
    }

    private fun FiscalArchiveEntity.toSummary() = FiscalArchiveSummary(
        id = id,
        createdAtEpochMillis = createdAtEpochMillis,
        periodFrom = periodFrom,
        periodTo = periodTo,
        archiveHash = archiveHash,
        relativePath = relativePath,
        ticketCount = ticketCount
    )
}
