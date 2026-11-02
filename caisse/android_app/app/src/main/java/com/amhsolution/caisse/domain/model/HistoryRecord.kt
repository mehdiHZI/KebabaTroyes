package com.amhsolution.caisse.domain.model

import com.amhsolution.caisse.domain.history.formatHistoryDateTime
import com.amhsolution.caisse.domain.payment.formatPaymentBreakdownLabel
import com.amhsolution.caisse.domain.payment.formatPaymentMethodLabel

data class HistoryRecord(
    val id: String,
    val ticketNumber: Int,
    val businessDay: String,
    val paidAtEpochMillis: Long,
    val orderType: OrderType,
    val lines: List<OrderLineSnapshot>,
    val totalCentimes: Int,
    val payments: List<PaymentRecord>,
    val deliveryInfo: DeliveryInfo? = null,
    /** Ticket d'origine si avoir / contre-opération. */
    val linkedTicketId: String? = null,
    val isCorrection: Boolean = false,
    val integrityHash: String = "",
    val previousIntegrityHash: String? = null,
    val isSimulation: Boolean = false,
    val registerId: String = "caisse-1"
) {
    val dateTimeLabel: String
        get() = formatHistoryDateTime(paidAtEpochMillis)

    val ticketLabel: String
        get() = "#$ticketNumber"

    val paymentLabel: String
        get() = formatPaymentMethodLabel(payments)

    val paymentBreakdownLabel: String
        get() = formatPaymentBreakdownLabel(payments)

    /** Legacy Milano : paiement ANNULE sur le même ticket (ne plus créer). */
    val isCancelled: Boolean
        get() = payments.any { it.method == PaymentMethod.ANNULE }

    val effectiveTotalCentimes: Int
        get() = when {
            isCancelled -> 0
            else -> totalCentimes
        }

    val itemsLabel: String
        get() = lines.joinToString(separator = ", ") { it.displayLabel }
}
