package com.amhsolution.caisse.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MoneyFormatTest {
    @Test
    fun `parse un montant avec virgule`() {
        assertEquals(1150, parseEurosInput("11,50"))
    }

    @Test
    fun `parse un montant avec point`() {
        assertEquals(950, parseEurosInput("9.50"))
    }

    @Test
    fun `refuse un montant invalide`() {
        assertNull(parseEurosInput("abc"))
    }
}
