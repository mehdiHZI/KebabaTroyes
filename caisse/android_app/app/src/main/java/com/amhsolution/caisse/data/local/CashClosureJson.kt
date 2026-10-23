package com.amhsolution.caisse.data.local

import com.amhsolution.caisse.domain.model.OrderType
import com.amhsolution.caisse.domain.model.PaymentMethod
import org.json.JSONObject

fun Map<PaymentMethod, Int>.toPaymentTotalsJson(): String =
    JSONObject().apply {
        this@toPaymentTotalsJson.forEach { (method, amountCentimes) ->
            put(method.name, amountCentimes)
        }
    }.toString()

fun String.toPaymentTotals(): Map<PaymentMethod, Int> {
    if (isBlank()) return emptyMap()
    val json = JSONObject(this)
    return buildMap {
        json.keys().forEach { key ->
            val method = runCatching { PaymentMethod.valueOf(key) }.getOrNull()
            if (method != null) put(method, json.getInt(key))
        }
    }
}

fun Map<PaymentMethod, Int>.toPaymentCountsJson(): String =
    JSONObject().apply {
        this@toPaymentCountsJson.forEach { (method, count) ->
            put(method.name, count)
        }
    }.toString()

fun String.toPaymentCounts(): Map<PaymentMethod, Int> {
    if (isBlank()) return emptyMap()
    val json = JSONObject(this)
    return buildMap {
        json.keys().forEach { key ->
            val method = runCatching { PaymentMethod.valueOf(key) }.getOrNull()
            if (method != null) put(method, json.getInt(key))
        }
    }
}

fun Map<OrderType, Int>.toOrderTypeTotalsJson(): String =
    JSONObject().apply {
        this@toOrderTypeTotalsJson.forEach { (type, amountCentimes) ->
            put(type.name, amountCentimes)
        }
    }.toString()

fun String.toOrderTypeTotals(): Map<OrderType, Int> {
    if (isBlank()) return emptyMap()
    val json = JSONObject(this)
    return buildMap {
        json.keys().forEach { key ->
            val type = runCatching { OrderType.valueOf(key) }.getOrNull()
            if (type != null) put(type, json.getInt(key))
        }
    }
}
