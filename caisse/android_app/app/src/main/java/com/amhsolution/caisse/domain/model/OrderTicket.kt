package com.amhsolution.caisse.domain.model

data class OrderLineSnapshot(
    val productName: String,
    val quantity: Int,
    val totalCentimes: Int,
    val productId: String? = null,
    val lineId: String? = null,
    val unitPriceCentimes: Int? = null,
    val pizzaCustomization: PizzaCustomization? = null,
    val productCustomization: ProductCustomization? = null,
    val specialOfferCustomization: SpecialOfferCustomization? = null,
    val studentMenuCustomization: StudentMenuCustomization? = null
) {
    val displayLabel: String
        get() = buildString {
            append(quantity)
            append("x ")
            append(productName)
            pizzaCustomization?.let { customization ->
                append(" (")
                append(customization.summary)
                append(')')
            }
            productCustomization?.summary?.takeIf { it.isNotBlank() }?.let { summary ->
                append(" (")
                append(summary)
                append(')')
            }
            specialOfferCustomization?.let { customization ->
                append(" (")
                append(customization.summary)
                append(')')
            }
            studentMenuCustomization?.let { customization ->
                append(" (")
                append(customization.summary)
                append(')')
            }
        }
}

data class DeliveryInfo(
    val customerName: String,
    val address: String,
    val driverNumber: Int,
    val phoneNumber: String = ""
) {
    val driverLabel: String
        get() = "Livreur $driverNumber"
}

data class OrderTicket(
    val id: String,
    val ticketNumber: Int,
    val orderType: OrderType,
    val lines: List<OrderLineSnapshot>,
    val totalCentimes: Int,
    val createdAtEpochMillis: Long,
    val statusLabel: String,
    val deliveryInfo: DeliveryInfo? = null
) {
    val itemsLabel: String
        get() = lines.joinToString(separator = ", ") { it.displayLabel }
}

fun List<CartLine>.toOrderLineSnapshots(): List<OrderLineSnapshot> =
    map { line ->
        OrderLineSnapshot(
            productName = line.product.name,
            quantity = line.quantity,
            totalCentimes = line.lineTotalCentimes,
            productId = line.product.id,
            lineId = line.lineId,
            unitPriceCentimes = line.unitPriceCentimes,
            pizzaCustomization = line.pizzaCustomization,
            productCustomization = line.productCustomization,
            specialOfferCustomization = line.specialOfferCustomization,
            studentMenuCustomization = line.studentMenuCustomization
        )
    }

/**
 * Lignes à envoyer en cuisine lors d'une mise à jour d'attente :
 * nouvelles lignes, ou hausses de quantité (delta uniquement).
 */
fun kitchenAdditionLines(
    previous: List<OrderLineSnapshot>,
    updated: List<OrderLineSnapshot>
): List<OrderLineSnapshot> {
    val previousByKey = previous.associateBy { it.stableKey() }
    return buildList {
        for (line in updated) {
            val old = previousByKey[line.stableKey()]
            when {
                old == null -> add(line)
                line.quantity > old.quantity -> {
                    val deltaQty = line.quantity - old.quantity
                    val unit = line.unitPriceCentimes
                        ?: if (line.quantity > 0) line.totalCentimes / line.quantity else 0
                    add(line.copy(quantity = deltaQty, totalCentimes = unit * deltaQty))
                }
            }
        }
    }
}

private fun OrderLineSnapshot.stableKey(): String =
    lineId?.takeIf { it.isNotBlank() }
        ?: listOf(
            productId.orEmpty(),
            productName,
            pizzaCustomization?.summary.orEmpty(),
            productCustomization?.summary.orEmpty(),
            specialOfferCustomization?.summary.orEmpty(),
            studentMenuCustomization?.summary.orEmpty()
        ).joinToString("\u0000")

/**
 * Reconstitue un panier approximatif à partir d'un ticket et d'un catalogue.
 * On utilise le nom produit comme clé principale ; si le produit n'existe plus
 * dans le catalogue, on crée un produit \"virtuel\" avec un prix déduit du total.
 */
fun OrderTicket.toCartLinesFromCatalog(catalog: List<Product>): List<CartLine> =
    lines.map { line ->
        val product = catalog.firstOrNull { it.id == line.productId }
            ?: catalog.firstOrNull { it.name == line.productName }
            ?: Product(
                id = line.productId ?: "ticket-$ticketNumber-${line.productName}",
                name = line.productName,
                category = ProductCategory.MENUS,
                priceCentimes = if (line.quantity > 0) line.totalCentimes / line.quantity else line.totalCentimes
            )
        CartLine(
            product = product,
            quantity = line.quantity,
            lineId = line.lineId ?: product.id,
            unitPriceCentimes = line.unitPriceCentimes
                ?: if (line.quantity > 0) line.totalCentimes / line.quantity else line.totalCentimes,
            pizzaCustomization = line.pizzaCustomization,
            productCustomization = line.productCustomization,
            specialOfferCustomization = line.specialOfferCustomization,
            studentMenuCustomization = line.studentMenuCustomization
        )
    }
