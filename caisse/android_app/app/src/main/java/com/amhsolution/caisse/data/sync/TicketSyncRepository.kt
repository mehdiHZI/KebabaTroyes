package com.amhsolution.caisse.data.sync

import com.amhsolution.caisse.data.local.CaisseDatabase
import com.amhsolution.caisse.data.local.entity.SyncOutboxEntity
import com.amhsolution.caisse.data.local.entity.SyncOutboxStatus
import com.amhsolution.caisse.domain.model.HistoryRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.UUID

enum class SyncUiState {
    DISABLED,
    OK,
    PENDING,
    ERROR
}

data class SyncStatusSnapshot(
    val state: SyncUiState,
    val pendingCount: Int,
    val errorCount: Int,
    val lastMessage: String? = null
)

class TicketSyncRepository(
    database: CaisseDatabase,
    private val serverConfigRepository: ServerConfigRepository,
    private val apiClient: CaisseApiClient = CaisseApiClient()
) {
    private val outboxDao = database.syncOutboxDao()
    private val _lastMessage = MutableStateFlow<String?>(null)
    val lastMessage: StateFlow<String?> = _lastMessage.asStateFlow()

    val status: Flow<SyncStatusSnapshot> =
        combine(
            serverConfigRepository.config,
            outboxDao.observePendingCount(),
            outboxDao.observeErrorCount(),
            _lastMessage
        ) { config, pending, errors, message ->
            when {
                !config.isConfigured -> SyncStatusSnapshot(
                    SyncUiState.DISABLED,
                    pending,
                    errors,
                    "API non configurée"
                )
                errors > 0 -> SyncStatusSnapshot(SyncUiState.ERROR, pending, errors, message)
                pending > 0 -> SyncStatusSnapshot(SyncUiState.PENDING, pending, errors, message)
                else -> SyncStatusSnapshot(SyncUiState.OK, 0, 0, message ?: "Synchronisé")
            }
        }

    suspend fun enqueuePaidTicket(record: HistoryRecord) = withContext(Dispatchers.IO) {
        val config = serverConfigRepository.config.first()
        val now = System.currentTimeMillis()
        val payload = record.toSyncPayloadJson(config.registerId)
        outboxDao.insertIgnore(
            SyncOutboxEntity(
                id = UUID.randomUUID().toString(),
                ticketId = record.id,
                payloadJson = payload,
                status = SyncOutboxStatus.PENDING,
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now
            )
        )
        flush()
    }

    suspend fun flush(): Int = withContext(Dispatchers.IO) {
        val config = serverConfigRepository.config.first()
        if (!config.isConfigured) {
            _lastMessage.value = "API non configurée"
            return@withContext 0
        }

        var synced = 0
        val pending = outboxDao.listPending()
        for (item in pending) {
            when (val result = apiClient.pushTicket(config, item.payloadJson)) {
                is CaisseApiResult.Success -> {
                    outboxDao.updateStatus(
                        id = item.id,
                        status = SyncOutboxStatus.SYNCED,
                        attemptCount = item.attemptCount + 1,
                        lastError = null,
                        updatedAt = System.currentTimeMillis()
                    )
                    synced += 1
                    _lastMessage.value = "Ticket sync OK"
                }
                is CaisseApiResult.Failure -> {
                    outboxDao.updateStatus(
                        id = item.id,
                        status = if (result.retryable) SyncOutboxStatus.PENDING else SyncOutboxStatus.ERROR,
                        attemptCount = item.attemptCount + 1,
                        lastError = result.message,
                        updatedAt = System.currentTimeMillis()
                    )
                    _lastMessage.value = result.message
                    if (!result.retryable) continue
                    // stop batch on network error to avoid hammering
                    break
                }
            }
        }
        synced
    }
}
