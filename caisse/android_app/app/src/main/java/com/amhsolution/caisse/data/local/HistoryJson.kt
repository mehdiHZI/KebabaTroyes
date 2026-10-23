package com.amhsolution.caisse.data.local

import com.amhsolution.caisse.domain.model.DeliveryInfo
import com.amhsolution.caisse.domain.model.OrderLineSnapshot
import com.amhsolution.caisse.domain.model.PaymentMethod
import com.amhsolution.caisse.domain.model.PaymentRecord
import com.amhsolution.caisse.domain.catalog.PizzaBase
import com.amhsolution.caisse.domain.catalog.PizzaCatalog
import com.amhsolution.caisse.domain.catalog.PizzaSize
import com.amhsolution.caisse.domain.model.PizzaCustomization
import com.amhsolution.caisse.domain.model.PizzaIngredientSelection
import com.amhsolution.caisse.domain.model.PizzaSupplementSelection
import com.amhsolution.caisse.domain.model.ProductCustomization
import com.amhsolution.caisse.domain.model.IngredientSelection
import com.amhsolution.caisse.domain.model.SpecialOfferCustomization
import com.amhsolution.caisse.domain.model.SpecialOfferPizzaSelection
import com.amhsolution.caisse.domain.model.StudentMenuChoiceKind
import com.amhsolution.caisse.domain.model.StudentMenuCustomization
import org.json.JSONArray
import org.json.JSONObject

fun List<OrderLineSnapshot>.toJsonLines(): String {
    val array = JSONArray()
    forEach { line ->
        val objectJson = JSONObject()
                .put("productName", line.productName)
                .put("quantity", line.quantity)
                .put("totalCentimes", line.totalCentimes)
        line.productId?.let { objectJson.put("productId", it) }
        line.lineId?.let { objectJson.put("lineId", it) }
        line.unitPriceCentimes?.let { objectJson.put("unitPriceCentimes", it) }
        line.pizzaCustomization?.let { objectJson.put("pizzaCustomization", it.toJsonObject()) }
        line.productCustomization?.let { objectJson.put("productCustomization", it.toJsonObject()) }
        line.specialOfferCustomization?.let {
            objectJson.put("specialOfferCustomization", it.toJsonObject())
        }
        line.studentMenuCustomization?.let {
            objectJson.put("studentMenuCustomization", it.toJsonObject())
        }
        array.put(objectJson)
    }
    return array.toString()
}

fun String.toOrderLineSnapshots(): List<OrderLineSnapshot> {
    val array = JSONArray(this)
    return buildList {
        for (index in 0 until array.length()) {
            val objectJson = array.getJSONObject(index)
            add(
                OrderLineSnapshot(
                    productName = objectJson.getString("productName"),
                    quantity = objectJson.getInt("quantity"),
                    totalCentimes = objectJson.getInt("totalCentimes"),
                    productId = objectJson.optString("productId").takeIf { it.isNotBlank() },
                    lineId = objectJson.optString("lineId").takeIf { it.isNotBlank() },
                    unitPriceCentimes = objectJson.optInt("unitPriceCentimes")
                        .takeIf { objectJson.has("unitPriceCentimes") },
                    pizzaCustomization = objectJson.optJSONObject("pizzaCustomization")
                        ?.toPizzaCustomizationOrNull(),
                    productCustomization = objectJson.optJSONObject("productCustomization")
                        ?.toProductCustomization(),
                    specialOfferCustomization = objectJson.optJSONObject("specialOfferCustomization")
                        ?.toSpecialOfferCustomizationOrNull(),
                    studentMenuCustomization = objectJson.optJSONObject("studentMenuCustomization")
                        ?.toStudentMenuCustomizationOrNull()
                )
            )
        }
    }
}

private fun PizzaCustomization.toJsonObject(): JSONObject =
    JSONObject()
        .put("size", size.name)
        .put("base", base.name)
        .put("removedIngredients", removedIngredients.toIngredientJsonArray())
        .put("addedSupplements", addedSupplements.toSupplementJsonArray())

private fun ProductCustomization.toJsonObject(): JSONObject =
    JSONObject()
        .put("removedIngredients", removedIngredients.toIngredientJsonArray())
        .put("addedSupplements", addedSupplements.toSupplementJsonArray())
        .also { json ->
            includedSide?.let { side ->
                json.put(
                    "includedSide",
                    JSONObject().put("id", side.id).put("label", side.label)
                )
            }
            includedDrink?.let { drink ->
                json.put(
                    "includedDrink",
                    JSONObject().put("id", drink.id).put("label", drink.label)
                )
            }
        }

