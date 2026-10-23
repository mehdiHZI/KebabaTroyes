package com.amhsolution.caisse.domain.history

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val historyDateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM HH:mm", Locale.FRANCE)
private val businessDayFormatter = DateTimeFormatter.ISO_LOCAL_DATE
val CashClosureCutoffTime: LocalTime = LocalTime.of(23, 59)

fun currentBusinessDay(zoneId: ZoneId = ZoneId.systemDefault()): String =
    businessDayFromEpochMillis(System.currentTimeMillis(), zoneId)

fun businessDayFromEpochMillis(
    epochMillis: Long,
    zoneId: ZoneId = ZoneId.systemDefault()
): String {
    val localDateTime = Instant.ofEpochMilli(epochMillis).atZone(zoneId).toLocalDateTime()
    val businessDay = if (!localDateTime.toLocalTime().isBefore(CashClosureCutoffTime)) {
        localDateTime.toLocalDate().plusDays(1)
    } else {
        localDateTime.toLocalDate()
    }
    return businessDay.format(businessDayFormatter)
}

fun businessDayString(day: LocalDate): String =
    day.format(businessDayFormatter)

fun formatHistoryDateTime(
    epochMillis: Long,
    zoneId: ZoneId = ZoneId.systemDefault()
): String =
    Instant.ofEpochMilli(epochMillis).atZone(zoneId).format(historyDateTimeFormatter)
