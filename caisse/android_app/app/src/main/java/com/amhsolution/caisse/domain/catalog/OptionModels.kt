package com.amhsolution.caisse.domain.catalog

/** Ingrédient / option affiché dans les dialogs legacy Milano. */
data class OptionIngredient(
    val id: String,
    val label: String
)

/** Composition par défaut d’un produit (ids d’[OptionIngredient]). */
data class ProductRecipe(
    val productId: String,
    val ingredientIds: List<String>
)