private fun SpecialOfferCustomization.toJsonObject(): JSONObject =
    JSONObject()
        .put(
            "pizzas",
            JSONArray().apply {
                pizzas.forEach { pizza ->
                    put(
                        JSONObject()
                            .put("productId", pizza.productId)
                            .put("productName", pizza.productName)
                            .put("customization", pizza.customization.toJsonObject())
                    )
                }
            }
        )
        .put("pizzaNames", JSONArray().apply { pizzaNames.forEach(::put) })
        .put("drinkLabel", drinkLabel)

private fun StudentMenuCustomization.toJsonObject(): JSONObject =
    JSONObject()
        .put("choiceKind", choiceKind.name)
        .put("itemProductId", itemProductId)
        .put("itemProductName", itemProductName)
        .put("drinkId", drinkId)
        .put("drinkLabel", drinkLabel)
        .also { json ->
            pizzaCustomization?.let { json.put("pizzaCustomization", it.toJsonObject()) }
            productCustomization?.let { json.put("productCustomization", it.toJsonObject()) }
        }

private fun JSONObject.toStudentMenuCustomizationOrNull(): StudentMenuCustomization? {
    val choiceKind = runCatching {
        StudentMenuChoiceKind.valueOf(getString("choiceKind"))
    }.getOrNull() ?: return null
    val itemProductId = optString("itemProductId").takeIf { it.isNotBlank() } ?: return null
    val itemProductName = optString("itemProductName").takeIf { it.isNotBlank() } ?: return null
    val drinkId = optString("drinkId").takeIf { it.isNotBlank() } ?: return null
    val drinkLabel = optString("drinkLabel").takeIf { it.isNotBlank() } ?: return null
    val pizzaCustomization = optJSONObject("pizzaCustomization")?.toPizzaCustomizationOrNull()
    val productCustomization = optJSONObject("productCustomization")?.toProductCustomization()
    if (choiceKind == StudentMenuChoiceKind.PIZZA && pizzaCustomization == null) return null
    return StudentMenuCustomization(
        choiceKind = choiceKind,
        itemProductId = itemProductId,
        itemProductName = itemProductName,
        drinkId = drinkId,
        drinkLabel = drinkLabel,
        pizzaCustomization = pizzaCustomization,
        productCustomization = productCustomization
    )
}

private fun List<PizzaIngredientSelection>.toIngredientJsonArray(): JSONArray =
    JSONArray().apply {
        this@toIngredientJsonArray.forEach { ingredient ->
            put(JSONObject().put("id", ingredient.id).put("label", ingredient.label))
        }
    }

private fun List<PizzaSupplementSelection>.toSupplementJsonArray(): JSONArray =
    JSONArray().apply {
        this@toSupplementJsonArray.forEach { supplement ->
            put(
                JSONObject()
                    .put("id", supplement.id)
                    .put("label", supplement.label)
                    .put("priceCentimes", supplement.priceCentimes)
            )
        }
    }

private fun JSONObject.toPizzaCustomizationOrNull(): PizzaCustomization? {
    val size = runCatching { PizzaSize.valueOf(getString("size")) }.getOrNull() ?: return null
    val base = runCatching { PizzaBase.valueOf(getString("base")) }.getOrNull() ?: return null

    return PizzaCustomization(
        size = size,
        base = base,
        removedIngredients = optJSONArray("removedIngredients").toIngredientSelections(),
        addedSupplements = optJSONArray("addedSupplements").toSupplementSelections()
    )
}

private fun JSONObject.toProductCustomization(): ProductCustomization =
    ProductCustomization(
        removedIngredients = optJSONArray("removedIngredients").toIngredientSelections(),
        addedSupplements = optJSONArray("addedSupplements").toSupplementSelections(),
        includedSide = optJSONObject("includedSide")?.let { side ->
            IngredientSelection(side.getString("id"), side.getString("label"))
        },
        includedDrink = optJSONObject("includedDrink")?.let { drink ->
            IngredientSelection(drink.getString("id"), drink.getString("label"))
        }
    )

