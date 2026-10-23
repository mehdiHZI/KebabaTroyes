package com.amhsolution.caisse.domain.model

data class CartLine(
    val product: Product,
    val quantity: Int,
    val lineId: String = product.id,
    val unitPriceCentimes: Int = product.priceCentimes,
    val pizzaCustomization: PizzaCustomization? = null,
    val productCustomization: ProductCustomization? = null,
    val specialOfferCustomization: SpecialOfferCustomization? = null,
    val studentMenuCustomization: StudentMenuCustomization? = null
) {
    val lineTotalCentimes: Int
        get() = unitPriceCentimes * quantity
}
