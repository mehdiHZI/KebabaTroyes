package com.amhsolution.caisse.domain.integrity

import com.amhsolution.caisse.domain.history.toCreditNote
import com.amhsolution.caisse.domain.model.HistoryRecord
import com.amhsolution.caisse.domain.model.OrderLineSnapshot
import com.amhsolution.caisse.domain.model.OrderType
import com.amhsolution.caisse.domain.model.PaymentMethod
import com.amhsolution.caisse.domain.model.PaymentRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TicketIntegrityTest {
    @Test
    fun `hash stable pour un meme payload`() {
        val record = sampleRecord()
        val first = TicketIntegrity.hashForRecord(record, INTEGRITY_GENESIS)
        val second = TicketIntegrity.hashForRecord(record, INTEGRITY_GENESIS)
        assertEquals(first, second)
        assertEquals(64, first.length)
    }

    @Test
    fun `chainage depend du hash precedent`() {
        val record = sampleRecord()
        val h1 = TicketIntegrity.hashForRecord(record, INTEGRITY_GENESIS)
        val h2 = TicketIntegrity.hashForRecord(record, h1)
        assertNotEquals(h1, h2)
    }

    @Test
    fun `verify reconnait un ticket scelle`() {
        val draft = sampleRecord()
        val hash = TicketIntegrity.hashForRecord(draft, INTEGRITY_GENESIS)
        val sealed = draft.copy(integrityHash = hash, previousIntegrityHash = INTEGRITY_GENESIS)
        assertTrue(TicketIntegrity.verify(sealed))
        assertFalse(TicketIntegrity.verify(sealed.copy(totalCentimes = 9999)))
    }

    @Test
    fun `avoir cree une contre-operation liee sans muter l original`() {
        val original = sampleRecord()
        val credit = original.toCreditNote(ticketNumber = 2, createdAtEpochMillis = 1_800_000_000_000L)
        assertTrue(credit.isCorrection)
        assertEquals(original.id, credit.linkedTicketId)
        assertEquals(-original.totalCentimes, credit.totalCentimes)
        assertEquals(PaymentMethod.AVOIR, credit.payments.single().method)
        assertFalse(original.isCorrection)
    }

    private fun sampleRecord() = HistoryRecord(
        id = "ticket-1",
        ticketNumber = 1,
        businessDay = "2026-11-02",
        paidAtEpochMillis = 1_799_000_000_000L,
        orderType = OrderType.SUR_PLACE,
        lines = listOf(
            OrderLineSnapshot(
                productName = "Kebaba Klassik",
                quantity = 1,
                totalCentimes = 800,
                productId = "kebaba-klassik",
                unitPriceCentimes = 800
            )
        ),
        totalCentimes = 800,
        payments = listOf(PaymentRecord(PaymentMethod.ESPECES, 800)),
        registerId = "caisse-1"
    )
}