private fun JSONObject.toSpecialOfferCustomizationOrNull(): SpecialOfferCustomization? {
    val drinkLabel = optString("drinkLabel").takeIf { it.isNotBlank() } ?: return null
    val pizzasJson = optJSONArray("pizzas")
    if (pizzasJson != null && pizzasJson.length() > 0) {
        val pizzas = buildList {
            for (index in 0 until pizzasJson.length()) {
                val pizzaJson = pizzasJson.optJSONObject(index) ?: continue
                val productId = pizzaJson.optString("productId").takeIf { it.isNotBlank() } ?: continue
                val productName = pizzaJson.optString("productName").takeIf { it.isNotBlank() } ?: continue
                val customization = pizzaJson.optJSONObject("customization")
                    ?.toPizzaCustomizationOrNull()
                    ?: continue
                add(
                    SpecialOfferPizzaSelection(
                        productId = productId,
                        productName = productName,
                        customization = customization
                    )
                )
            }
        }
        if (pizzas.isEmpty()) return null
        return SpecialOfferCustomization(pizzas = pizzas, drinkLabel = drinkLabel)
    }

    // Ancien format : noms seuls, sans personnalisation.
    val names = optJSONArray("pizzaNames") ?: return null
    val pizzaNames = buildList {
        for (index in 0 until names.length()) add(names.getString(index))
    }
    if (pizzaNames.isEmpty()) return null
    val pizzas = pizzaNames.mapNotNull { name ->
        val product = PizzaCatalog.products.firstOrNull { it.name == name } ?: return@mapNotNull null
        val recipe = PizzaCatalog.recipeFor(product.id) ?: return@mapNotNull null
        SpecialOfferPizzaSelection(
            productId = product.id,
            productName = product.name,
            customization = PizzaCustomization(
                size = PizzaSize.JUNIOR,
                base = recipe.defaultBase
            )
        )
    }
    if (pizzas.isEmpty()) return null
    return SpecialOfferCustomization(pizzas = pizzas, drinkLabel = drinkLabel)
}

private fun JSONArray?.toIngredientSelections(): List<PizzaIngredientSelection> {
    if (this == null) return emptyList()
    return buildList {
        for (index in 0 until length()) {
            val ingredient = getJSONObject(index)
            add(PizzaIngredientSelection(ingredient.getString("id"), ingredient.getString("label")))
        }
    }
}

private fun JSONArray?.toSupplementSelections(): List<PizzaSupplementSelection> {
    if (this == null) return emptyList()
    return buildList {
        for (index in 0 until length()) {
            val supplement = getJSONObject(index)
            add(
                PizzaSupplementSelection(
                    id = supplement.getString("id"),
                    label = supplement.getString("label"),
                    priceCentimes = supplement.getInt("priceCentimes")
                )
            )
        }
    }
}

fun List<PaymentRecord>.toJsonPayments(): String {
    val array = JSONArray()
    forEach { payment ->
        val objectJson = JSONObject()
            .put("method", payment.method.name)
            .put("amountCentimes", payment.amountCentimes)
        payment.receivedCentimes?.let { objectJson.put("receivedCentimes", it) }
        payment.changeCentimes?.let { objectJson.put("changeCentimes", it) }
        array.put(objectJson)
    }
    return array.toString()
}

fun String.toPaymentRecords(): List<PaymentRecord> {
    val array = JSONArray(this)
    return buildList {
        for (index in 0 until array.length()) {
            val objectJson = array.getJSONObject(index)
            add(
                PaymentRecord(
                    method = PaymentMethod.valueOf(objectJson.getString("method")),
                    amountCentimes = objectJson.getInt("amountCentimes"),
                    receivedCentimes = objectJson.optInt("receivedCentimes").takeIf { objectJson.has("receivedCentimes") },
                    changeCentimes = objectJson.optInt("changeCentimes").takeIf { objectJson.has("changeCentimes") }
                )
            )
        }
    }
}

fun DeliveryInfo.toJson(): String =
    JSONObject()
        .put("customerName", customerName)
        .put("address", address)
        .put("driverNumber", driverNumber)
        .put("phoneNumber", phoneNumber)
        .toString()

fun String.toDeliveryInfoOrNull(): DeliveryInfo? {
    if (isBlank()) return null
    val objectJson = JSONObject(this)
    return DeliveryInfo(
        customerName = objectJson.getString("customerName"),
        address = objectJson.getString("address"),
        driverNumber = objectJson.getInt("driverNumber"),
        phoneNumber = objectJson.optString("phoneNumber")
    )
}
