package com.amhsolution.caisse.domain.closure

import com.amhsolution.caisse.domain.model.HistoryRecord
import com.amhsolution.caisse.domain.model.OrderLineSnapshot
import com.amhsolution.caisse.domain.model.OrderType
import com.amhsolution.caisse.domain.model.PaymentMethod
import com.amhsolution.caisse.domain.model.PaymentRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CashClosureXzTest {
    @Test
    fun `rapport X ne verrouille pas`() {
        val x = listOf(record()).buildDailyCashClosure(
            businessDay = "2026-11-02",
            closedAtEpochMillis = 1L,
            isFinal = false
        )
        assertEquals(CashClosureType.DAILY_X, x.type)
        assertFalse(x.isFinal)
        assertFalse(x.locksTickets)
    }

    @Test
    fun `Z definitive verrouille`() {
        val z = listOf(record()).buildDailyCashClosure(
            businessDay = "2026-11-02",
            closedAtEpochMillis = 1L,
            isFinal = true
        )
        assertEquals(CashClosureType.DAILY_Z, z.type)
        assertTrue(z.isFinal)
        assertTrue(z.locksTickets)
    }

    @Test
    fun `exclut les tickets FACTICE des clotures`() {
        val records = listOf(
            record(id = "prod", total = 1000),
            record(id = "fake", total = 5000).copy(isSimulation = true)
        )
        val closure = records.buildDailyCashClosure(
            businessDay = "2026-11-02",
            closedAtEpochMillis = 1L,
            isFinal = true
        )
        assertEquals(1, closure.ticketCount)
        assertEquals(1000, closure.totalRevenueCentimes)
    }

    private fun record(id: String = "t1", total: Int = 1000) = HistoryRecord(
        id = id,
        ticketNumber = 1,
        businessDay = "2026-11-02",
        paidAtEpochMillis = 1L,
        orderType = OrderType.SUR_PLACE,
        lines = listOf(
            OrderLineSnapshot(
                productName = "Produit",
                quantity = 1,
                totalCentimes = total,
                productId = "p",
                unitPriceCentimes = total
            )
        ),
        totalCentimes = total,
        payments = listOf(PaymentRecord(PaymentMethod.ESPECES, total))
    )
}
