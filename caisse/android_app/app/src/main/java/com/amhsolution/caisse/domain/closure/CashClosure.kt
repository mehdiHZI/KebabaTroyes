package com.amhsolution.caisse.domain.closure

import com.amhsolution.caisse.domain.model.HistoryRecord
import com.amhsolution.caisse.domain.model.OrderType
import com.amhsolution.caisse.domain.model.PaymentMethod

enum class CashClosureType(val label: String) {
    DAILY_Z("Ticket Z journalier"),
    MONTHLY_Z("Ticket Z mensuel"),
    YEARLY_Z("Ticket Z annuel")
}

data class CashClosure(
    val id: String,
    val type: CashClosureType,
    val businessDay: String,
    val closedAtEpochMillis: Long,
    val ticketCount: Int,
    val firstTicketNumber: Int?,
    val lastTicketNumber: Int?,
    val totalRevenueCentimes: Int,
    val itemCount: Int,
    val averageBasketCentimes: Int,
    val paymentTotalsCentimes: Map<PaymentMethod, Int>,
    val paymentCounts: Map<PaymentMethod, Int>,
    val orderTypeTotalsCentimes: Map<OrderType, Int>,
    /**
     * true = ticket Z automatique (définitif) : les tickets du jour sont verrouillés.
     * false = clôture manuelle (provisoire) : recalculable / écrasable.
     */
    val isFinal: Boolean = false
) {
    val cancelledOrderCount: Int
        get() = paymentCounts[PaymentMethod.ANNULE] ?: 0

    val locksTickets: Boolean
        get() = type == CashClosureType.DAILY_Z && isFinal

    val statusLabel: String
        get() = when {
            type != CashClosureType.DAILY_Z -> type.label
            isFinal -> "Cloture automatique (definitive)"
            else -> "Cloture manuelle (provisoire)"
        }

    val ticketRangeLabel: String
        get() = when {
            firstTicketNumber == null || lastTicketNumber == null -> "Aucun ticket"
            firstTicketNumber == lastTicketNumber -> "#$firstTicketNumber"
            else -> "#$firstTicketNumber - #$lastTicketNumber"
        }
}

fun List<HistoryRecord>.buildDailyCashClosure(
    businessDay: String,
    closedAtEpochMillis: Long,
    isFinal: Boolean = false,
    id: String = "daily-z-$businessDay"
): CashClosure {
    val dayRecords = filter { it.businessDay == businessDay }
        .sortedWith(compareBy<HistoryRecord> { it.ticketNumber }.thenBy { it.paidAtEpochMillis })
    val records = dayRecords.filterNot { it.isCancelled }

    require(dayRecords.isNotEmpty()) {
        "Impossible de cloturer une journee sans commande."
    }

    val paymentTotals = records
        .flatMap { it.payments }
        .groupingBy { it.method }
        .fold(0) { total, payment -> total + payment.amountCentimes }

    val paymentCounts = records
        .flatMap { it.payments }
        .groupingBy { it.method }
        .eachCount()
        .toMutableMap()
        .apply {
            this[PaymentMethod.ANNULE] = dayRecords.count { it.isCancelled }
        }

    val orderTypeTotals = records
        .groupingBy { it.orderType }
        .fold(0) { total, record -> total + record.effectiveTotalCentimes }

    val totalRevenueCentimes = records.sumOf { it.effectiveTotalCentimes }
    val itemCount = records.sumOf { record -> record.lines.sumOf { it.quantity } }

    return CashClosure(
        id = id,
        type = CashClosureType.DAILY_Z,
        businessDay = businessDay,
        closedAtEpochMillis = closedAtEpochMillis,
        ticketCount = records.size,
        firstTicketNumber = dayRecords.firstOrNull()?.ticketNumber,
        lastTicketNumber = dayRecords.lastOrNull()?.ticketNumber,
        totalRevenueCentimes = totalRevenueCentimes,
        itemCount = itemCount,
        averageBasketCentimes = if (records.isNotEmpty()) totalRevenueCentimes / records.size else 0,
        paymentTotalsCentimes = paymentTotals,
        paymentCounts = paymentCounts,
        orderTypeTotalsCentimes = orderTypeTotals,
        isFinal = isFinal
    )
}

fun List<CashClosure>.buildMonthlyCashClosure(
    periodMonth: String,
    closedAtEpochMillis: Long,
    id: String = "monthly-z-$periodMonth-$closedAtEpochMillis"
): CashClosure {
    val dailyClosures = filter {
        it.type == CashClosureType.DAILY_Z && it.businessDay.startsWith("$periodMonth-")
    }
    return dailyClosures.buildAggregatedCashClosure(
        type = CashClosureType.MONTHLY_Z,
        periodKey = periodMonth,
        closedAtEpochMillis = closedAtEpochMillis,
        id = id
    )
}

fun List<CashClosure>.buildYearlyCashClosure(
    periodYear: String,
    closedAtEpochMillis: Long,
    id: String = "yearly-z-$periodYear-$closedAtEpochMillis"
): CashClosure {
    val dailyClosures = filter {
        it.type == CashClosureType.DAILY_Z && it.businessDay.startsWith("$periodYear-")
    }
    return dailyClosures.buildAggregatedCashClosure(
        type = CashClosureType.YEARLY_Z,
        periodKey = periodYear,
        closedAtEpochMillis = closedAtEpochMillis,
        id = id
    )
}

private fun List<CashClosure>.buildAggregatedCashClosure(
    type: CashClosureType,
    periodKey: String,
    closedAtEpochMillis: Long,
    id: String
): CashClosure {
    val dailyClosures = filter { it.type == CashClosureType.DAILY_Z }
        .sortedBy { it.businessDay }

    require(dailyClosures.isNotEmpty()) {
        "Impossible de creer une cloture ${type.label.lowercase()} sans cloture journaliere."
    }

    val ticketCount = dailyClosures.sumOf { it.ticketCount }
    val totalRevenueCentimes = dailyClosures.sumOf { it.totalRevenueCentimes }
    val itemCount = dailyClosures.sumOf { it.itemCount }
    val paymentTotals = PaymentMethod.entries.associateWith { method ->
        dailyClosures.sumOf { it.paymentTotalsCentimes[method] ?: 0 }
    }
    val paymentCounts = PaymentMethod.entries.associateWith { method ->
        dailyClosures.sumOf { it.paymentCounts[method] ?: 0 }
    }
    val orderTypeTotals = OrderType.entries.associateWith { orderType ->
        dailyClosures.sumOf { it.orderTypeTotalsCentimes[orderType] ?: 0 }
    }

    return CashClosure(
        id = id,
        type = type,
        businessDay = periodKey,
        closedAtEpochMillis = closedAtEpochMillis,
        ticketCount = ticketCount,
        firstTicketNumber = dailyClosures.mapNotNull { it.firstTicketNumber }.minOrNull(),
        lastTicketNumber = dailyClosures.mapNotNull { it.lastTicketNumber }.maxOrNull(),
        totalRevenueCentimes = totalRevenueCentimes,
        itemCount = itemCount,
        averageBasketCentimes = if (ticketCount > 0) totalRevenueCentimes / ticketCount else 0,
        paymentTotalsCentimes = paymentTotals,
        paymentCounts = paymentCounts,
        orderTypeTotalsCentimes = orderTypeTotals,
        isFinal = true
    )
}
