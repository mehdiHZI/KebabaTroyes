package com.amhsolution.caisse.domain.statistics

import com.amhsolution.caisse.domain.history.HistoryDateFilter
import com.amhsolution.caisse.domain.history.matchesBusinessDay
import com.amhsolution.caisse.domain.model.HistoryRecord
import com.amhsolution.caisse.domain.model.PaymentMethod

data class SessionStatistics(
    val totalOrders: Int,
    val activeOrders: Int,
    val cancelledOrders: Int,
    val totalRevenueCentimes: Int,
    val cashRevenueCentimes: Int,
    val cardRevenueCentimes: Int,
    val ticketRestoRevenueCentimes: Int,
    val otherRevenueCentimes: Int
) {
    val paymentBreakdownTotalCentimes: Int
        get() = cashRevenueCentimes +
            cardRevenueCentimes +
            ticketRestoRevenueCentimes +
            otherRevenueCentimes
}

fun List<HistoryRecord>.computeStatistics(
    dateFilter: HistoryDateFilter = HistoryDateFilter.today()
): SessionStatistics {
    val records = filter { dateFilter.matchesBusinessDay(it.businessDay) }
    val activeRecords = records.filterNot { it.isCancelled }

    return SessionStatistics(
        totalOrders = records.size,
        activeOrders = activeRecords.size,
        cancelledOrders = records.count { it.isCancelled },
        totalRevenueCentimes = activeRecords.sumOf { it.effectiveTotalCentimes },
        cashRevenueCentimes = activeRecords.sumPaymentsFor(PaymentMethod.ESPECES),
        cardRevenueCentimes = activeRecords.sumPaymentsFor(PaymentMethod.CARTE_BANCAIRE),
        ticketRestoRevenueCentimes = activeRecords.sumPaymentsFor(PaymentMethod.TITRE_RESTAURANT),
        otherRevenueCentimes = activeRecords.sumPaymentsFor(PaymentMethod.AUTRE)
    )
}

private fun List<HistoryRecord>.sumPaymentsFor(method: PaymentMethod): Int =
    sumOf { record ->
        record.payments
            .filter { it.method == method }
            .sumOf { it.amountCentimes }
    }
