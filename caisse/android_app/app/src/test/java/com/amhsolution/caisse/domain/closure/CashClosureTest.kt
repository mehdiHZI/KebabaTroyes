package com.amhsolution.caisse.domain.closure

import com.amhsolution.caisse.domain.model.HistoryRecord
import com.amhsolution.caisse.domain.model.OrderLineSnapshot
import com.amhsolution.caisse.domain.model.OrderType
import com.amhsolution.caisse.domain.model.PaymentMethod
import com.amhsolution.caisse.domain.model.PaymentRecord
import org.junit.Assert.assertEquals
import org.junit.Test

class CashClosureTest {
    @Test
    fun `construit une cloture journaliere avec totaux par paiement et type de commande`() {
        val records = listOf(
            record(
                ticketNumber = 2,
                businessDay = "2026-06-24",
                orderType = OrderType.SUR_PLACE,
                totalCentimes = 1500,
                payments = listOf(PaymentRecord(PaymentMethod.ESPECES, 1000), PaymentRecord(PaymentMethod.CARTE_BANCAIRE, 500))
            ),
            record(
                ticketNumber = 1,
                businessDay = "2026-06-24",
                orderType = OrderType.LIVRAISON,
                totalCentimes = 2400,
                payments = listOf(PaymentRecord(PaymentMethod.TITRE_RESTAURANT, 2400))
            ),
            record(
                ticketNumber = 3,
                businessDay = "2026-06-23",
                orderType = OrderType.A_EMPORTER,
                totalCentimes = 9999,
                payments = listOf(PaymentRecord(PaymentMethod.ESPECES, 9999))
            )
        )

        val closure = records.buildDailyCashClosure(
            businessDay = "2026-06-24",
            closedAtEpochMillis = 1_782_300_000_000
        )

        assertEquals(CashClosureType.DAILY_Z, closure.type)
        assertEquals(2, closure.ticketCount)
        assertEquals(1, closure.firstTicketNumber)
        assertEquals(2, closure.lastTicketNumber)
        assertEquals(3900, closure.totalRevenueCentimes)
        assertEquals(2, closure.itemCount)
        assertEquals(1950, closure.averageBasketCentimes)
        assertEquals(1000, closure.paymentTotalsCentimes[PaymentMethod.ESPECES])
        assertEquals(500, closure.paymentTotalsCentimes[PaymentMethod.CARTE_BANCAIRE])
        assertEquals(2400, closure.paymentTotalsCentimes[PaymentMethod.TITRE_RESTAURANT])
        assertEquals(1, closure.paymentCounts[PaymentMethod.ESPECES])
        assertEquals(1, closure.paymentCounts[PaymentMethod.CARTE_BANCAIRE])
        assertEquals(1, closure.paymentCounts[PaymentMethod.TITRE_RESTAURANT])
        assertEquals(1500, closure.orderTypeTotalsCentimes[OrderType.SUR_PLACE])
        assertEquals(2400, closure.orderTypeTotalsCentimes[OrderType.LIVRAISON])
        assertEquals(false, closure.isFinal)
        assertEquals(false, closure.locksTickets)
        assertEquals("Cloture manuelle (provisoire)", closure.statusLabel)
    }

    @Test
    fun `une cloture journaliere finale verrouille les tickets`() {
        val closure = listOf(
            record(
                ticketNumber = 1,
                businessDay = "2026-06-24",
                orderType = OrderType.SUR_PLACE,
                totalCentimes = 1000,
                payments = listOf(PaymentRecord(PaymentMethod.ESPECES, 1000))
            )
        ).buildDailyCashClosure(
            businessDay = "2026-06-24",
            closedAtEpochMillis = 1_782_300_000_000,
            isFinal = true
        )

        assertEquals(true, closure.isFinal)
        assertEquals(true, closure.locksTickets)
        assertEquals("Cloture automatique (definitive)", closure.statusLabel)
    }

