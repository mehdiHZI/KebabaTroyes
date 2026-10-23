package com.amhsolution.caisse.domain

import java.util.Locale

fun formatEuros(centimes: Int): String {
    val euros = centimes / 100
    val cents = kotlin.math.abs(centimes % 100)
    return String.format(Locale.FRANCE, "%d,%02d €", euros, cents)
}

fun formatEurosAmount(centimes: Int): String {
    val euros = centimes / 100
    val cents = kotlin.math.abs(centimes % 100)
    return String.format(Locale.FRANCE, "%d,%02d", euros, cents)
}

fun parseEurosInput(text: String): Int? {
    val cleaned = text
        .replace("€", "")
        .replace(" ", "")
        .trim()
        .replace(',', '.')
    if (cleaned.isEmpty()) return null

    val value = cleaned.toDoubleOrNull() ?: return null
    if (value < 0) return null

    return kotlin.math.round(value * 100).toInt()
}
