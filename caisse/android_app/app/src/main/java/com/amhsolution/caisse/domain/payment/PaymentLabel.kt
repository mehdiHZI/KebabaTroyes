package com.amhsolution.caisse.domain.payment

import com.amhsolution.caisse.domain.formatEuros
import com.amhsolution.caisse.domain.model.PaymentMethod
import com.amhsolution.caisse.domain.model.PaymentRecord

fun paymentMethodShortLabel(method: PaymentMethod): String =
    when (method) {
        PaymentMethod.ESPECES -> "Espèces"
        PaymentMethod.CARTE_BANCAIRE -> "CB"
        PaymentMethod.TITRE_RESTAURANT -> "Ticket resto"
        PaymentMethod.AUTRE -> "Autre"
        PaymentMethod.ANNULE -> "Annulé"
    }

fun formatPaymentMethodLabel(payments: List<PaymentRecord>): String {
    if (payments.isEmpty()) return "—"

    val labels = payments.map { paymentMethodShortLabel(it.method) }.distinct()
    return labels.joinToString(" + ")
}

fun formatPaymentBreakdownLabel(payments: List<PaymentRecord>): String {
    if (payments.isEmpty()) return "—"

    val totalsByMethod = linkedMapOf<PaymentMethod, Int>()
    payments.forEach { payment ->
        totalsByMethod[payment.method] =
            totalsByMethod.getOrDefault(payment.method, 0) + payment.amountCentimes
    }

    return totalsByMethod.entries.joinToString(" + ") { (method, amountCentimes) ->
        "${paymentMethodShortLabel(method)} ${formatEuros(amountCentimes)}"
    }
}

fun singlePaymentForTotal(method: PaymentMethod, totalCentimes: Int): PaymentRecord =
    PaymentRecord(
        method = method,
        amountCentimes = totalCentimes
    )
