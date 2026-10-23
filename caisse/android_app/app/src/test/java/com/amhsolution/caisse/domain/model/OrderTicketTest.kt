package com.amhsolution.caisse.domain.model

import com.amhsolution.caisse.domain.catalog.SampleCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OrderTicketTest {
    @Test
    fun `une ancienne ligne sans nouveaux champs reste lisible`() {
        val oldLine = OrderLineSnapshot(
            productName = "Kebaba Klassik",
            quantity = 2,
            totalCentimes = 1600
        )
        val ticket = ticket(lines = listOf(oldLine), totalCentimes = 1600)

        val restored = ticket.toCartLinesFromCatalog(SampleCatalog.products).single()

        assertEquals("Kebaba Klassik", restored.product.name)
        assertEquals(2, restored.quantity)
        assertEquals(800, restored.unitPriceCentimes)
        assertEquals(1600, restored.lineTotalCentimes)
        assertNull(restored.pizzaCustomization)
    }

    @Test
    fun `une personnalisation produit conserve retraits et supplements`() {
        val product = SampleCatalog.products.first { it.id == "kebaba-klassik" }
        val customization = ProductCustomization(
            removedIngredients = listOf(IngredientSelection("tomates", "Tomate")),
            addedSupplements = listOf(SupplementSelection("feta", "Feta", 150))
        )
        val original = CartLine(
            product = product,
            quantity = 1,
            lineId = "klassik-test",
            unitPriceCentimes = 950,
            productCustomization = customization
        )

        val restored = ticket(
            lines = listOf(original).toOrderLineSnapshots(),
            totalCentimes = 950
        ).toCartLinesFromCatalog(SampleCatalog.products).single()

        assertEquals(customization, restored.productCustomization)
        assertEquals(original.lineId, restored.lineId)
        assertEquals(950, restored.unitPriceCentimes)
    }

    private fun ticket(
        lines: List<OrderLineSnapshot>,
        totalCentimes: Int
    ) = OrderTicket(
        id = "ticket-test",
        ticketNumber = 1,
        orderType = OrderType.SUR_PLACE,
        lines = lines,
        totalCentimes = totalCentimes,
        createdAtEpochMillis = 0L,
        statusLabel = "Paye"
    )
}
