package com.amhsolution.caisse.domain.model

data class PaymentRecord(
    val method: PaymentMethod,
    val amountCentimes: Int,
    val receivedCentimes: Int? = null,
    val changeCentimes: Int? = null
)
