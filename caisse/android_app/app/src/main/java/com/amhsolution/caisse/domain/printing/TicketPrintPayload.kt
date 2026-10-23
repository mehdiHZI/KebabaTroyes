package com.amhsolution.caisse.domain.printing

import com.amhsolution.caisse.domain.catalog.PizzaCatalog
import com.amhsolution.caisse.domain.catalog.ProductOptionCatalog
import com.amhsolution.caisse.domain.catalog.SampleCatalog
import com.amhsolution.caisse.domain.model.DeliveryInfo
import com.amhsolution.caisse.domain.model.HistoryRecord
import com.amhsolution.caisse.domain.model.IngredientSelection
import com.amhsolution.caisse.domain.model.OrderLineSnapshot
import com.amhsolution.caisse.domain.model.OrderTicket
import com.amhsolution.caisse.domain.model.OrderType
import com.amhsolution.caisse.domain.model.PaymentRecord
import com.amhsolution.caisse.domain.model.StudentMenuChoiceKind
import com.amhsolution.caisse.domain.model.SupplementSelection
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.ZoneId

private const val TicketPrintSchemaVersion = 1

data class TicketPrintPayload(
    val schemaVersion: Int,
    val documentType: TicketPrintDocumentType,
    val restaurantName: String,
    val ticketNumber: Int,
    val ticketLabel: String,
    val businessDay: String,
    val paidAtEpochMillis: Long,
    val orderType: OrderType,
    val lines: List<TicketPrintLine>,
    val totalCentimes: Int,
    val payments: List<PaymentRecord>,
    val deliveryInfo: DeliveryInfo?,
    val printer: TicketPrinterOptions,
    val fiscalInfo: TicketFiscalInfo? = null,
    /** Titre cuisine optionnel (ex. « AJOUT CUISINE »). Null = « BON CUISINE ». */
    val kitchenTitle: String? = null
)

enum class TicketPrintDocumentType {
    CUSTOMER_RECEIPT,
    CUSTOMER_PROVISIONAL_RECEIPT,
    KITCHEN_TICKET,
    CLOSURE_REPORT
}

data class TicketPrinterOptions(
    val profile: String = MilanoGustoPrinterConfig.PROFILE_ID,
    val paperWidth: String = "80mm",
    val lineColumns: Int = MilanoGustoPrinterConfig.LINE_COLUMNS_80MM,
    val cutPaper: Boolean = true,
    val openCashDrawer: Boolean = false,
    val codePage: PrinterCodePage = PrinterCodePage.PC850
)

data class TicketPrintLine(
    val productName: String,
    val displayLabel: String,
    val quantity: Int,
    val unitTotalCentimes: Int,
    val lineTotalCentimes: Int,
    val kitchenDetails: List<String> = emptyList()
)

data class TicketFiscalInfo(
    val vatNumber: String,
    val vatRateBasisPoints: Int,
    val totalExcludingVatCentimes: Int,
    val vatCentimes: Int
) {
    val vatRateLabel: String
        get() = "${vatRateBasisPoints / 100},${(vatRateBasisPoints % 100).toString().padStart(2, '0')} %"
}

fun HistoryRecord.toCustomerTicketPrintPayload(
    restaurantName: String = "Kebaba",
    printer: TicketPrinterOptions = TicketPrinterOptions(),
    vatNumber: String = DefaultRestaurantVatNumber,
    vatRateBasisPoints: Int = DefaultRestaurantVatRateBasisPoints
): TicketPrintPayload =
    TicketPrintPayload(
        schemaVersion = TicketPrintSchemaVersion,
        documentType = TicketPrintDocumentType.CUSTOMER_RECEIPT,
        restaurantName = restaurantName,
        ticketNumber = ticketNumber,
        ticketLabel = ticketLabel,
        businessDay = businessDay,
        paidAtEpochMillis = paidAtEpochMillis,
        orderType = orderType,
        lines = lines.map { it.toTicketPrintLine() },
        totalCentimes = totalCentimes,
        payments = payments,
        deliveryInfo = deliveryInfo,
        printer = printer,
        fiscalInfo = ticketFiscalInfo(
            totalCentimes = totalCentimes,
            vatNumber = vatNumber,
            vatRateBasisPoints = vatRateBasisPoints
        )
    )

