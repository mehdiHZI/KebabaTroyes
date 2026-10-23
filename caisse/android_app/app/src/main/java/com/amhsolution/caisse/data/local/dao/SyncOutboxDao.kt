package com.amhsolution.caisse.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.amhsolution.caisse.data.local.entity.SyncOutboxEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncOutboxDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(entity: SyncOutboxEntity)

    @Query(
        """
        SELECT * FROM sync_outbox
        WHERE status IN ('PENDING', 'ERROR')
        ORDER BY createdAtEpochMillis ASC
        LIMIT :limit
        """
    )
    suspend fun listPending(limit: Int = 50): List<SyncOutboxEntity>

    @Query("SELECT COUNT(*) FROM sync_outbox WHERE status IN ('PENDING', 'ERROR')")
    fun observePendingCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM sync_outbox WHERE status = 'ERROR'")
    fun observeErrorCount(): Flow<Int>

    @Query(
        """
        UPDATE sync_outbox
        SET status = :status,
            attemptCount = :attemptCount,
            lastError = :lastError,
            updatedAtEpochMillis = :updatedAt
        WHERE id = :id
        """
    )
    suspend fun updateStatus(
        id: String,
        status: String,
        attemptCount: Int,
        lastError: String?,
        updatedAt: Long
    )
}
