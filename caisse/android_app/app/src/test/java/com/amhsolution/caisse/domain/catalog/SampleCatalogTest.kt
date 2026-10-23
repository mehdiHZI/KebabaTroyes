package com.amhsolution.caisse.domain.catalog

import com.amhsolution.caisse.domain.model.ProductCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SampleCatalogTest {
    @Test
    fun kebaba_klassik_price_is_8_euros() {
        val klassik = SampleCatalog.products.first { it.id == "kebaba-klassik" }
        assertEquals(800, klassik.priceCentimes)
        assertEquals(ProductCategory.KLASSIK, klassik.category)
    }

    @Test
    fun soft_drinks_cost_2_euros() {
        val softs = SampleCatalog.products.filter {
            it.category == ProductCategory.BOISSONS && it.id != "cafe"
        }
        assertTrue(softs.isNotEmpty())
        assertTrue(softs.all { it.priceCentimes == 200 })
    }

    @Test
    fun all_product_ids_are_unique() {
        val ids = SampleCatalog.products.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun menu_prices_cover_sandwich_families() {
        assertEquals(1000, KebabaOptionCatalog.menuPriceCentimes(
            SampleCatalog.products.first { it.id == "kebaba-klassik" }
        ))
        assertEquals(1250, KebabaOptionCatalog.menuPriceCentimes(
            SampleCatalog.products.first { it.id == "kofte" }
        ))
        assertEquals(1400, KebabaOptionCatalog.menuPriceCentimes(
            SampleCatalog.products.first { it.id == "monster" }
        ))
    }
}
