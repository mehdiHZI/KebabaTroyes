package com.amhsolution.caisse.data.local

import org.json.JSONObject

fun Map<String, Int>.toPriceOverridesJson(): String {
    val json = JSONObject()
    forEach { (productId, priceCentimes) ->
        json.put(productId, priceCentimes)
    }
    return json.toString()
}

fun String.toPriceOverrides(): Map<String, Int> {
    if (isBlank()) return emptyMap()

    val json = JSONObject(this)
    return buildMap {
        json.keys().forEach { key ->
            put(key, json.getInt(key))
        }
    }
}
