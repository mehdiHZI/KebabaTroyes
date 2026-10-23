package com.amhsolution.caisse.domain.history

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class DailyTicketCounterTest {
    @Test
    fun `premier ticket du jour`() {
        assertEquals(1, nextDailyTicketNumber("2026-06-16", null, null))
    }

    @Test
    fun `incremente dans la meme journee`() {
        assertEquals(3, nextDailyTicketNumber("2026-06-16", "2026-06-16", 2))
    }

    @Test
    fun `reinitialise au premier ticket d un nouveau jour`() {
        assertEquals(1, nextDailyTicketNumber("2026-06-17", "2026-06-16", 145))
    }

    @Test
    fun `refuse au dela de 999`() {
        assertThrows(IllegalArgumentException::class.java) {
            nextDailyTicketNumber("2026-06-16", "2026-06-16", 999)
        }
    }
}
