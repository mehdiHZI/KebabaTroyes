package com.amhsolution.caisse.domain.statistics

import com.amhsolution.caisse.domain.model.HistoryRecord
import com.amhsolution.caisse.domain.model.OrderLineSnapshot
import com.amhsolution.caisse.domain.model.OrderType
import com.amhsolution.caisse.domain.model.PaymentMethod
import com.amhsolution.caisse.domain.model.PaymentRecord
import org.junit.Assert.assertEquals
import org.junit.Test

class SessionStatisticsTest {
    @Test
    fun `calcule les totaux du jour`() {
        val records = listOf(
            record("2026-06-16", 1800, PaymentMethod.ESPECES),
            record("2026-06-16", 3200, PaymentMethod.CARTE_BANCAIRE),
            record("2026-06-16", 500, PaymentMethod.TITRE_RESTAURANT),
            record("2026-06-15", 9999, PaymentMethod.ESPECES)
        )

        val stats = records.computeStatistics(
            dateFilter = com.amhsolution.caisse.domain.history.HistoryDateFilter.Single(
                java.time.LocalDate.of(2026, 6, 16)
            )
        )

        assertEquals(3, stats.totalOrders)
        assertEquals(3, stats.activeOrders)
        assertEquals(0, stats.cancelledOrders)
        assertEquals(5500, stats.totalRevenueCentimes)
        assertEquals(1800, stats.cashRevenueCentimes)
        assertEquals(3200, stats.cardRevenueCentimes)
        assertEquals(500, stats.ticketRestoRevenueCentimes)
        assertEquals(0, stats.otherRevenueCentimes)
        assertEquals(stats.totalRevenueCentimes, stats.paymentBreakdownTotalCentimes)
    }

    @Test
    fun `affiche les tickets annules sans les compter dans le chiffre d affaires`() {
        val records = listOf(
            record("2026-06-16", 1800, PaymentMethod.ESPECES),
            record("2026-06-16", 3200, PaymentMethod.ANNULE)
        )

        val stats = records.computeStatistics(
            dateFilter = com.amhsolution.caisse.domain.history.HistoryDateFilter.Single(
                java.time.LocalDate.of(2026, 6, 16)
            )
        )

        assertEquals(2, stats.totalOrders)
        assertEquals(1, stats.activeOrders)
        assertEquals(1, stats.cancelledOrders)
        assertEquals(1800, stats.totalRevenueCentimes)
        assertEquals(1800, stats.cashRevenueCentimes)
        assertEquals(stats.totalRevenueCentimes, stats.paymentBreakdownTotalCentimes)
    }

    @Test
    fun `ventile chaque part d un paiement mixte dans les statistiques`() {
        val records = listOf(
            record(
                businessDay = "2026-06-16",
                totalCentimes = 4200,
                payments = listOf(
                    PaymentRecord(PaymentMethod.ESPECES, 1200),
                    PaymentRecord(PaymentMethod.CARTE_BANCAIRE, 2000),
                    PaymentRecord(PaymentMethod.TITRE_RESTAURANT, 700),
                    PaymentRecord(PaymentMethod.AUTRE, 300)
                )
            )
        )

        val stats = records.computeStatistics(
            dateFilter = com.amhsolution.caisse.domain.history.HistoryDateFilter.Single(
                java.time.LocalDate.of(2026, 6, 16)
            )
        )

        assertEquals(4200, stats.totalRevenueCentimes)
        assertEquals(1200, stats.cashRevenueCentimes)
        assertEquals(2000, stats.cardRevenueCentimes)
        assertEquals(700, stats.ticketRestoRevenueCentimes)
        assertEquals(300, stats.otherRevenueCentimes)
        assertEquals(stats.totalRevenueCentimes, stats.paymentBreakdownTotalCentimes)
    }

    private fun record(
        businessDay: String,
        totalCentimes: Int,
        method: PaymentMethod
    ): HistoryRecord = record(
        businessDay = businessDay,
        totalCentimes = totalCentimes,
        payments = listOf(
            PaymentRecord(
                method = method,
                amountCentimes = if (method == PaymentMethod.ANNULE) 0 else totalCentimes
            )
        )
    )

    private fun record(
        businessDay: String,
        totalCentimes: Int,
        payments: List<PaymentRecord>
    ): HistoryRecord =
        HistoryRecord(
            id = "test-$totalCentimes",
            ticketNumber = 1,
            businessDay = businessDay,
            paidAtEpochMillis = 0L,
            orderType = OrderType.SUR_PLACE,
            lines = listOf(OrderLineSnapshot("Test", 1, totalCentimes)),
            totalCentimes = totalCentimes,
            payments = payments
        )
}
