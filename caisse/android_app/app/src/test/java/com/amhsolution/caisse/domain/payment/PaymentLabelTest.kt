package com.amhsolution.caisse.domain.payment

import com.amhsolution.caisse.domain.model.PaymentMethod
import com.amhsolution.caisse.domain.model.PaymentRecord
import org.junit.Assert.assertEquals
import org.junit.Test

class PaymentLabelTest {
    @Test
    fun `libelle paiement simple`() {
        val payments = listOf(PaymentRecord(PaymentMethod.CARTE_BANCAIRE, 3200))
        assertEquals("CB", formatPaymentMethodLabel(payments))
    }

    @Test
    fun `libelle paiement mixte`() {
        val payments = listOf(
            PaymentRecord(PaymentMethod.ESPECES, 2000),
            PaymentRecord(PaymentMethod.CARTE_BANCAIRE, 2200)
        )
        assertEquals("Espèces + CB", formatPaymentMethodLabel(payments))
        assertEquals(
            "Espèces 20,00 € + CB 22,00 €",
            formatPaymentBreakdownLabel(payments)
        )
    }

    @Test
    fun `ventilation regroupe les versements utilises plusieurs fois`() {
        val payments = listOf(
            PaymentRecord(PaymentMethod.ESPECES, 500),
            PaymentRecord(PaymentMethod.CARTE_BANCAIRE, 1200),
            PaymentRecord(PaymentMethod.ESPECES, 300)
        )

        assertEquals(
            "Espèces 8,00 € + CB 12,00 €",
            formatPaymentBreakdownLabel(payments)
        )
    }

    @Test
    fun `libelle ticket restaurant`() {
        val payments = listOf(PaymentRecord(PaymentMethod.TITRE_RESTAURANT, 2150))
        assertEquals("Ticket resto", formatPaymentMethodLabel(payments))
    }
}
