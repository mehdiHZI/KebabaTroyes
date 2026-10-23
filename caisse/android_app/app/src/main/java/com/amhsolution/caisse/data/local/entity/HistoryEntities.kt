package com.amhsolution.caisse.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "history_entries")
data class HistoryEntryEntity(
    @PrimaryKey val id: String,
    val ticketNumber: Int,
    val businessDay: String,
    val paidAtEpochMillis: Long,
    val orderType: String,
    val linesJson: String,
    val totalCentimes: Int,
    val paymentsJson: String,
    val deliveryInfoJson: String?
)

@Entity(tableName = "ticket_counter")
data class TicketCounterEntity(
    @PrimaryKey val businessDay: String,
    val lastIssuedNumber: Int
)

@Entity(
    tableName = "cash_closures",
    indices = [
        Index(value = ["businessDay", "type"], unique = true)
    ]
)
data class CashClosureEntity(
    @PrimaryKey val id: String,
    val type: String,
    val businessDay: String,
    val closedAtEpochMillis: Long,
    val ticketCount: Int,
    val firstTicketNumber: Int?,
    val lastTicketNumber: Int?,
    val totalRevenueCentimes: Int,
    val itemCount: Int,
    val averageBasketCentimes: Int,
    val paymentTotalsJson: String,
    val paymentCountsJson: String,
    val orderTypeTotalsJson: String,
    /** 1 = Z automatique définitive ; 0 = Z manuelle provisoire. */
    val isFinal: Boolean = false
)
