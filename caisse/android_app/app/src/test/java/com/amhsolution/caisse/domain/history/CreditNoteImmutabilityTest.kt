package com.amhsolution.caisse.domain.history

import com.amhsolution.caisse.domain.model.HistoryRecord
import com.amhsolution.caisse.domain.model.OrderLineSnapshot
import com.amhsolution.caisse.domain.model.OrderType
import com.amhsolution.caisse.domain.model.PaymentMethod
import com.amhsolution.caisse.domain.model.PaymentRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class CreditNoteImmutabilityTest {
    @Test
    fun `refuse un avoir sur une contre-operation`() {
        val credit = sample().toCreditNote(ticketNumber = 2)
        assertThrows(IllegalArgumentException::class.java) {
            credit.toCreditNote(ticketNumber = 3)
        }
    }

    @Test
    fun `refuse un avoir sur un ticket legacy annule`() {
        val cancelled = sample().copy(
            payments = listOf(PaymentRecord(PaymentMethod.ANNULE, 0))
        )
        assertThrows(IllegalArgumentException::class.java) {
            cancelled.toCreditNote(ticketNumber = 2)
        }
    }

    @Test
    fun `montant avoir egal a l oppose du total effectif`() {
        val original = sample()
        val credit = original.toCreditNote(ticketNumber = 9, createdAtEpochMillis = 1_800_000_000_000L)
        assertEquals(-original.effectiveTotalCentimes, credit.effectiveTotalCentimes)
        assertEquals("AVOIR", credit.payments.single().method.name)
    }

    private fun sample() = HistoryRecord(
        id = "sale-1",
        ticketNumber = 1,
        businessDay = "2026-11-02",
        paidAtEpochMillis = 1_799_000_000_000L,
        orderType = OrderType.A_EMPORTER,
        lines = listOf(
            OrderLineSnapshot(
                productName = "Kofte",
                quantity = 1,
                totalCentimes = 950,
                productId = "kofte",
                unitPriceCentimes = 950
            )
        ),
        totalCentimes = 950,
        payments = listOf(PaymentRecord(PaymentMethod.CARTE_BANCAIRE, 950))
    )
}