fun HistoryRecord.toKitchenTicketPrintPayload(
    restaurantName: String = "Kebaba",
    printer: TicketPrinterOptions = TicketPrinterOptions(openCashDrawer = false)
): TicketPrintPayload =
    TicketPrintPayload(
        schemaVersion = TicketPrintSchemaVersion,
        documentType = TicketPrintDocumentType.KITCHEN_TICKET,
        restaurantName = restaurantName,
        ticketNumber = ticketNumber,
        ticketLabel = ticketLabel,
        businessDay = businessDay,
        paidAtEpochMillis = paidAtEpochMillis,
        orderType = orderType,
        lines = lines.map { it.toTicketPrintLine() },
        totalCentimes = totalCentimes,
        payments = emptyList(),
        deliveryInfo = deliveryInfo?.toKitchenReference(),
        printer = printer
    )

fun OrderTicket.toProvisionalCustomerTicketPrintPayload(
    restaurantName: String = "Kebaba",
    printedAtEpochMillis: Long = System.currentTimeMillis(),
    zoneId: ZoneId = ZoneId.systemDefault(),
    printer: TicketPrinterOptions = TicketPrinterOptions(openCashDrawer = false)
): TicketPrintPayload =
    TicketPrintPayload(
        schemaVersion = TicketPrintSchemaVersion,
        documentType = TicketPrintDocumentType.CUSTOMER_PROVISIONAL_RECEIPT,
        restaurantName = restaurantName,
        ticketNumber = ticketNumber,
        ticketLabel = "#$ticketNumber",
        businessDay = Instant.ofEpochMilli(createdAtEpochMillis)
            .atZone(zoneId)
            .toLocalDate()
            .toString(),
        paidAtEpochMillis = printedAtEpochMillis,
        orderType = orderType,
        lines = lines.map { it.toTicketPrintLine() },
        totalCentimes = totalCentimes,
        payments = emptyList(),
        deliveryInfo = deliveryInfo,
        printer = printer
    )

fun OrderTicket.toKitchenTicketPrintPayload(
    restaurantName: String = "Kebaba",
    printedAtEpochMillis: Long = System.currentTimeMillis(),
    zoneId: ZoneId = ZoneId.systemDefault(),
    printer: TicketPrinterOptions = TicketPrinterOptions(openCashDrawer = false),
    addition: Boolean = false
): TicketPrintPayload =
    TicketPrintPayload(
        schemaVersion = TicketPrintSchemaVersion,
        documentType = TicketPrintDocumentType.KITCHEN_TICKET,
        restaurantName = restaurantName,
        ticketNumber = ticketNumber,
        ticketLabel = "#$ticketNumber",
        businessDay = Instant.ofEpochMilli(createdAtEpochMillis)
            .atZone(zoneId)
            .toLocalDate()
            .toString(),
        paidAtEpochMillis = printedAtEpochMillis,
        orderType = orderType,
        lines = lines.map { it.toTicketPrintLine() },
        totalCentimes = totalCentimes,
        payments = emptyList(),
        deliveryInfo = deliveryInfo?.toKitchenReference(),
        printer = printer,
        kitchenTitle = if (addition) "AJOUT CUISINE" else null
    )

fun TicketPrintPayload.toJsonString(): String =
    JSONObject()
        .put("schemaVersion", schemaVersion)
        .put("documentType", documentType.name)
        .put("restaurant", JSONObject().put("name", restaurantName))
        .put(
            "ticket",
            JSONObject()
                .put("number", ticketNumber)
                .put("label", ticketLabel)
                .put("businessDay", businessDay)
                .put("paidAtEpochMillis", paidAtEpochMillis)
                .put("orderType", orderType.name)
                .put("orderTypeLabel", orderType.label)
        )
        .put("delivery", deliveryInfo?.toJsonObject() ?: JSONObject.NULL)
        .put("lines", lines.toTicketLinesJsonArray())
        .put(
            "totals",
            JSONObject()
                .put("totalCentimes", totalCentimes)
                .put("fiscal", fiscalInfo?.toJsonObject() ?: JSONObject.NULL)
        )
        .put("payments", payments.toPaymentsJsonArray())
        .put("printer", printer.toJsonObject())
        .toString()

