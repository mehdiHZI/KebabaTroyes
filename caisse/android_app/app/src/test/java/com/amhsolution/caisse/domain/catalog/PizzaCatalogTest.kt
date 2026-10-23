package com.amhsolution.caisse.domain.catalog

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pizza Milano retirée — catalogue vide côté Kebaba. */
class PizzaCatalogTest {
    @Test
    fun pizza_catalog_is_empty_for_kebaba() {
        assertTrue(PizzaCatalog.products.isEmpty())
        assertTrue(PizzaCatalog.recipes.isEmpty())
        assertTrue(PizzaCatalog.ingredients.isEmpty())
        assertEquals(0, PizzaCatalog.defaultSupplementPrices.size)
    }
}
