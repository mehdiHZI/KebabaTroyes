package com.amhsolution.caisse.domain.payment

import com.amhsolution.caisse.domain.model.PaymentMethod
import com.amhsolution.caisse.domain.model.PaymentRecord

private const val MAX_INPUT_CENTIMES = 9_999_999

fun appendKeypadDigit(currentCentimes: Int, digit: Int): Int {
    require(digit in 0..9)
    val next = currentCentimes * 10 + digit
    return next.coerceAtMost(MAX_INPUT_CENTIMES)
}

fun clearKeypadInput(): Int = 0

fun totalPaidCentimes(payments: List<PaymentRecord>): Int =
    payments.sumOf { it.amountCentimes }

fun remainingCentimes(totalCentimes: Int, payments: List<PaymentRecord>): Int =
    (totalCentimes - totalPaidCentimes(payments)).coerceAtLeast(0)

fun cashChangeCentimes(
    inputCentimes: Int,
    remainingCentimes: Int
): Int? {
    if (inputCentimes <= remainingCentimes) return null
    return inputCentimes - remainingCentimes
}

fun buildPaymentRecord(
    method: PaymentMethod,
    inputCentimes: Int,
    remainingCentimes: Int
): PaymentRecord? {
    if (inputCentimes <= 0 || remainingCentimes <= 0) return null

    return when (method) {
        PaymentMethod.ESPECES -> {
            val applied = minOf(inputCentimes, remainingCentimes)
            PaymentRecord(
                method = method,
                amountCentimes = applied,
                receivedCentimes = inputCentimes,
                changeCentimes = cashChangeCentimes(inputCentimes, remainingCentimes)
            )
        }

        PaymentMethod.ANNULE -> null

        else -> PaymentRecord(
            method = method,
            amountCentimes = minOf(inputCentimes, remainingCentimes)
        )
    }
}

fun formatPaymentsSummary(payments: List<PaymentRecord>): String {
    if (payments.isEmpty()) return "Paiements : —"

    val parts = payments.map { payment ->
        val shortLabel = when (payment.method) {
            PaymentMethod.ESPECES -> "Espèces"
            PaymentMethod.CARTE_BANCAIRE -> "CB"
            PaymentMethod.TITRE_RESTAURANT -> "TR"
            PaymentMethod.AUTRE -> "Autre"
            PaymentMethod.ANNULE -> "Annulé"
        }
        val euros = payment.amountCentimes / 100
        val cents = payment.amountCentimes % 100
        "$shortLabel ${euros},${cents.toString().padStart(2, '0')} EUR"
    }

    return "Paiements : ${parts.joinToString(" + ")}"
}