private fun OrderLineSnapshot.toTicketPrintLine(): TicketPrintLine {
    val smoothieFlavor = smoothieFlavorForKitchen()
    return TicketPrintLine(
        productName = if (smoothieFlavor != null) "Smoothie" else productName,
        displayLabel = displayLabel,
        quantity = quantity,
        unitTotalCentimes = if (quantity > 0) totalCentimes / quantity else totalCentimes,
        lineTotalCentimes = totalCentimes,
        kitchenDetails = buildList {
            smoothieFlavor?.let { add(it) }
            addAll(buildKitchenDetails())
        }
    )
}

private fun DeliveryInfo.toJsonObject(): JSONObject =
    JSONObject()
        .put("customerName", customerName)
        .put("address", address)
        .put("phoneNumber", phoneNumber)
        .put("driverNumber", driverNumber)
        .put("driverLabel", driverLabel)

private fun DeliveryInfo.toKitchenReference(): DeliveryInfo =
    copy(address = "", phoneNumber = "")

private fun List<TicketPrintLine>.toTicketLinesJsonArray(): JSONArray =
    JSONArray().apply {
        this@toTicketLinesJsonArray.forEach { line ->
            put(
                JSONObject()
                    .put("productName", line.productName)
                    .put("displayLabel", line.displayLabel)
                    .put("quantity", line.quantity)
                    .put("unitTotalCentimes", line.unitTotalCentimes)
                    .put("lineTotalCentimes", line.lineTotalCentimes)
                    .put("kitchenDetails", line.kitchenDetails.toJsonArray())
            )
        }
    }

private fun List<PaymentRecord>.toPaymentsJsonArray(): JSONArray =
    JSONArray().apply {
        this@toPaymentsJsonArray.forEach { payment ->
            val paymentJson = JSONObject()
                .put("method", payment.method.name)
                .put("label", payment.method.label)
                .put("amountCentimes", payment.amountCentimes)
            payment.receivedCentimes?.let { paymentJson.put("receivedCentimes", it) }
            payment.changeCentimes?.let { paymentJson.put("changeCentimes", it) }
            put(paymentJson)
        }
    }

private fun TicketPrinterOptions.toJsonObject(): JSONObject =
    JSONObject()
        .put("profile", profile)
        .put("paperWidth", paperWidth)
        .put("lineColumns", lineColumns)
        .put("cutPaper", cutPaper)
        .put("openCashDrawer", openCashDrawer)
        .put("codePage", codePage.name)

private fun TicketFiscalInfo.toJsonObject(): JSONObject =
    JSONObject()
        .put("vatNumber", vatNumber)
        .put("vatRateBasisPoints", vatRateBasisPoints)
        .put("vatRateLabel", vatRateLabel)
        .put("totalExcludingVatCentimes", totalExcludingVatCentimes)
        .put("vatCentimes", vatCentimes)

private fun ticketFiscalInfo(
    totalCentimes: Int,
    vatNumber: String,
    vatRateBasisPoints: Int
): TicketFiscalInfo {
    val divisor = 10_000 + vatRateBasisPoints
    val vatCentimes = ((totalCentimes.toLong() * vatRateBasisPoints) + divisor / 2)
        .div(divisor)
        .toInt()
    return TicketFiscalInfo(
        vatNumber = vatNumber,
        vatRateBasisPoints = vatRateBasisPoints,
        totalExcludingVatCentimes = totalCentimes - vatCentimes,
        vatCentimes = vatCentimes
    )
}

