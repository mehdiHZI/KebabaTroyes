package com.amhsolution.caisse.domain.model

import com.amhsolution.caisse.domain.catalog.PizzaBase
import com.amhsolution.caisse.domain.catalog.PizzaSize

data class IngredientSelection(
    val id: String,
    val label: String
)

data class SupplementSelection(
    val id: String,
    val label: String,
    val priceCentimes: Int
)

typealias PizzaIngredientSelection = IngredientSelection
typealias PizzaSupplementSelection = SupplementSelection

data class PizzaCustomization(
    val size: PizzaSize,
    val base: PizzaBase,
    val removedIngredients: List<IngredientSelection> = emptyList(),
    val addedSupplements: List<SupplementSelection> = emptyList()
) {
    val summary: String
        get() = buildList {
            add(this@PizzaCustomization.size.label)
            add(this@PizzaCustomization.base.label)
            if (removedIngredients.isNotEmpty()) {
                add("Sans ${removedIngredients.joinToString { it.label }}")
            }
            if (addedSupplements.isNotEmpty()) {
                add("Supp. ${addedSupplements.joinToString { it.label }}")
            }
        }.joinToString(" · ")

    val stableKey: String
        get() = buildString {
            append(size.name)
            append('-')
            append(base.name)
            append("-sans-")
            append(removedIngredients.map { it.id }.sorted().joinToString("_"))
            append("-supp-")
            append(
                addedSupplements
                    .map { "${it.id}-${it.priceCentimes}" }
                    .sorted()
                    .joinToString("_")
            )
        }
}

data class ProductCustomization(
    val removedIngredients: List<IngredientSelection> = emptyList(),
    val addedSupplements: List<SupplementSelection> = emptyList(),
    /** Boisson incluse (ex. pasta / menu) — sans surcoût. */
    val includedDrink: IngredientSelection? = null,
    /** Accompagnement du menu (frites / pâtes cheeses) — sans surcoût. */
    val includedSide: IngredientSelection? = null
) {
    val summary: String
        get() = buildList {
            val burgerReplacements = addedSupplements.filter {
                it.id.startsWith("burger-replace-sauce-")
            }
            val removedForSummary = if (burgerReplacements.isNotEmpty()) {
                removedIngredients.filterNot { it.id.startsWith("sauce-") }
            } else {
                removedIngredients
            }
            if (removedForSummary.isNotEmpty()) {
                add("Sans ${removedForSummary.joinToString { it.label }}")
            }
            burgerReplacements.forEach { add("Sauce ${it.label}") }
            val otherSupplements = addedSupplements.filterNot {
                it.id.startsWith("burger-replace-sauce-")
            }
            if (otherSupplements.isNotEmpty()) {
                add("Supp. ${otherSupplements.joinToString { it.label }}")
            }
            includedSide?.let { add("Accomp. ${it.label}") }
            includedDrink?.let { add("Boisson ${it.label}") }
        }.joinToString(" · ")

    val stableKey: String
        get() = buildString {
            append("sans-")
            append(removedIngredients.map { it.id }.sorted().joinToString("_"))
            append("-supp-")
            append(
                addedSupplements
                    .map { "${it.id}-${it.priceCentimes}" }
                    .sorted()
                    .joinToString("_")
            )
            append("-side-")
            append(includedSide?.id.orEmpty())
            append("-boisson-")
            append(includedDrink?.id.orEmpty())
        }
}

data class SpecialOfferPizzaSelection(
    val productId: String,
    val productName: String,
    val customization: PizzaCustomization
) {
    val summary: String
        get() = buildString {
            append(productName)
            val detail = customization.offerDetailSummary
            if (detail.isNotBlank()) {
                append(" (")
                append(detail)
                append(')')
            }
        }
}

data class SpecialOfferCustomization(
    val pizzas: List<SpecialOfferPizzaSelection>,
    val drinkLabel: String
) {
    val pizzaNames: List<String>
        get() = pizzas.map { it.productName }

    val supplementsTotalCentimes: Int
        get() = pizzas.sumOf { pizza ->
            pizza.customization.addedSupplements.sumOf { it.priceCentimes }
        }

    val summary: String
        get() = buildString {
            append(pizzas.joinToString(" · ") { it.summary })
            append(" · ")
            append(drinkLabel)
        }

    val stableKey: String
        get() = buildString {
            pizzas.forEach { pizza ->
                append(pizza.productId)
                append('-')
                append(pizza.customization.stableKey)
                append('|')
            }
            append(drinkLabel)
        }
}

enum class StudentMenuChoiceKind {
    PIZZA,
    BURGER,
    SANDWICH
}

data class StudentMenuCustomization(
    val choiceKind: StudentMenuChoiceKind,
    val itemProductId: String,
    val itemProductName: String,
    val drinkId: String,
    val drinkLabel: String,
    val pizzaCustomization: PizzaCustomization? = null,
    val productCustomization: ProductCustomization? = null
) {
    val priceCentimes: Int
        get() = when (choiceKind) {
            StudentMenuChoiceKind.PIZZA -> 800
            StudentMenuChoiceKind.BURGER,
            StudentMenuChoiceKind.SANDWICH -> 750
        }

    val summary: String
        get() = buildString {
            append(itemProductName)
            pizzaCustomization?.let { customization ->
                val detail = buildList {
                    add(customization.base.label)
                    if (customization.removedIngredients.isNotEmpty()) {
                        add("Sans ${customization.removedIngredients.joinToString { it.label }}")
                    }
                }.joinToString(" · ")
                if (detail.isNotBlank()) {
                    append(" (")
                    append(detail)
                    append(')')
                }
            }
            productCustomization?.summary?.takeIf { it.isNotBlank() }?.let { detail ->
                append(" (")
                append(detail)
                append(')')
            }
            append(" · ")
            append(drinkLabel)
        }

    val stableKey: String
        get() = buildString {
            append(choiceKind.name)
            append('-')
            append(itemProductId)
            append('-')
            append(pizzaCustomization?.stableKey.orEmpty())
            append('-')
            append(productCustomization?.stableKey.orEmpty())
            append('-')
            append(drinkId)
        }
}

private val PizzaCustomization.offerDetailSummary: String
    get() = buildList {
        add(base.label)
        if (removedIngredients.isNotEmpty()) {
            add("Sans ${removedIngredients.joinToString { it.label }}")
        }
        if (addedSupplements.isNotEmpty()) {
            add("Supp. ${addedSupplements.joinToString { it.label }}")
        }
    }.joinToString(" · ")

