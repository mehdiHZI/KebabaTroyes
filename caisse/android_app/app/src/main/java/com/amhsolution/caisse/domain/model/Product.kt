package com.amhsolution.caisse.domain.model

/**
 * Article catalogue Kebaba.
 * Prix = TTC en centimes. Les flags pilotent le parcours de personnalisation.
 */
data class Product(
    val id: String,
    val name: String,
    val category: ProductCategory,
    val priceCentimes: Int,
    val menuPriceCentimes: Int? = null,
    val hasPain: Boolean = false,
    val hasFrites: Boolean = false,
    val hasCrudites: Boolean = false,
    val hasSupplements: Boolean = false,
    val hasSauces: Boolean = false,
    val hasTakosMeats: Boolean = false,
    val supportsDrinkPlus: Boolean = false,
    val takosMeatCount: Int = 0,
    val enabled: Boolean = true,
    val updatedAtEpochMillis: Long = 0L
) {
    val supportsMenuToggle: Boolean
        get() = menuPriceCentimes != null && menuPriceCentimes > 0

    val isCustomizable: Boolean
        get() = hasPain || hasFrites || hasCrudites || hasSupplements ||
            hasSauces || hasTakosMeats || supportsMenuToggle || supportsDrinkPlus ||
            category == ProductCategory.MENUS
}