private fun OrderLineSnapshot.buildKitchenDetails(): List<String> {
    val pizzaDetails = pizzaKitchenDetails()
    if (pizzaDetails.isNotEmpty()) return pizzaDetails

    val productDetails = productKitchenDetails()
    if (productDetails.isNotEmpty()) return productDetails

    specialOfferCustomization?.let { customization ->
        return buildList {
            customization.pizzas.forEachIndexed { index, pizza ->
                add("Pizza ${index + 1} : ${pizza.productName}")
                val recipe = PizzaCatalog.recipeFor(pizza.productId)
                val removedIds = pizza.customization.removedIngredients.map { it.id }.toSet()
                add(pizza.customization.base.label)
                recipe?.ingredientIds
                    ?.filterNot { it in removedIds }
                    ?.mapNotNull { PizzaCatalog.ingredientFor(it)?.label }
                    ?.let { addAll(it) }
                addAll(
                    ingredientSelectionsAsKitchenLines(
                        pizza.customization.removedIngredients,
                        "Sans"
                    )
                )
                addAll(
                    supplementSelectionsAsKitchenLines(
                        pizza.customization.addedSupplements,
                        "Supp."
                    )
                )
            }
            add("Boisson : ${customization.drinkLabel}")
        }
    }

    studentMenuCustomization?.let { customization ->
        return buildList {
            add("MENU ETUDIANT")
            when (customization.choiceKind) {
                StudentMenuChoiceKind.PIZZA -> {
                    add(customization.itemProductName)
                    customization.pizzaCustomization?.let { pizza ->
                        add("Taille : ${pizza.size.kitchenLabel}")
                        add(pizza.base.label)
                        val recipe = PizzaCatalog.recipeFor(customization.itemProductId)
                        val removedIds = pizza.removedIngredients.map { it.id }.toSet()
                        recipe?.ingredientIds
                            ?.filterNot { it in removedIds }
                            ?.mapNotNull { PizzaCatalog.ingredientFor(it)?.label }
                            ?.let { addAll(it) }
                        addAll(
                            ingredientSelectionsAsKitchenLines(pizza.removedIngredients, "Sans")
                        )
                    }
                }
                StudentMenuChoiceKind.BURGER,
                StudentMenuChoiceKind.SANDWICH -> {
                    add(customization.itemProductName)
                    val recipe = ProductOptionCatalog.recipeFor(customization.itemProductId)
                    val category = SampleCatalog.products
                        .firstOrNull { it.id == customization.itemProductId }
                        ?.category
                    val productCustom = customization.productCustomization
                    val removedIngredients = productCustom?.removedIngredients.orEmpty()
                    val removedIds = removedIngredients.map { it.id }.toSet()
                    if (recipe != null && category != null) {
                        recipe.ingredientIds
                            .filterNot { it in removedIds }
                            .mapNotNull {
                                ProductOptionCatalog.ingredientFor(category, it)?.label
                            }
                            .let { addAll(it) }
                    }
                    val burgerReplacements = productCustom?.addedSupplements.orEmpty().filter {
                        ProductOptionCatalog.isBurgerReplacementSauce(it.id)
                    }
                    val removedForKitchen = if (burgerReplacements.isNotEmpty()) {
                        removedIngredients.filterNot {
                            ProductOptionCatalog.isBurgerRecipeSauce(it.id)
                        }
                    } else {
                        removedIngredients
                    }
                    addAll(ingredientSelectionsAsKitchenLines(removedForKitchen, "Sans"))
                    addAll(burgerReplacements.map { "Sauce ${it.label}" })
                }
            }
            add(customization.drinkLabel)
        }
    }

    return emptyList()
}

/** Goût du smoothie pour le bon cuisine (hors ice coffee / café). */
private fun OrderLineSnapshot.smoothieFlavorForKitchen(): String? {
    val id = productId ?: return null
    if (!id.startsWith("smoothie-") || id.startsWith("smoothie-ice-coffee")) return null
    SmoothieKitchenFlavors[id]?.let { return it }
    if (productName.startsWith("Smoothie ", ignoreCase = true)) {
        return productName.substringAfter("Smoothie ").trim().takeIf { it.isNotEmpty() }
    }
    return null
}

