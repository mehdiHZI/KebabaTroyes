package com.amhsolution.caisse.domain.payment

import com.amhsolution.caisse.domain.model.CartLine
import com.amhsolution.caisse.domain.model.OrderLineSnapshot

/**
 * Ligne compacte pour l'écran d'encaissement : intitulé sans composition, avec prix.
 * Utile pour repérer rapidement les montants quand les clients paient séparément.
 */
data class PaymentArticleLine(
    val label: String,
    val totalCentimes: Int
)

fun CartLine.toPaymentArticleLine(): PaymentArticleLine {
    val title = studentMenuCustomization?.let { customization ->
        "Menu étudiant · ${customization.itemProductName}"
    } ?: product.name
    return PaymentArticleLine(
        label = "${quantity}x $title",
        totalCentimes = lineTotalCentimes
    )
}

fun OrderLineSnapshot.toPaymentArticleLine(): PaymentArticleLine {
    val title = studentMenuCustomization?.let { customization ->
        "Menu étudiant · ${customization.itemProductName}"
    } ?: productName
    return PaymentArticleLine(
        label = "${quantity}x $title",
        totalCentimes = totalCentimes
    )
}

fun List<CartLine>.toPaymentArticleLines(): List<PaymentArticleLine> =
    map { it.toPaymentArticleLine() }

fun List<OrderLineSnapshot>.toPaymentArticleLinesFromSnapshots(): List<PaymentArticleLine> =
    map { it.toPaymentArticleLine() }