    @Test
    fun `exclut les montants annules et compte les commandes annulees`() {
        val records = listOf(
            record(
                ticketNumber = 1,
                businessDay = "2026-06-24",
                orderType = OrderType.SUR_PLACE,
                totalCentimes = 1500,
                payments = listOf(PaymentRecord(PaymentMethod.ESPECES, 1500))
            ),
            record(
                ticketNumber = 2,
                businessDay = "2026-06-24",
                orderType = OrderType.SUR_PLACE,
                totalCentimes = 9999,
                payments = listOf(PaymentRecord(PaymentMethod.ANNULE, 0))
            )
        )

        val closure = records.buildDailyCashClosure(
            businessDay = "2026-06-24",
            closedAtEpochMillis = 1_782_300_000_000
        )

        assertEquals(1, closure.ticketCount)
        assertEquals(2, closure.lastTicketNumber)
        assertEquals(1500, closure.totalRevenueCentimes)
        assertEquals(1500, closure.paymentTotalsCentimes[PaymentMethod.ESPECES])
        assertEquals(1, closure.cancelledOrderCount)
    }

    @Test
    fun `cloture une journee contenant uniquement une commande annulee`() {
        val closure = listOf(
            record(
                ticketNumber = 7,
                businessDay = "2026-06-24",
                orderType = OrderType.A_EMPORTER,
                totalCentimes = 9999,
                payments = listOf(PaymentRecord(PaymentMethod.ANNULE, 0))
            )
        ).buildDailyCashClosure(
            businessDay = "2026-06-24",
            closedAtEpochMillis = 1_782_300_000_000
        )

        assertEquals(0, closure.ticketCount)
        assertEquals(7, closure.firstTicketNumber)
        assertEquals(7, closure.lastTicketNumber)
        assertEquals(0, closure.totalRevenueCentimes)
        assertEquals(0, closure.averageBasketCentimes)
        assertEquals(1, closure.cancelledOrderCount)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `refuse de cloturer une journee sans ticket`() {
        emptyList<HistoryRecord>().buildDailyCashClosure(
            businessDay = "2026-06-24",
            closedAtEpochMillis = 1_782_300_000_000
        )
    }

    @Test
    fun `construit une cloture mensuelle depuis les clotures journalieres`() {
        val dailyClosures = listOf(
            dailyClosure(
                businessDay = "2026-06-01",
                ticketCount = 2,
                totalCentimes = 2500,
                itemCount = 3,
                paymentTotals = mapOf(PaymentMethod.ESPECES to 2500),
                paymentCounts = mapOf(
                    PaymentMethod.ESPECES to 2,
                    PaymentMethod.ANNULE to 1
                ),
                orderTypeTotals = mapOf(OrderType.SUR_PLACE to 2500)
            ),
            dailyClosure(
                businessDay = "2026-06-02",
                ticketCount = 1,
                totalCentimes = 1500,
                itemCount = 2,
                paymentTotals = mapOf(PaymentMethod.CARTE_BANCAIRE to 1500),
                paymentCounts = mapOf(
                    PaymentMethod.CARTE_BANCAIRE to 1,
                    PaymentMethod.ANNULE to 2
                ),
                orderTypeTotals = mapOf(OrderType.LIVRAISON to 1500)
            ),
            dailyClosure(
                businessDay = "2026-07-01",
                ticketCount = 1,
                totalCentimes = 9999,
                itemCount = 1
            )
        )

        val closure = dailyClosures.buildMonthlyCashClosure(
            periodMonth = "2026-06",
            closedAtEpochMillis = 1_782_900_000_000
        )

        assertEquals(CashClosureType.MONTHLY_Z, closure.type)
        assertEquals("2026-06", closure.businessDay)
        assertEquals(3, closure.ticketCount)
        assertEquals(4000, closure.totalRevenueCentimes)
        assertEquals(5, closure.itemCount)
        assertEquals(1333, closure.averageBasketCentimes)
        assertEquals(2500, closure.paymentTotalsCentimes[PaymentMethod.ESPECES])
        assertEquals(1500, closure.paymentTotalsCentimes[PaymentMethod.CARTE_BANCAIRE])
        assertEquals(2, closure.paymentCounts[PaymentMethod.ESPECES])
        assertEquals(1, closure.paymentCounts[PaymentMethod.CARTE_BANCAIRE])
        assertEquals(3, closure.cancelledOrderCount)
        assertEquals(2500, closure.orderTypeTotalsCentimes[OrderType.SUR_PLACE])
        assertEquals(1500, closure.orderTypeTotalsCentimes[OrderType.LIVRAISON])
    }

    @Test
    fun `construit une cloture annuelle depuis les clotures journalieres`() {
        val dailyClosures = listOf(
            dailyClosure(
                businessDay = "2026-01-04",
                ticketCount = 1,
                totalCentimes = 1200,
                itemCount = 1,
                paymentTotals = mapOf(PaymentMethod.ESPECES to 1200),
                paymentCounts = mapOf(PaymentMethod.ESPECES to 1)
            ),
            dailyClosure(
                businessDay = "2026-12-30",
                ticketCount = 2,
                totalCentimes = 3800,
                itemCount = 4,
                paymentTotals = mapOf(PaymentMethod.TITRE_RESTAURANT to 3800),
                paymentCounts = mapOf(PaymentMethod.TITRE_RESTAURANT to 2)
            ),
            dailyClosure(
                businessDay = "2025-12-30",
                ticketCount = 1,
                totalCentimes = 9999,
                itemCount = 1
            )
        )

        val closure = dailyClosures.buildYearlyCashClosure(
            periodYear = "2026",
            closedAtEpochMillis = 1_799_000_000_000
        )

        assertEquals(CashClosureType.YEARLY_Z, closure.type)
        assertEquals("2026", closure.businessDay)
        assertEquals(3, closure.ticketCount)
        assertEquals(5000, closure.totalRevenueCentimes)
        assertEquals(5, closure.itemCount)
        assertEquals(1666, closure.averageBasketCentimes)
        assertEquals(1200, closure.paymentTotalsCentimes[PaymentMethod.ESPECES])
        assertEquals(3800, closure.paymentTotalsCentimes[PaymentMethod.TITRE_RESTAURANT])
        assertEquals(1, closure.paymentCounts[PaymentMethod.ESPECES])
        assertEquals(2, closure.paymentCounts[PaymentMethod.TITRE_RESTAURANT])
    }

    private fun record(
        ticketNumber: Int,
        businessDay: String,
        orderType: OrderType,
        totalCentimes: Int,
        payments: List<PaymentRecord> = listOf(PaymentRecord(PaymentMethod.ESPECES, totalCentimes))
    ): HistoryRecord =
        HistoryRecord(
            id = "history-$ticketNumber-$totalCentimes",
            ticketNumber = ticketNumber,
            businessDay = businessDay,
            paidAtEpochMillis = ticketNumber.toLong(),
            orderType = orderType,
            lines = listOf(OrderLineSnapshot("Produit test", 1, totalCentimes)),
            totalCentimes = totalCentimes,
            payments = payments
        )

    private fun dailyClosure(
        businessDay: String,
        ticketCount: Int,
        totalCentimes: Int,
        itemCount: Int,
        paymentTotals: Map<PaymentMethod, Int> = emptyMap(),
        paymentCounts: Map<PaymentMethod, Int> = emptyMap(),
        orderTypeTotals: Map<OrderType, Int> = emptyMap()
    ): CashClosure =
        CashClosure(
            id = "daily-z-$businessDay",
            type = CashClosureType.DAILY_Z,
            businessDay = businessDay,
            closedAtEpochMillis = 1_782_300_000_000,
            ticketCount = ticketCount,
            firstTicketNumber = 1,
            lastTicketNumber = ticketCount,
            totalRevenueCentimes = totalCentimes,
            itemCount = itemCount,
            averageBasketCentimes = totalCentimes / ticketCount,
            paymentTotalsCentimes = paymentTotals,
            paymentCounts = paymentCounts,
            orderTypeTotalsCentimes = orderTypeTotals,
            isFinal = false
        )
}