private val SmoothieKitchenFlavors = mapOf(
    "smoothie-banane" to "Banane",
    "smoothie-fraise" to "Fraise",
    "smoothie-mangue" to "Mangue",
    "smoothie-fraise-banane" to "Fraise-Banane",
    "smoothie-fraise-mangue" to "Fraise-Mangue",
    "smoothie-banane-mangue" to "Banane-Mangue",
    "smoothie-tropical" to "Tropical"
)

private fun OrderLineSnapshot.pizzaKitchenDetails(): List<String> {
    val recipe = productId?.let(PizzaCatalog::recipeFor) ?: return emptyList()
    val customization = pizzaCustomization
    val removedIngredients = customization?.removedIngredients.orEmpty()
    val removedIds = removedIngredients.map { it.id }.toSet()
    val baseLabel = customization?.base?.label ?: recipe.defaultBase.label
    val sizeLabel = customization?.size?.kitchenLabel
    val ingredients = recipe.ingredientIds
        .filterNot { it in removedIds }
        .mapNotNull { PizzaCatalog.ingredientFor(it)?.label }

    return buildList {
        sizeLabel?.let { add("Taille : $it") }
        add(baseLabel)
        addAll(ingredients)
        addAll(ingredientSelectionsAsKitchenLines(removedIngredients, "Sans"))
        addAll(supplementSelectionsAsKitchenLines(customization?.addedSupplements.orEmpty(), "Supp."))
    }
}

private fun OrderLineSnapshot.productKitchenDetails(): List<String> {
    val productId = productId ?: return emptyList()
    val product = SampleCatalog.products.firstOrNull { it.id == productId } ?: return emptyList()
    val recipe = ProductOptionCatalog.recipeFor(productId) ?: return emptyList()
    val customization = productCustomization
    val removedIngredients = customization?.removedIngredients.orEmpty()
    val removedIds = removedIngredients.map { it.id }.toSet()
    val ingredients = recipe.ingredientIds
        .filterNot { it in removedIds }
        .mapNotNull { ProductOptionCatalog.ingredientFor(product.category, it)?.label }

    return buildList {
        addAll(ingredients)
        val supplements = customization?.addedSupplements.orEmpty()
        val burgerReplacements = supplements.filter { ProductOptionCatalog.isBurgerReplacementSauce(it.id) }
        val removedForKitchen = if (burgerReplacements.isNotEmpty()) {
            removedIngredients.filterNot { ProductOptionCatalog.isBurgerRecipeSauce(it.id) }
        } else {
            removedIngredients
        }
        addAll(ingredientSelectionsAsKitchenLines(removedForKitchen, "Sans"))
        addAll(burgerReplacements.map { "Sauce ${it.label}" })
        val menuOption = supplements.filter { it.id == ProductOptionCatalog.MENU_OPTION_ID }
        val fritesSauces = supplements.filter { ProductOptionCatalog.isFritesSauce(it.id) }
        val otherSupplements = supplements.filterNot {
            it.id == ProductOptionCatalog.MENU_OPTION_ID ||
                ProductOptionCatalog.isFritesSauce(it.id) ||
                ProductOptionCatalog.isBurgerReplacementSauce(it.id)
        }
        addAll(supplementSelectionsAsKitchenLines(otherSupplements, "Supp."))
        if (menuOption.isNotEmpty()) {
            add("Menu")
        }
        customization?.includedSide?.let { add(it.label) }
        addAll(fritesSauces.map { "Sauce ${it.label}" })
        customization?.includedDrink?.let { add(it.label) }
    }
}

private fun ingredientSelectionsAsKitchenLines(
    ingredients: List<IngredientSelection>,
    prefix: String
): List<String> =
    ingredients.map { "$prefix ${it.label}" }

private fun supplementSelectionsAsKitchenLines(
    supplements: List<SupplementSelection>,
    prefix: String
): List<String> =
    supplements.map { "$prefix ${it.label}" }

private fun List<String>.toJsonArray(): JSONArray =
    JSONArray().apply {
        this@toJsonArray.forEach { put(it) }
    }

private const val DefaultRestaurantVatNumber = "FR37106121080"
private const val DefaultRestaurantVatRateBasisPoints = 1_000
