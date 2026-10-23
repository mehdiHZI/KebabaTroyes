package com.amhsolution.caisse.domain.history

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

sealed interface HistoryDateFilter {
    data class Single(val day: LocalDate) : HistoryDateFilter
    data class Range(val start: LocalDate, val end: LocalDate) : HistoryDateFilter

    companion object {
        fun today(): HistoryDateFilter = Single(LocalDate.parse(currentBusinessDay()))
    }
}

fun HistoryDateFilter.matchesBusinessDay(businessDay: String): Boolean {
    val date = LocalDate.parse(businessDay)
    return when (this) {
        is HistoryDateFilter.Single -> date == day
        is HistoryDateFilter.Range -> !date.isBefore(start) && !date.isAfter(end)
    }
}

fun HistoryDateFilter.displayLabel(): String {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.FRANCE)
    val today = LocalDate.parse(currentBusinessDay())

    return when (this) {
        is HistoryDateFilter.Single -> {
            if (day == today) "Journee : aujourd'hui"
            else "Journee : ${day.format(formatter)}"
        }

        is HistoryDateFilter.Range -> {
            if (start == end) {
                if (start == today) "Journee : aujourd'hui"
                else "Journee : ${start.format(formatter)}"
            } else {
                "Periode : ${start.format(formatter)} - ${end.format(formatter)}"
            }
        }
    }
}
