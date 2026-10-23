package com.amhsolution.caisse.domain.catalog

import com.amhsolution.caisse.domain.model.ProductCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductOptionCatalogTest {
    @Test
    fun sandwich_categories_support_menu_option() {
        assertEquals(300, ProductOptionCatalog.menuOptionPriceFor(ProductCategory.KLASSIK))
        assertEquals(300, ProductOptionCatalog.menuOptionPriceFor(ProductCategory.DWICH))
        assertEquals(null, ProductOptionCatalog.menuOptionPriceFor(ProductCategory.BOISSONS))
    }

    @Test
    fun kebaba_option_catalog_sauces_extra() {
        assertEquals(0, KebabaOptionCatalog.sauceExtraCentimes(2))
        assertEquals(50, KebabaOptionCatalog.sauceExtraCentimes(3))
        assertEquals(100, KebabaOptionCatalog.sauceExtraCentimes(4))
    }

    @Test
    fun customizable_products_have_recipes() {
        val klassik = SampleCatalog.products.first { it.id == "kebaba-klassik" }
        assertTrue(ProductOptionCatalog.supportsCustomization(klassik))
        assertTrue(KebabaOptionCatalog.supportsCustomization(klassik))
        assertFalse(
            KebabaOptionCatalog.supportsCustomization(
                SampleCatalog.products.first { it.id == "coca-cola-33" }
            )
        )
    }
}
