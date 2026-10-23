package com.amhsolution.caisse.domain.catalog

import com.amhsolution.caisse.domain.history.businessDayFromEpochMillis
import com.amhsolution.caisse.domain.model.HistoryRecord
import com.amhsolution.caisse.domain.model.OrderLineSnapshot
import com.amhsolution.caisse.domain.model.OrderType
import com.amhsolution.caisse.domain.model.PaymentMethod
import com.amhsolution.caisse.domain.model.PaymentRecord
import com.amhsolution.caisse.domain.model.ProductCustomization
import com.amhsolution.caisse.domain.model.SupplementSelection
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

object SampleHistoryRecords {
    private val zoneId = ZoneId.systemDefault()

    private fun epochAt(day: Int, hour: Int, minute: Int): Long =
        LocalDate.of(2026, 10, day)
            .atTime(LocalTime.of(hour, minute))
            .atZone(zoneId)
            .toInstant()
            .toEpochMilli()

    private val menuOptionCustomization = ProductCustomization(
        addedSupplements = listOf(
            SupplementSelection(
                ProductOptionCatalog.MENU_OPTION_ID,
                ProductOptionCatalog.MENU_OPTION_LABEL,
                200
            )
        )
    )

    val records: List<HistoryRecord> = listOf(
        HistoryRecord(
            id = "history-1",
            ticketNumber = 1,
            businessDay = businessDayFromEpochMillis(epochAt(20, 12, 8), zoneId),
            paidAtEpochMillis = epochAt(20, 12, 8),
            orderType = OrderType.SUR_PLACE,
            lines = listOf(
                OrderLineSnapshot(
                    productName = "Kebaba Klassik",
                    quantity = 1,
                    totalCentimes = 1000,
                    productId = "kebaba-klassik",
                    unitPriceCentimes = 1000,
                    productCustomization = menuOptionCustomization
                )
            ),
            totalCentimes = 1000,
            payments = listOf(PaymentRecord(PaymentMethod.ESPECES, 1000, 2000, 1000))
        ),
        HistoryRecord(
            id = "history-2",
            ticketNumber = 2,
            businessDay = businessDayFromEpochMillis(epochAt(20, 12, 14), zoneId),
            paidAtEpochMillis = epochAt(20, 12, 14),
            orderType = OrderType.A_EMPORTER,
            lines = listOf(
                OrderLineSnapshot("Bowl Klassik", 1, 1200),
                OrderLineSnapshot("Tiramisu", 1, 400)
            ),
            totalCentimes = 1600,
            payments = listOf(
                PaymentRecord(PaymentMethod.ESPECES, 1000),
                PaymentRecord(PaymentMethod.CARTE_BANCAIRE, 600)
            )
        ),
        HistoryRecord(
            id = "history-3",
            ticketNumber = 3,
            businessDay = businessDayFromEpochMillis(epochAt(20, 12, 37), zoneId),
            paidAtEpochMillis = epochAt(20, 12, 37),
            orderType = OrderType.A_EMPORTER,
            lines = listOf(
                OrderLineSnapshot("Monster", 1, 1100),
                OrderLineSnapshot("Coca Cola 33 cl", 1, 200)
            ),
            totalCentimes = 1300,
            payments = listOf(PaymentRecord(PaymentMethod.CARTE_BANCAIRE, 1300))
        )
    )
}
