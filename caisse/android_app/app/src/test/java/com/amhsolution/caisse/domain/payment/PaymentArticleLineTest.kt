package com.amhsolution.caisse.domain.payment

import com.amhsolution.caisse.domain.catalog.SampleCatalog
import com.amhsolution.caisse.domain.model.CartLine
import com.amhsolution.caisse.domain.model.OrderLineSnapshot
import org.junit.Assert.assertEquals
import org.junit.Test

class PaymentArticleLineTest {
    @Test
    fun `affiche intitule et prix sans composition`() {
        val product = SampleCatalog.products.first { it.id == "kebaba-klassik" }
        val line = CartLine(product = product, quantity = 2, unitPriceCentimes = 800)

        val article = line.toPaymentArticleLine()

        assertEquals("2x Kebaba Klassik", article.label)
        assertEquals(1600, article.totalCentimes)
    }

    @Test
    fun `ligne snapshot simple`() {
        val snapshot = OrderLineSnapshot(
            productName = "Menu Étudiant",
            quantity = 1,
            totalCentimes = 800,
            productId = "menu-etudiant"
        )

        val article = snapshot.toPaymentArticleLine()

        assertEquals("1x Menu Étudiant", article.label)
        assertEquals(800, article.totalCentimes)
    }
}
