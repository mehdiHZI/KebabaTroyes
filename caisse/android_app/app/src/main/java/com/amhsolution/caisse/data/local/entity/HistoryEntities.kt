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
    val deliveryInfoJson: String?,
    val linkedTicketId: String? = null,
    val isCorrection: Boolean = false,
    val integrityHash: String = "",
    val previousIntegrityHash: String? = null,
    val isSimulation: Boolean = false,
    val registerId: String = "caisse-1"
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
    /** 1 = Z définitive ; 0 = rapport X / provisoire. */
    val isFinal: Boolean = false,
    val closureHash: String = "",
    val previousClosureHash: String? = null
)

/** Totaux perpétuels depuis la 1ère utilisation (jamais remis à zéro sans archivage matériel). */
@Entity(tableName = "perpetual_totals")
data class PerpetualTotalsEntity(
    @PrimaryKey val id: String = "default",
    val totalRevenueCentimes: Long = 0L,
    val ticketCount: Long = 0L,
    val correctionCount: Long = 0L,
    val lastUpdatedEpochMillis: Long = 0L,
    val integrityHash: String = ""
)

/** Journal des archives fiscales générées. */
@Entity(tableName = "fiscal_archives")
data class FiscalArchiveEntity(
    @PrimaryKey val id: String,
    val createdAtEpochMillis: Long,
    val periodFrom: String,
    val periodTo: String,
    val archiveHash: String,
    val relativePath: String,
    val ticketCount: Int,
    val noticeIncluded: Boolean = true
)
