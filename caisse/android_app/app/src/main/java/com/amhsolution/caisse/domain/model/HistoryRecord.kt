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
    val deliveryInfo: DeliveryInfo? = null
) {
    val dateTimeLabel: String
        get() = formatHistoryDateTime(paidAtEpochMillis)

    val ticketLabel: String
        get() = "#$ticketNumber"

    val paymentLabel: String
        get() = formatPaymentMethodLabel(payments)

    val paymentBreakdownLabel: String
        get() = formatPaymentBreakdownLabel(payments)

    val isCancelled: Boolean
        get() = payments.any { it.method == PaymentMethod.ANNULE }

    val effectiveTotalCentimes: Int
        get() = if (isCancelled) 0 else totalCentimes

    val itemsLabel: String
        get() = lines.joinToString(separator = ", ") { it.displayLabel }
}
