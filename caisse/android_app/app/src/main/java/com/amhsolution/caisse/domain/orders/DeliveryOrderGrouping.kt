package com.amhsolution.caisse.domain.orders

import com.amhsolution.caisse.domain.model.OrderTicket

data class DriverOrderGroup(
    val driverNumber: Int?,
    val orders: List<OrderTicket>
)

fun List<OrderTicket>.groupedByDriver(): List<DriverOrderGroup> =
    groupBy { it.deliveryInfo?.driverNumber }
        .map { (driverNumber, orders) ->
            DriverOrderGroup(
                driverNumber = driverNumber,
                orders = orders.sortedBy { it.createdAtEpochMillis }
            )
        }
        .sortedWith(compareBy<DriverOrderGroup> { it.driverNumber == null }.thenBy { it.driverNumber })
