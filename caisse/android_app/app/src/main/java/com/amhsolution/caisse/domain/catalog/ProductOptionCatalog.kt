package com.amhsolution.caisse.domain.catalog

import com.amhsolution.caisse.domain.model.Product
import com.amhsolution.caisse.domain.model.ProductCategory

/**
 * Pont de compatibilité avec l’UI Milano (ProductCustomizationDialog, etc.).
 * La personnalisation Kebaba V1 passe par [KebabaOptionCatalog] + KebabaCustomizationDialog.
 */
object ProductOptionCatalog {
    const val DEFAULT_SUPPLEMENT_PRICE_CENTIMES = 100
    const val MENU_OPTION_ID = "option-menu"
    const val MENU_OPTION_LABEL = "Menu (frites + boisson)"
    const val MENU_OPTION_PRICE_CENTIMES = 300
    const val STUDENT_MENU_PRODUCT_ID = "menu-etudiant"
    const val STUDENT_MENU_BURGER_ID = "kebaba-klassik"
    const val STUDENT_MENU_SANDWICH_ID = "kofte"
    const val STUDENT_MENU_EXCLUDED_PIZZA_ID = ""
    const val STUDENT_MENU_PIZZA_PRICE_CENTIMES = 800
    const val STUDENT_MENU_CLASSICO_PRICE_CENTIMES = 800
    const val MENU_SIDE_FRITES_ID = "menu-side-frites"

    val menuSideOptions: List<OptionIngredient> = listOf(
        OptionIngredient("menu-side-frites", "Frites")
    )

    val fritesSauceOptions: List<OptionIngredient> =
        KebabaOptionCatalog.sauces.map { OptionIngredient("frites-sauce-${it.id}", it.label) }

    val burgerReplacementSauceOptions: List<OptionIngredient> =
        KebabaOptionCatalog.sauces.map { OptionIngredient("burger-replace-sauce-${it.id}", it.label) }

    private val ingredientsByCategory: Map<ProductCategory, List<OptionIngredient>> =
        ProductCategory.entries.associateWith { category ->
            when (category) {
                ProductCategory.KLASSIK,
                ProductCategory.DWICH,
                ProductCategory.KREATIONS,
                ProductCategory.BOWLS,
                ProductCategory.ASSIETTES,
                ProductCategory.TAKOS,
                ProductCategory.MENUS ->
                    (KebabaOptionCatalog.crudites + KebabaOptionCatalog.supplements + KebabaOptionCatalog.sauces)
                        .map { OptionIngredient(it.id, it.label) }
                ProductCategory.FRITES -> fritesSauceOptions
                else -> emptyList()
            }
        }

    private val recipes: List<ProductRecipe> =
        SampleCatalog.products
            .filter { KebabaOptionCatalog.supportsCustomization(it) }
            .map { ProductRecipe(it.id, KebabaOptionCatalog.crudites.take(6).map { c -> c.id }) }

    val defaultSupplementPrices: Map<String, Int> = buildMap {
        KebabaOptionCatalog.supplements.forEach { opt ->
            ProductCategory.entries.forEach { cat ->
                put(priceKey(cat, opt.id), opt.priceCentimes)
            }
        }
        KebabaOptionCatalog.sauces.forEach { opt ->
            put(priceKey(ProductCategory.FRITES, "frites-sauce-${opt.id}"), 0)
        }
    }

    fun supports(category: ProductCategory): Boolean = category in ingredientsByCategory &&
        ingredientsByCategory[category].orEmpty().isNotEmpty()

    fun supportsCustomization(product: Product): Boolean =
        KebabaOptionCatalog.supportsCustomization(product)

    fun isFritesProduct(productId: String): Boolean =
        productId.startsWith("frites-") || productId.startsWith("boulgour-")

    fun isFritesSauce(sauceId: String): Boolean = sauceId.startsWith("frites-sauce-")

    fun isBurgerRecipeSauce(ingredientId: String): Boolean = false

    fun isBurgerReplacementSauce(sauceId: String): Boolean =
        sauceId.startsWith("burger-replace-sauce-")

    fun burgerDefaultSauce(product: Product): OptionIngredient? = null

    fun isFritesMenuSide(sideId: String?): Boolean = sideId == MENU_SIDE_FRITES_ID

    fun fritesSaucePrice(sauceId: String, prices: Map<String, Int>): Int =
        prices[priceKey(ProductCategory.FRITES, sauceId)] ?: 0

    fun supportsMenuOption(category: ProductCategory): Boolean =
        category == ProductCategory.KLASSIK ||
            category == ProductCategory.DWICH ||
            category == ProductCategory.KREATIONS ||
            category == ProductCategory.TAKOS

    fun supportsIncludedDrink(category: ProductCategory): Boolean = false

    fun includedDrinkOptions(): List<OptionIngredient> =
        SampleCatalog.products
            .filter { it.category == ProductCategory.BOISSONS }
            .map { OptionIngredient(it.id, it.name) }

    fun specialOfferIncludesBottle(productId: String): Boolean = false

    fun specialOfferIncludesCans(productId: String): Boolean = false

    fun isStudentMenu(productId: String): Boolean = productId == STUDENT_MENU_PRODUCT_ID

    fun studentMenuPizzaOptions(): List<Product> = emptyList()

    fun studentMenuBurger(): Product? =
        SampleCatalog.products.firstOrNull { it.id == STUDENT_MENU_BURGER_ID }

    fun studentMenuSandwich(): Product? =
        SampleCatalog.products.firstOrNull { it.id == STUDENT_MENU_SANDWICH_ID }

    fun studentMenuDrinkOptions(): List<OptionIngredient> = includedDrinkOptions()

    fun specialOfferBottleDrinkOptions(): List<OptionIngredient> = emptyList()

    fun specialOfferCanDrinkOptions(): List<OptionIngredient> = includedDrinkOptions()

    fun menuOptionPriceFor(category: ProductCategory): Int? =
        if (supportsMenuOption(category)) MENU_OPTION_PRICE_CENTIMES else null

    fun ingredientsFor(category: ProductCategory): List<OptionIngredient> =
        ingredientsByCategory[category].orEmpty()

    fun recipeFor(productId: String): ProductRecipe? =
        recipes.firstOrNull { it.productId == productId }

    fun ingredientFor(category: ProductCategory, ingredientId: String): OptionIngredient? =
        ingredientsFor(category).firstOrNull { it.id == ingredientId }

    fun priceKey(category: ProductCategory, ingredientId: String): String =
        "${category.name}:$ingredientId"

    fun supplementPrice(
        category: ProductCategory,
        ingredientId: String,
        prices: Map<String, Int>
    ): Int = prices[priceKey(category, ingredientId)]
        ?: KebabaOptionCatalog.supplements.firstOrNull { it.id == ingredientId }?.priceCentimes
        ?: DEFAULT_SUPPLEMENT_PRICE_CENTIMES
}
