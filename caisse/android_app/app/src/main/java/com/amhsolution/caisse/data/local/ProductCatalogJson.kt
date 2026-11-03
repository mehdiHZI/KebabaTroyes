package com.amhsolution.caisse.data.local

import com.amhsolution.caisse.domain.model.Product
import com.amhsolution.caisse.domain.model.ProductCategory
import org.json.JSONArray
import org.json.JSONObject

fun List<Product>.toCatalogProductsJson(): String {
    val array = JSONArray()
    forEach { product -> array.put(product.toJsonObject()) }
    return array.toString()
}

fun String.toCatalogProducts(): List<Product> {
    if (isBlank()) return emptyList()
    val array = JSONArray(this)
    return buildList {
        for (i in 0 until array.length()) {
            val obj = array.optJSONObject(i) ?: continue
            obj.toProduct()?.let { add(it) }
        }
    }
}

fun Product.toJsonObject(): JSONObject =
    JSONObject()
        .put("id", id)
        .put("name", name)
        .put("category", category.name)
        .put("priceCentimes", priceCentimes)
        .put("menuPriceCentimes", menuPriceCentimes ?: JSONObject.NULL)
        .put("hasPain", hasPain)
        .put("hasFrites", hasFrites)
        .put("hasCrudites", hasCrudites)
        .put("hasSupplements", hasSupplements)
        .put("hasSauces", hasSauces)
        .put("hasTakosMeats", hasTakosMeats)
        .put("supportsDrinkPlus", supportsDrinkPlus)
        .put("takosMeatCount", takosMeatCount)
        .put("enabled", enabled)
        .put("updatedAtEpochMillis", updatedAtEpochMillis)

fun JSONObject.toProduct(): Product? {
    val id = optString("id").takeIf { it.isNotBlank() } ?: return null
    val name = optString("name").takeIf { it.isNotBlank() } ?: return null
    val categoryName = optString("category").takeIf { it.isNotBlank() } ?: return null
    val category = runCatching { ProductCategory.valueOf(categoryName) }.getOrNull()
        ?: return null
    val price = if (has("priceCentimes")) getInt("priceCentimes") else return null
    val menu = if (isNull("menuPriceCentimes") || !has("menuPriceCentimes")) {
        null
    } else {
        getInt("menuPriceCentimes")
    }
    return Product(
        id = id,
        name = name,
        category = category,
        priceCentimes = price,
        menuPriceCentimes = menu,
        hasPain = optBoolean("hasPain", false),
        hasFrites = optBoolean("hasFrites", false),
        hasCrudites = optBoolean("hasCrudites", false),
        hasSupplements = optBoolean("hasSupplements", false),
        hasSauces = optBoolean("hasSauces", false),
        hasTakosMeats = optBoolean("hasTakosMeats", false),
        supportsDrinkPlus = optBoolean("supportsDrinkPlus", false),
        takosMeatCount = optInt("takosMeatCount", 0),
        enabled = optBoolean("enabled", true),
        updatedAtEpochMillis = optLong("updatedAtEpochMillis", 0L)
    )
}
