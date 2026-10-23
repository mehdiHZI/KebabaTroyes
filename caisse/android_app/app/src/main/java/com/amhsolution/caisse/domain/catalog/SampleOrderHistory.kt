package com.amhsolution.caisse.domain.catalog

import com.amhsolution.caisse.domain.model.OrderLineSnapshot
import com.amhsolution.caisse.domain.model.OrderTicket
import com.amhsolution.caisse.domain.model.OrderType
import com.amhsolution.caisse.domain.model.ProductCustomization
import com.amhsolution.caisse.domain.model.SupplementSelection

object SampleOrderHistory {
    private val menuOptionCustomization = ProductCustomization(
        addedSupplements = listOf(
            SupplementSelection(
                ProductOptionCatalog.MENU_OPTION_ID,
                ProductOptionCatalog.MENU_OPTION_LABEL,
                200
            )
        )
    )

    val orders: List<OrderTicket> = listOf(
        OrderTicket(
            id = "pending-1",
            ticketNumber = 12,
            orderType = OrderType.SUR_PLACE,
            lines = listOf(
                OrderLineSnapshot(
                    productName = "Kebaba Klassik",
                    quantity = 2,
                    totalCentimes = 2000,
                    productId = "kebaba-klassik",
                    unitPriceCentimes = 1000,
                    productCustomization = menuOptionCustomization
                )
            ),
            totalCentimes = 2000,
            createdAtEpochMillis = System.currentTimeMillis(),
            statusLabel = "A regler"
        ),
        OrderTicket(
            id = "pending-2",
            ticketNumber = 13,
            orderType = OrderType.A_EMPORTER,
            lines = listOf(
                OrderLineSnapshot("Monster", 1, 1100),
                OrderLineSnapshot("Tiramisu", 1, 400)
            ),
            totalCentimes = 1500,
            createdAtEpochMillis = System.currentTimeMillis(),
            statusLabel = "A emporter",
            deliveryInfo = null
        )
    )
}
