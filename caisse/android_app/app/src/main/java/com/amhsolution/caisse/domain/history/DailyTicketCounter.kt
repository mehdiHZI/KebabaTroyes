package com.amhsolution.caisse.domain.history

const val MAX_DAILY_TICKET_NUMBER = 999

fun nextDailyTicketNumber(
    businessDay: String,
    counterDay: String?,
    lastIssuedNumber: Int?
): Int {
    if (counterDay != businessDay) return 1

    val next = (lastIssuedNumber ?: 0) + 1
    require(next <= MAX_DAILY_TICKET_NUMBER) {
        "Limite journaliere de $MAX_DAILY_TICKET_NUMBER tickets atteinte."
    }
    return next
}
