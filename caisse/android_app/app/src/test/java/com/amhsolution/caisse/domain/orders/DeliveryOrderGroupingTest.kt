package com.amhsolution.caisse.domain.orders

import com.amhsolution.caisse.domain.model.DeliveryInfo
import com.amhsolution.caisse.domain.model.OrderTicket
import com.amhsolution.caisse.domain.model.OrderType
import org.junit.Assert.assertEquals
import org.junit.Test

class DeliveryOrderGroupingTest {
    @Test
    fun `les livraisons sont classees par livreur puis par anciennete`() {
        val groups = listOf(
            order("driver-2-recent", driver = 2, createdAt = 30),
            order("driver-1", driver = 1, createdAt = 20),
            order("driver-2-old", driver = 2, createdAt = 10),
            order("missing", driver = null, createdAt = 5)
        ).groupedByDriver()

        assertEquals(listOf(1, 2, null), groups.map { it.driverNumber })
        assertEquals(listOf("driver-2-old", "driver-2-recent"), groups[1].orders.map { it.id })
    }

    private fun order(id: String, driver: Int?, createdAt: Long) = OrderTicket(
        id = id,
        ticketNumber = createdAt.toInt(),
        orderType = OrderType.LIVRAISON,
        lines = emptyList(),
        totalCentimes = 100,
        createdAtEpochMillis = createdAt,
        statusLabel = "Livraison",
        deliveryInfo = driver?.let { DeliveryInfo("Client", "Adresse", it) }
    )
}
