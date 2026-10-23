package com.amhsolution.caisse.data.sync

import com.amhsolution.caisse.domain.model.HistoryRecord
import com.amhsolution.caisse.domain.model.OrderLineSnapshot
import com.amhsolution.caisse.domain.model.OrderType
import com.amhsolution.caisse.domain.model.PaymentMethod
import com.amhsolution.caisse.domain.model.PaymentRecord
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TicketSyncPayloadTest {
    @Test
    fun payload_contains_ticket_id_and_register() {
        val record = HistoryRecord(
            id = "ticket-abc",
            ticketNumber = 42,
            businessDay = "2026-10-23",
            paidAtEpochMillis = 1_700_000_000_000L,
            orderType = OrderType.SUR_PLACE,
            lines = listOf(
                OrderLineSnapshot(
                    productName = "Kebaba Klassik",
                    quantity = 1,
                    totalCentimes = 800,
                    productId = "kebaba-klassik"
                )
            ),
            totalCentimes = 800,
            payments = listOf(PaymentRecord(PaymentMethod.ESPECES, 800))
        )

        val json = JSONObject(record.toSyncPayloadJson("caisse-1"))
        assertEquals("ticket-abc", json.getString("ticketId"))
        assertEquals(42, json.getInt("ticketNumber"))
        assertEquals("caisse-1", json.getString("registerId"))
        assertEquals(800, json.getInt("totalCentimes"))
        assertTrue(json.getJSONArray("lines").length() == 1)
        assertTrue(json.getJSONArray("payments").length() == 1)
    }
}
