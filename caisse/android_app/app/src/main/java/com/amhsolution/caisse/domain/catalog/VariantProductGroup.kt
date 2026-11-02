package com.amhsolution.caisse.domain.catalog

import com.amhsolution.caisse.domain.model.Product
import com.amhsolution.caisse.domain.model.ProductCategory

/**
 * Famille d'articles regroupés en caisse (ex. Tenders → ×4 / ×8).
 */
data class VariantProductGroup(
    val id: String,
    val label: String,
    val category: ProductCategory,
    val variantProductIds: List<String>
) {
    fun resolveVariants(catalog: List<Product>): List<Product> =
        variantProductIds.mapNotNull { id -> catalog.firstOrNull { it.id == id } }

    fun startingPriceCentimes(catalog: List<Product>): Int =
        resolveVariants(catalog).minOfOrNull { it.priceCentimes } ?: 0
}

object VariantProductGroupCatalog {
    val ptitPlusGroups: List<VariantProductGroup> = listOf(
        VariantProductGroup(
            id = "chicken-wings",
            label = "Chicken Wings",
            category = ProductCategory.PTIT_PLUS,
            variantProductIds = listOf("chicken-wings-x4", "chicken-wings-x8")
        ),
        VariantProductGroup(
            id = "tenders",
            label = "Tenders",
            category = ProductCategory.PTIT_PLUS,
            variantProductIds = listOf("tenders-x4", "tenders-x8")
        ),
        VariantProductGroup(
            id = "camembert",
            label = "Camembert",
            category = ProductCategory.PTIT_PLUS,
            variantProductIds = listOf("camembert-x4", "camembert-x8")
        ),
        VariantProductGroup(
            id = "oignon-rings",
            label = "Oignon Rings",
            category = ProductCategory.PTIT_PLUS,
            variantProductIds = listOf("oignon-rings-x4", "oignon-rings-x8")
        ),
        VariantProductGroup(
            id = "mozza-sticks",
            label = "Mozza Sticks",
            category = ProductCategory.PTIT_PLUS,
            variantProductIds = listOf("mozza-sticks-x4", "mozza-sticks-x8")
        ),
        VariantProductGroup(
            id = "nuggets",
            label = "Nuggets",
            category = ProductCategory.PTIT_PLUS,
            variantProductIds = listOf("nuggets-x4", "nuggets-x8")
        ),
        VariantProductGroup(
            id = "jalapenos-cheezy",
            label = "Jalapenos Cheezy",
            category = ProductCategory.PTIT_PLUS,
            variantProductIds = listOf("jalapenos-cheezy-x4", "jalapenos-cheezy-x8")
        )
    )

    val barquetteGroups: List<VariantProductGroup> = listOf(
        VariantProductGroup(
            id = "barquette-kebab-frites",
            label = "Barquette Kebab + Frites",
            category = ProductCategory.BARQUETTES,
            variantProductIds = listOf(
                "barquette-kebab-frites-petite",
                "barquette-kebab-frites-grande"
            )
        ),
        VariantProductGroup(
            id = "barquette-viande-kebab",
            label = "Barquette Viande de Kebab",
            category = ProductCategory.BARQUETTES,
            variantProductIds = listOf(
                "barquette-viande-kebab-petite",
                "barquette-viande-kebab-grande"
            )
        )
    )

    /** Frites / boulgour / cheddar — un bouton famille, puis le choix. */
    val fritesSizeGroups: List<VariantProductGroup> = listOf(
        VariantProductGroup(
            id = "frites",
            label = "Frites",
            category = ProductCategory.FRITES,
            variantProductIds = listOf("frites-petite", "frites-grande")
        ),
        VariantProductGroup(
            id = "boulgour",
            label = "Boulgour",
            category = ProductCategory.FRITES,
            variantProductIds = listOf("boulgour-petite", "boulgour-grande")
        ),
        VariantProductGroup(
            id = "frites-cheddar",
            label = "Frites Cheddar",
            category = ProductCategory.FRITES,
            variantProductIds = listOf(
                "frites-cheddar",
                "frites-cheddar-oignon-frit",
                "frites-cheddar-bacon"
            )
        )
    )

    fun groupsFor(category: ProductCategory): List<VariantProductGroup> =
        when (category) {
            ProductCategory.PTIT_PLUS -> ptitPlusGroups
            ProductCategory.BARQUETTES -> barquetteGroups
            ProductCategory.FRITES -> fritesSizeGroups
            else -> emptyList()
        }

    /** Produits de la catégorie non couverts par un groupe (ex. frites cheddar). */
    fun ungroupedProducts(category: ProductCategory, catalog: List<Product>): List<Product> {
        val groupedIds = groupsFor(category).flatMap { it.variantProductIds }.toSet()
        return catalog.filter { it.category == category && it.id !in groupedIds }
    }

    fun variantShortLabel(product: Product): String {
        val name = product.name
        return when {
            name.contains("×4", ignoreCase = true) ||
                Regex("""(?i)(?<![\w])x4(?![\w])""").containsMatchIn(name) -> "×4"
            name.contains("×8", ignoreCase = true) ||
                Regex("""(?i)(?<![\w])x8(?![\w])""").containsMatchIn(name) -> "×8"
            name.contains("oignon", ignoreCase = true) &&
                name.contains("frit", ignoreCase = true) -> "Cheddar Oignon Frit"
            name.contains("bacon", ignoreCase = true) -> "Cheddar Bacon"
            name.equals("Frites Cheddar", ignoreCase = true) -> "Cheddar"
            name.contains("petite", ignoreCase = true) -> "Petite"
            name.contains("grande", ignoreCase = true) -> "Grande"
            else -> name
        }
    }
}
