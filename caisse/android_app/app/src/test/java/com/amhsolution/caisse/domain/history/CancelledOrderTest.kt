package com.amhsolution.caisse.domain.history

import com.amhsolution.caisse.domain.model.DeliveryInfo
import com.amhsolution.caisse.domain.model.OrderLineSnapshot
import com.amhsolution.caisse.domain.model.OrderTicket
import com.amhsolution.caisse.domain.model.OrderType
import com.amhsolution.caisse.domain.model.PaymentMethod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class CancelledOrderTest {
    @Test
    fun `conserve une commande annulee dans l historique sans chiffre d affaires`() {
        val createdAt = Instant.parse("2026-07-17T12:00:00Z").toEpochMilli()
        val cancelledAt = Instant.parse("2026-07-17T12:05:00Z").toEpochMilli()
        val order = OrderTicket(
            id = "pending-42",
            ticketNumber = 42,
            orderType = OrderType.LIVRAISON,
            lines = listOf(OrderLineSnapshot("Margherita", 2, 1600)),
            totalCentimes = 1600,
            createdAtEpochMillis = createdAt,
            statusLabel = "Livraison",
            deliveryInfo = DeliveryInfo("Camille", "10 rue de Paris", 3, "0612345678")
        )

        val record = order.toCancelledHistoryRecord(cancelledAt, ZoneId.of("UTC"))

        assertEquals("cancelled-pending-42", record.id)
        assertEquals(42, record.ticketNumber)
        assertEquals("2026-07-17", record.businessDay)
        assertEquals(cancelledAt, record.paidAtEpochMillis)
        assertEquals(order.lines, record.lines)
        assertEquals(order.deliveryInfo, record.deliveryInfo)
        assertEquals(1600, record.totalCentimes)
        assertEquals(0, record.effectiveTotalCentimes)
        assertEquals(PaymentMethod.ANNULE, record.payments.single().method)
        assertTrue(record.isCancelled)
    }
}
