package com.amhsolution.caisse.domain.catalog

import com.amhsolution.caisse.domain.model.Product

/**
 * Ancien catalogue pizza Milano — vidé pour Kebaba.
 * Conservé pour compiler les écrans / tests legacy non branchés.
 */
enum class PizzaBase(val label: String) {
    TOMATE("Base tomate"),
    CREME("Base crème")
}

enum class PizzaSize(val label: String, val kitchenLabel: String) {
    JUNIOR("S", "29cm"),
    SENIOR("M", "33cm"),
    MEGA("L", "40cm")
}

data class PizzaIngredient(
    val id: String,
    val label: String
)

data class PizzaRecipe(
    val productId: String,
    val defaultBase: PizzaBase,
    val ingredientIds: List<String>
)

object PizzaCatalog {
    const val DEFAULT_SUPPLEMENT_PRICE_CENTIMES = 100
    const val DEFAULT_SIZE_OFFSET_SENIOR_CENTIMES = 300
    const val DEFAULT_SIZE_OFFSET_MEGA_CENTIMES = 700
    const val SIZE_OFFSET_SENIOR_KEY = "SENIOR"
    const val SIZE_OFFSET_MEGA_KEY = "MEGA"

    val defaultSizeOffsets: Map<String, Int> = mapOf(
        SIZE_OFFSET_SENIOR_KEY to DEFAULT_SIZE_OFFSET_SENIOR_CENTIMES,
        SIZE_OFFSET_MEGA_KEY to DEFAULT_SIZE_OFFSET_MEGA_CENTIMES
    )

    val ingredients: List<PizzaIngredient> = emptyList()
    val products: List<Product> = emptyList()
    val recipes: List<PizzaRecipe> = emptyList()

    val defaultSupplementPrices: Map<String, Int> = emptyMap()

    fun recipeFor(productId: String): PizzaRecipe? = null

    fun ingredientFor(id: String): PizzaIngredient? = null

    fun sizeOffsetFor(
        size: PizzaSize,
        sizeOffsets: Map<String, Int> = defaultSizeOffsets
    ): Int = when (size) {
        PizzaSize.JUNIOR -> 0
        PizzaSize.SENIOR -> sizeOffsets[SIZE_OFFSET_SENIOR_KEY] ?: DEFAULT_SIZE_OFFSET_SENIOR_CENTIMES
        PizzaSize.MEGA -> sizeOffsets[SIZE_OFFSET_MEGA_KEY] ?: DEFAULT_SIZE_OFFSET_MEGA_CENTIMES
    }

    fun priceFor(
        product: Product,
        size: PizzaSize,
        sizeOffsets: Map<String, Int> = defaultSizeOffsets
    ): Int = product.priceCentimes + sizeOffsetFor(size, sizeOffsets)

    fun customizedPriceFor(
        product: Product,
        size: PizzaSize,
        supplementIds: Set<String>,
        supplementPrices: Map<String, Int>,
        sizeOffsets: Map<String, Int> = defaultSizeOffsets
    ): Int = priceFor(product, size, sizeOffsets) + supplementIds.sumOf { id ->
        supplementPrices[id] ?: DEFAULT_SUPPLEMENT_PRICE_CENTIMES
    }

    fun supplementPrice(id: String, prices: Map<String, Int>): Int =
        prices[id] ?: DEFAULT_SUPPLEMENT_PRICE_CENTIMES
}
