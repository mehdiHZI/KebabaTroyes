package com.amhsolution.caisse.domain.history

import com.amhsolution.caisse.domain.model.HistoryRecord
import com.amhsolution.caisse.domain.model.OrderType
import com.amhsolution.caisse.domain.model.PaymentMethod
import com.amhsolution.caisse.domain.model.PaymentRecord
import org.junit.Assert.assertEquals
import org.junit.Test

class HistorySortTest {
    private val records = listOf(
        record("livraison-carte", 3L, OrderType.LIVRAISON, PaymentMethod.CARTE_BANCAIRE),
        record("sur-place-especes", 1L, OrderType.SUR_PLACE, PaymentMethod.ESPECES),
        record("emporter-ticket", 2L, OrderType.A_EMPORTER, PaymentMethod.TITRE_RESTAURANT)
    )

    @Test
    fun `le tri par type suit l'ordre metier`() {
        assertEquals(
            listOf("sur-place-especes", "emporter-ticket", "livraison-carte"),
            records.sortedForHistory(HistorySort.TICKET_TYPE, ascending = true).map { it.id }
        )
    }

    @Test
    fun `le tri par paiement peut etre inverse`() {
        assertEquals(
            listOf("emporter-ticket", "livraison-carte", "sur-place-especes"),
            records.sortedForHistory(HistorySort.PAYMENT, ascending = false).map { it.id }
        )
    }

    private fun record(
        id: String,
        paidAt: Long,
        type: OrderType,
        payment: PaymentMethod
    ) = HistoryRecord(
        id = id,
        ticketNumber = paidAt.toInt(),
        businessDay = "2026-06-22",
        paidAtEpochMillis = paidAt,
        orderType = type,
        lines = emptyList(),
        totalCentimes = 100,
        payments = listOf(PaymentRecord(payment, 100))
    )
}
