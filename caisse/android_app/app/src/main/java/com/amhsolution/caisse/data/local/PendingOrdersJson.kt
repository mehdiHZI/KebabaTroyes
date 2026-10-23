package com.amhsolution.caisse.data.local

import com.amhsolution.caisse.domain.model.DeliveryInfo
import com.amhsolution.caisse.domain.model.OrderTicket
import com.amhsolution.caisse.domain.model.OrderType
import org.json.JSONArray
import org.json.JSONObject

fun List<OrderTicket>.toJsonPendingOrders(): String {
    val array = JSONArray()
    forEach { order ->
        val objectJson = JSONObject()
            .put("id", order.id)
            .put("ticketNumber", order.ticketNumber)
            .put("orderType", order.orderType.name)
            .put("lines", JSONArray(order.lines.toJsonLines()))
            .put("totalCentimes", order.totalCentimes)
            .put("createdAtEpochMillis", order.createdAtEpochMillis)
            .put("statusLabel", order.statusLabel)
        order.deliveryInfo?.let { objectJson.put("deliveryInfo", JSONObject(it.toJson())) }
        array.put(objectJson)
    }
    return array.toString()
}

fun String.toPendingOrders(): List<OrderTicket> {
    if (isBlank()) return emptyList()
    val array = JSONArray(this)
    return buildList {
        for (index in 0 until array.length()) {
            val objectJson = array.getJSONObject(index)
            add(
                OrderTicket(
                    id = objectJson.getString("id"),
                    ticketNumber = objectJson.getInt("ticketNumber"),
                    orderType = OrderType.valueOf(objectJson.getString("orderType")),
                    lines = objectJson.getJSONArray("lines").toString().toOrderLineSnapshots(),
                    totalCentimes = objectJson.getInt("totalCentimes"),
                    createdAtEpochMillis = objectJson.getLong("createdAtEpochMillis"),
                    statusLabel = objectJson.getString("statusLabel"),
                    deliveryInfo = objectJson.optJSONObject("deliveryInfo")?.toDeliveryInfo()
                )
            )
        }
    }
}

private fun JSONObject.toDeliveryInfo(): DeliveryInfo =
    DeliveryInfo(
        customerName = getString("customerName"),
        address = getString("address"),
        driverNumber = getInt("driverNumber"),
        phoneNumber = optString("phoneNumber")
    )
