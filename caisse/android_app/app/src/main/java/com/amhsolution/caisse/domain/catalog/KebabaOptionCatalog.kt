package com.amhsolution.caisse.domain.catalog

import com.amhsolution.caisse.domain.model.Product

data class KebabaOption(
    val id: String,
    val label: String,
    val priceCentimes: Int = 0
)

/**
 * Options parcours PDF étapes + prix menu.
 * Format (seul/menu) → pain → garnitures → suppléments → [si menu: frites + boisson] → sauces.
 */
object KebabaOptionCatalog {
    const val EXTRA_SAUCE_CENTS = 50
    const val MAX_INCLUDED_SAUCES = 2
    const val TAKOS_MENU_UPGRADE_CENTS = 300
    const val BOWL_ASSIETTE_DRINK_CENTS = 100
    const val MENU_OPTION_ID = "option-menu"

    val formatOptions: List<KebabaOption> = listOf(
        KebabaOption("format-seul", "Seul"),
        KebabaOption("format-menu", "Menu")
    )

    val pains: List<KebabaOption> = listOf(
        KebabaOption("pain-frais", "Pain (frais de Momo)"),
        KebabaOption("durum", "Dürum")
    )

    /** Softs inclus dans un menu (hors café). */
    val menuDrinks: List<KebabaOption> = listOf(
        KebabaOption("coca-cola-33", "Coca Cola 33 cl"),
        KebabaOption("coca-cola-cherry-33", "Coca Cola Cherry 33 cl"),
        KebabaOption("oasis-pomme-cassis-framboise-33", "Oasis Pomme Cassis Framboise 33 cl"),
        KebabaOption("oasis-tropical-33", "Oasis tropical 33 cl"),
        KebabaOption("fanta-orange-33", "Fanta orange 33 cl"),
        KebabaOption("tropico-orange-ananas-33", "Tropico orange ananas 33 cl"),
        KebabaOption("perrier-33", "Perrier 33 cl"),
        KebabaOption("lipton-ice-tea-peche-33", "Lipton ice tea pêche 33 cl"),
        KebabaOption("orangina-33", "Orangina 33 cl"),
        KebabaOption("ayran-25", "Ayran 25 cl"),
        KebabaOption("eau-50", "Eau 50 cl")
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

    const val SUP_VIANDE_ID = "sup-viande"
    const val SUP_VIANDE_CENTS = 300

    val supplements: List<KebabaOption> = listOf(
        KebabaOption("piment-jalapeno", "Piment Jalapeno", 100),
        KebabaOption("cornichon", "Cornichon", 100),
        KebabaOption("cheddar-maison", "Cheddar Maison", 150),
        KebabaOption("sup-boursin", "Boursin", 150),
        KebabaOption("feta", "Feta", 150),
        KebabaOption("sup-chevre", "Chèvre", 150),
        KebabaOption("oeuf", "Œuf", 150),
        KebabaOption("avocat", "Avocat", 200),
        KebabaOption(SUP_VIANDE_ID, "Viande", SUP_VIANDE_CENTS)
    )

    /**
     * Viandes Takos (carte visuelle) — aussi utilisées pour le supplément viande.
     */
    val takosMeats: List<KebabaOption> = listOf(
        KebabaOption("viande-kebab", "Kebab"),
        KebabaOption("viande-poulet", "Poulet Mariné"),
        KebabaOption("viande-steak", "Steak"),
        KebabaOption("viande-nugget", "Nugget"),
        KebabaOption("viande-tenders", "Tenders"),
        KebabaOption("viande-cordon-bleu", "Cordon Bleu"),
        KebabaOption("viande-falafel", "Falafel")
    )

    /** Supplément viande : mêmes choix, +3 € chacune, multi libre. */
    val supplementMeats: List<KebabaOption> =
        takosMeats.map { it.copy(priceCentimes = SUP_VIANDE_CENTS) }

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

    fun supportsCustomization(product: Product): Boolean = product.isCustomizable

    fun hasPain(product: Product): Boolean = product.hasPain

    fun hasFritesEtape(product: Product): Boolean = product.hasFrites

    fun hasCrudites(product: Product): Boolean = product.hasCrudites

    fun hasSauces(product: Product): Boolean = product.hasSauces

    fun hasSupplements(product: Product): Boolean = product.hasSupplements

    fun hasTakosMeats(product: Product): Boolean = product.hasTakosMeats

    /** Nombre de viandes à choisir selon le SKU Takos. */
    fun takosRequiredMeatCount(product: Product): Int =
        product.takosMeatCount.coerceAtLeast(0)

    fun supportsMenuToggle(product: Product): Boolean = product.supportsMenuToggle

    fun menuPriceCentimes(product: Product): Int? = product.menuPriceCentimes

    fun supportsDrinkPlus(product: Product): Boolean = product.supportsDrinkPlus

    fun sauceExtraCentimes(sauceCount: Int): Int {
        val extra = (sauceCount - MAX_INCLUDED_SAUCES).coerceAtLeast(0)
        return extra * EXTRA_SAUCE_CENTS
    }
}
