package com.amhsolution.caisse.domain.history

import com.amhsolution.caisse.domain.model.HistoryRecord

enum class HistorySort(val label: String) {
    DATE("Date"),
    TICKET_TYPE("Type de ticket"),
    PAYMENT("Paiement")
}

fun List<HistoryRecord>.sortedForHistory(
    sort: HistorySort,
    ascending: Boolean
): List<HistoryRecord> {
    val comparator = when (sort) {
        HistorySort.DATE -> compareBy<HistoryRecord> { it.paidAtEpochMillis }
        HistorySort.TICKET_TYPE -> compareBy<HistoryRecord> { it.orderType.ordinal }
            .thenBy { it.paidAtEpochMillis }
        HistorySort.PAYMENT -> compareBy<HistoryRecord> { record ->
            record.payments.minOfOrNull { it.method.ordinal } ?: Int.MAX_VALUE
        }.thenBy { it.paidAtEpochMillis }
    }
    return sortedWith(if (ascending) comparator else comparator.reversed())
}
