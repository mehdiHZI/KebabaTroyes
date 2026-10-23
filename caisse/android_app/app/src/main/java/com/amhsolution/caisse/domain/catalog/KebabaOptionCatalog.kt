package com.amhsolution.caisse.domain.catalog

import com.amhsolution.caisse.domain.model.Product
import com.amhsolution.caisse.domain.model.ProductCategory

data class KebabaOption(
    val id: String,
    val label: String,
    val priceCentimes: Int = 0
)

/**
 * Options parcours PDF étapes + prix menu.
 * Étape 1 pain → 2 frites → 3 garnitures → 4 suppléments → 5 sauces.
 */
object KebabaOptionCatalog {
    const val EXTRA_SAUCE_CENTS = 50
    const val MAX_INCLUDED_SAUCES = 2
    const val TAKOS_MENU_UPGRADE_CENTS = 300
    const val BOWL_ASSIETTE_DRINK_CENTS = 100

    val pains: List<KebabaOption> = listOf(
        KebabaOption("pain-frais", "Pain (frais de Momo)"),
        KebabaOption("durum", "Dürum")
    )

    val fritesEtape: List<KebabaOption> = listOf(
        KebabaOption("frites-maison", "Frites Maison", 0),
        KebabaOption("frites-opt-cheddar", "Cheddar", 150),
        KebabaOption("frites-opt-cheddar-oignon-frit", "Cheddar Oignon Frit", 200),
        KebabaOption("frites-opt-cheddar-bacon", "Cheddar Bacon", 250)
    )

    val crudites: List<KebabaOption> = listOf(
        KebabaOption("salade", "Salade"),
        KebabaOption("tomates", "Tomate"),
        KebabaOption("oignon-rouge", "Oignon Rouge"),
        KebabaOption("choux-rouge", "Choux Rouge"),
        KebabaOption("concombre", "Concombre"),
        KebabaOption("carotte", "Carotte"),
        KebabaOption("sans-crudites", "Sans les crudités"),
        KebabaOption("complet", "Complet")
    )

    val supplements: List<KebabaOption> = listOf(
        KebabaOption("piment-jalapeno", "Piment Jalapeno", 100),
        KebabaOption("cornichon", "Cornichon", 100),
        KebabaOption("cheddar-maison", "Cheddar Maison", 150),
        KebabaOption("sup-boursin", "Boursin", 150),
        KebabaOption("feta", "Feta", 150),
        KebabaOption("sup-chevre", "Chèvre", 150),
        KebabaOption("oeuf", "Œuf", 150),
        KebabaOption("avocat", "Avocat", 200),
        KebabaOption("sup-viande", "Viande", 300)
    )

    val sauces: List<KebabaOption> = listOf(
        KebabaOption("harissa", "Harissa"),
        KebabaOption("blanche-maison", "Blanche Maison"),
        KebabaOption("ketchup", "Ketchup"),
        KebabaOption("samourai", "Samouraï"),
        KebabaOption("mayo", "Mayonnaise"),
        KebabaOption("bbq", "BBQ"),
        KebabaOption("brazil", "Brazil"),
        KebabaOption("andalouse", "Andalouse"),
        KebabaOption("algerienne", "Algérienne"),
        KebabaOption("curry", "Curry"),
        KebabaOption("biggy", "Biggy")
    )

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

    private val customizableCategories = setOf(
        ProductCategory.KLASSIK,
        ProductCategory.DWICH,
        ProductCategory.KREATIONS,
        ProductCategory.BOWLS,
        ProductCategory.ASSIETTES,
        ProductCategory.TAKOS,
        ProductCategory.MENUS
    )

    fun supportsCustomization(product: Product): Boolean =
        product.category in customizableCategories

    fun hasPain(product: Product): Boolean =
        product.category in setOf(
            ProductCategory.KLASSIK,
            ProductCategory.DWICH,
            ProductCategory.KREATIONS
        )

    fun hasFritesEtape(product: Product): Boolean =
        product.category in setOf(
            ProductCategory.KLASSIK,
            ProductCategory.DWICH,
            ProductCategory.KREATIONS,
            ProductCategory.TAKOS,
            ProductCategory.MENUS
        )

    fun hasCrudites(product: Product): Boolean =
        product.category in setOf(
            ProductCategory.KLASSIK,
            ProductCategory.DWICH,
            ProductCategory.KREATIONS,
            ProductCategory.BOWLS,
            ProductCategory.ASSIETTES,
            ProductCategory.MENUS
        )

    fun hasSauces(product: Product): Boolean = hasCrudites(product)

    fun hasSupplements(product: Product): Boolean =
        product.category != ProductCategory.MENUS && hasCrudites(product)

    fun supportsMenuToggle(product: Product): Boolean =
        product.id in menuPrices

    fun menuPriceCentimes(product: Product): Int? = menuPrices[product.id]

    fun supportsDrinkPlus(product: Product): Boolean =
        product.category == ProductCategory.BOWLS ||
            product.category == ProductCategory.ASSIETTES

    fun sauceExtraCentimes(sauceCount: Int): Int {
        val extra = (sauceCount - MAX_INCLUDED_SAUCES).coerceAtLeast(0)
        return extra * EXTRA_SAUCE_CENTS
    }
}
