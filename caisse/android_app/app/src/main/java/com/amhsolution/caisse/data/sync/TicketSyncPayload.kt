package com.amhsolution.caisse.data.sync

import com.amhsolution.caisse.domain.model.HistoryRecord
import org.json.JSONArray
import org.json.JSONObject

fun HistoryRecord.toSyncPayloadJson(registerId: String): String {
    val lines = JSONArray()
    this.lines.forEach { line ->
        lines.put(
            JSONObject()
                .put("productId", line.productId)
                .put("productName", line.productName)
                .put("quantity", line.quantity)
                .put("unitPriceCentimes", line.unitPriceCentimes)
                .put("totalCentimes", line.totalCentimes)
                .put("lineId", line.lineId)
                .put(
                    "detail",
                    listOfNotNull(
                        line.productCustomization?.summary,
                        line.pizzaCustomization?.summary,
                        line.studentMenuCustomization?.summary,
                        line.specialOfferCustomization?.summary
                    ).filter { it.isNotBlank() }.joinToString(" · ").ifBlank { null }
                )
        )
    }

    val payments = JSONArray()
    this.payments.forEach { payment ->
        payments.put(
            JSONObject()
                .put("method", payment.method.name)
                .put("amountCentimes", payment.amountCentimes)
                .put("receivedCentimes", payment.receivedCentimes)
                .put("changeCentimes", payment.changeCentimes)
        )
    }

    val delivery = deliveryInfo?.let { info ->
        JSONObject()
            .put("customerName", info.customerName)
            .put("address", info.address)
            .put("phoneNumber", info.phoneNumber)
            .put("driverNumber", info.driverNumber)
    }

    return JSONObject()
        .put("ticketId", id)
        .put("ticketNumber", ticketNumber)
        .put("businessDay", businessDay)
        .put("paidAtEpochMillis", paidAtEpochMillis)
        .put("orderType", orderType.name)
        .put("totalCentimes", totalCentimes)
        .put("registerId", registerId)
        .put("lines", lines)
        .put("payments", payments)
        .put("delivery", delivery)
        .toString()
}
