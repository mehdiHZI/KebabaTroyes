package com.amhsolution.caisse.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sync_outbox",
    indices = [Index(value = ["status"]), Index(value = ["ticketId"], unique = true)]
)
data class SyncOutboxEntity(
    @PrimaryKey val id: String,
    val ticketId: String,
    val payloadJson: String,
    val status: String,
    val attemptCount: Int = 0,
    val lastError: String? = null,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long
)

object SyncOutboxStatus {
    const val PENDING = "PENDING"
    const val SYNCED = "SYNCED"
    const val ERROR = "ERROR"
}
