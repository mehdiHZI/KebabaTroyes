package com.amhsolution.caisse.domain.history

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class HistoryDateFilterTest {
    @Test
    fun `filtre jour unique`() {
        val filter = HistoryDateFilter.Single(LocalDate.of(2026, 6, 3))
        assertTrue(filter.matchesBusinessDay("2026-06-03"))
        assertFalse(filter.matchesBusinessDay("2026-06-04"))
    }

    @Test
    fun `filtre periode`() {
        val filter = HistoryDateFilter.Range(
            start = LocalDate.of(2026, 6, 3),
            end = LocalDate.of(2026, 6, 10)
        )
        assertTrue(filter.matchesBusinessDay("2026-06-03"))
        assertTrue(filter.matchesBusinessDay("2026-06-07"))
        assertTrue(filter.matchesBusinessDay("2026-06-10"))
        assertFalse(filter.matchesBusinessDay("2026-06-11"))
    }
}
