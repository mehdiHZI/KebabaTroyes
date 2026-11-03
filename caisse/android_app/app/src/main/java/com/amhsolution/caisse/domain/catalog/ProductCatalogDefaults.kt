package com.amhsolution.caisse.domain.catalog

import com.amhsolution.caisse.domain.model.Product
import com.amhsolution.caisse.domain.model.ProductCategory

/** Flags / prix menu par défaut selon la carte Kebaba (seed + formulaire admin). */
object ProductCatalogDefaults {

    private val menuPrices: Map<String, Int> = mapOf(
        "kebaba-klassik" to 1000,
        "kofte" to 1250,
        "poulet" to 1250,
        "vege" to 1250,
        "ricain" to 1250,
        "sucuk" to 1250,
        "adana" to 1350,
        "krousty" to 1400,
        "avocado-fromage-frais" to 1400,
        "cheezy-jalapanos" to 1400,
        "boursin-lardons" to 1400,
        "monster" to 1400,
        "chevre-miel" to 1400,
        "takos-1-viande" to 1100,
        "takos-2-viandes" to 1300,
        "takos-3-viandes" to 1500
    )

    fun menuPriceForId(productId: String): Int? = menuPrices[productId]

    fun takosMeatCountForId(productId: String): Int =
        when (productId) {
            "takos-1-viande" -> 1
            "takos-2-viandes" -> 2
            "takos-3-viandes" -> 3
            else -> 0
        }

    fun flagsFor(category: ProductCategory): CatalogFlags {
        val sandwich = category in setOf(
            ProductCategory.KLASSIK,
            ProductCategory.DWICH,
            ProductCategory.KREATIONS
        )
        val hasFrites = sandwich ||
            category == ProductCategory.TAKOS ||
            category == ProductCategory.MENUS
        val hasCrudites = sandwich ||
            category == ProductCategory.BOWLS ||
            category == ProductCategory.ASSIETTES ||
            category == ProductCategory.TAKOS ||
            category == ProductCategory.MENUS
        return CatalogFlags(
            hasPain = sandwich,
            hasFrites = hasFrites,
            hasCrudites = hasCrudites,
            hasSupplements = hasCrudites && category != ProductCategory.MENUS,
            hasSauces = hasCrudites,
            hasTakosMeats = category == ProductCategory.TAKOS,
            supportsDrinkPlus = category == ProductCategory.BOWLS ||
                category == ProductCategory.ASSIETTES
        )
    }

    /** Construit un produit seed avec flags + prix menu carte. */
    fun product(
        id: String,
        name: String,
        category: ProductCategory,
        priceCentimes: Int,
        menuPriceCentimes: Int? = menuPriceForId(id),
        takosMeatCount: Int = takosMeatCountForId(id),
        enabled: Boolean = true,
        updatedAtEpochMillis: Long = 0L
    ): Product {
        val flags = flagsFor(category)
        return Product(
            id = id,
            name = name,
            category = category,
            priceCentimes = priceCentimes,
            menuPriceCentimes = menuPriceCentimes,
            hasPain = flags.hasPain,
            hasFrites = flags.hasFrites,
            hasCrudites = flags.hasCrudites,
            hasSupplements = flags.hasSupplements,
            hasSauces = flags.hasSauces,
            hasTakosMeats = flags.hasTakosMeats,
            supportsDrinkPlus = flags.supportsDrinkPlus,
            takosMeatCount = if (flags.hasTakosMeats) takosMeatCount else 0,
            enabled = enabled,
            updatedAtEpochMillis = updatedAtEpochMillis
        )
    }

    /** Préremplit les flags d’un nouvel article selon la catégorie choisie. */
    fun applyCategoryDefaults(product: Product): Product {
        val flags = flagsFor(product.category)
        return product.copy(
            hasPain = flags.hasPain,
            hasFrites = flags.hasFrites,
            hasCrudites = flags.hasCrudites,
            hasSupplements = flags.hasSupplements,
            hasSauces = flags.hasSauces,
            hasTakosMeats = flags.hasTakosMeats,
            supportsDrinkPlus = flags.supportsDrinkPlus,
            takosMeatCount = if (flags.hasTakosMeats) {
                product.takosMeatCount.coerceAtLeast(1)
            } else {
                0
            }
        )
    }
}

data class CatalogFlags(
    val hasPain: Boolean,
    val hasFrites: Boolean,
    val hasCrudites: Boolean,
    val hasSupplements: Boolean,
    val hasSauces: Boolean,
    val hasTakosMeats: Boolean,
    val supportsDrinkPlus: Boolean
)
