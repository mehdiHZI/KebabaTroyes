package com.amhsolution.caisse.ui.register

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.amhsolution.caisse.domain.catalog.OptionIngredient
import com.amhsolution.caisse.domain.catalog.ProductOptionCatalog
import com.amhsolution.caisse.domain.formatEuros
import com.amhsolution.caisse.domain.model.IngredientSelection
import com.amhsolution.caisse.domain.model.Product
import com.amhsolution.caisse.domain.model.ProductCustomization
import com.amhsolution.caisse.domain.model.SupplementSelection
import com.amhsolution.caisse.ui.theme.CaisseBlue
import com.amhsolution.caisse.ui.theme.CaisseBlueDark
import com.amhsolution.caisse.ui.theme.CaisseMuted
import com.amhsolution.caisse.ui.theme.WireframeBorder
import com.amhsolution.caisse.ui.theme.WireframeGreenBackground
import com.amhsolution.caisse.ui.theme.WireframeGreenBorder
import com.amhsolution.caisse.ui.theme.WireframeGreenText
import com.amhsolution.caisse.ui.theme.WireframeNavBackground

@Composable
fun ProductCustomizationDialog(
    product: Product,
    supplementPrices: Map<String, Int>,
    onDismiss: () -> Unit,
    onConfirm: (ProductCustomization, Int) -> Unit
) {
    val recipe = remember(product.id) { ProductOptionCatalog.recipeFor(product.id) } ?: return
    val defaultIngredients = remember(recipe, product.category) {
        recipe.ingredientIds.mapNotNull { id ->
            ProductOptionCatalog.ingredientFor(product.category, id)
        }
    }
    val availableSupplements = remember(product.category) {
        ProductOptionCatalog.ingredientsFor(product.category)
    }
    val compositionColumnCount = productCompositionColumnCountFor(defaultIngredients.size)
    val compositionFontSize = productCompositionFontSizeFor(defaultIngredients.size)
    val menuOptionPrice = remember(product.category) {
        ProductOptionCatalog.menuOptionPriceFor(product.category)
    }
    val drinkOptions = remember { ProductOptionCatalog.includedDrinkOptions() }
    val sideOptions = remember { ProductOptionCatalog.menuSideOptions }
    val fritesSauceOptions = remember { ProductOptionCatalog.fritesSauceOptions }
    val burgerReplacementSauceOptions = remember { ProductOptionCatalog.burgerReplacementSauceOptions }
    val pastaRequiresDrink = remember(product.category) {
        ProductOptionCatalog.supportsIncludedDrink(product.category)
    }
    val isFritesProduct = remember(product.id) {
        ProductOptionCatalog.isFritesProduct(product.id)
    }
    val defaultBurgerSauce = remember(product.id, product.category) {
        ProductOptionCatalog.burgerDefaultSauce(product)
    }
    var removedIds by remember(product.id) { mutableStateOf<Set<String>>(emptySet()) }
    var supplementIds by remember(product.id) { mutableStateOf<Set<String>>(emptySet()) }
    var menuOptionSelected by remember(product.id) { mutableStateOf(false) }
    var selectedDrinkId by remember(product.id) { mutableStateOf<String?>(null) }
    var selectedSideId by remember(product.id) { mutableStateOf<String?>(null) }
    var selectedSauceIds by remember(product.id) { mutableStateOf<Set<String>>(emptySet()) }
    var selectedBurgerReplacementSauceId by remember(product.id) { mutableStateOf<String?>(null) }

    val showDrinkPicker = pastaRequiresDrink || menuOptionSelected
    val showSidePicker = menuOptionSelected
    val showFritesSauces = isFritesProduct ||
        (showSidePicker && ProductOptionCatalog.isFritesMenuSide(selectedSideId))
    val burgerSauceRemoved =
        defaultBurgerSauce != null && defaultBurgerSauce.id in removedIds
    val showBurgerSauceReplace = burgerSauceRemoved
    val requiresDrinkSelection = pastaRequiresDrink || menuOptionSelected
    val requiresSideSelection = menuOptionSelected
    val supplementTotal = supplementIds.sumOf { id ->
        ProductOptionCatalog.supplementPrice(product.category, id, supplementPrices)
    }
    val sauceTotal = selectedSauceIds.sumOf { id ->
        ProductOptionCatalog.fritesSaucePrice(id, supplementPrices)
    }
    val menuOptionTotal = if (menuOptionSelected) menuOptionPrice ?: 0 else 0
    val totalCentimes =
        product.priceCentimes + supplementTotal + menuOptionTotal + sauceTotal

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .widthIn(max = 840.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, WireframeBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(product.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(
                            when {
                                isFritesProduct -> "Choisir les sauces"
                                showBurgerSauceReplace -> "Choisir la sauce de remplacement"
                                pastaRequiresDrink -> "Personnaliser et choisir la boisson incluse"
                                menuOptionSelected -> "Personnaliser le menu (accompagnement + boisson)"
                                else -> "Personnaliser la composition"
                            },
                            color = CaisseMuted
                        )
                    }
                    Text(
                        formatEuros(totalCentimes),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = WireframeGreenText
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = WireframeBorder.copy(alpha = 0.25f)
                )

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    if (defaultIngredients.isNotEmpty()) {
                        item {
                            Text("Composition incluse", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Touchez un ingrédient pour le retirer.", style = MaterialTheme.typography.bodySmall, color = CaisseMuted)
                        }
                        item(key = "default-composition") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(7.dp)
                            ) {
                                defaultIngredients.forEach { ingredient ->
                                    CompactIngredientCard(
                                        ingredient = ingredient,
                                        included = ingredient.id !in removedIds,
                                        onClick = {
                                            val nextRemoved = removedIds.toggleValue(ingredient.id)
                                            removedIds = nextRemoved
                                            if (
                                                defaultBurgerSauce != null &&
                                                ingredient.id == defaultBurgerSauce.id &&
                                                ingredient.id !in nextRemoved
                                            ) {
                                                selectedBurgerReplacementSauceId = null
                                            }
                                        },
                                        fontSize = compositionFontSize,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                repeat(compositionColumnCount - defaultIngredients.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                    if (showBurgerSauceReplace) {
                        item(key = "burger-sauce-replace-title") {
                            Text(
                                "Sauce de remplacement",
                                modifier = Modifier.padding(top = 8.dp),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Optionnel — remplace ${defaultBurgerSauce?.label.orEmpty()}.",
                                style = MaterialTheme.typography.bodySmall,
                                color = CaisseMuted
                            )
                        }
                        items(
                            burgerReplacementSauceOptions.chunked(ProductSupplementColumnCount),
                            key = { row -> row.joinToString(prefix = "burger-sauce-") { it.id } }
                        ) { sauceRow ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(7.dp)
                            ) {
                                sauceRow.forEach { sauce ->
                                    CompactOptionCard(
                                        label = sauce.label,
                                        detail = "Remplace",
                                        selected = selectedBurgerReplacementSauceId == sauce.id,
                                        onClick = {
                                            selectedBurgerReplacementSauceId =
                                                if (selectedBurgerReplacementSauceId == sauce.id) {
                                                    null
                                                } else {
                                                    sauce.id
                                                }
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                repeat(ProductSupplementColumnCount - sauceRow.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                    menuOptionPrice?.let { price ->
                        item(key = "menu-option") {
                            Text(
                                "Offre menu",
                                modifier = Modifier.padding(top = 8.dp),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Ajoute l'accompagnement et la boisson au produit.",
                                style = MaterialTheme.typography.bodySmall,
                                color = CaisseMuted
                            )
                        }
                        item(key = "menu-option-card") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(7.dp)
                            ) {
                                CompactOptionCard(
                                    label = "Le menu",
                                    detail = "+${formatEuros(price)}",
                                    selected = menuOptionSelected,
                                    onClick = {
                                        val enabling = !menuOptionSelected
                                        menuOptionSelected = enabling
                                        if (!enabling) {
                                            selectedDrinkId = null
                                            selectedSideId = null
                                            selectedSauceIds = emptySet()
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                                repeat(ProductSupplementColumnCount - 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                    if (showSidePicker) {
                        item(key = "menu-side-title") {
                            Text(
                                "Accompagnement",
                                modifier = Modifier.padding(top = 8.dp),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Choisissez frites ou pâtes cheeses.",
                                style = MaterialTheme.typography.bodySmall,
                                color = CaisseMuted
                            )
                        }
                        item(key = "menu-side-options") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(7.dp)
                            ) {
                                sideOptions.forEach { side ->
                                    CompactOptionCard(
                                        label = side.label,
                                        detail = "Inclus",
                                        selected = selectedSideId == side.id,
                                        onClick = {
                                            selectedSideId = side.id
                                            if (!ProductOptionCatalog.isFritesMenuSide(side.id)) {
                                                selectedSauceIds = emptySet()
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                repeat(ProductSupplementColumnCount - sideOptions.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                    if (showFritesSauces) {
                        item(key = "frites-sauces-title") {
                            Text(
                                "Sauces",
                                modifier = Modifier.padding(top = 8.dp),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Sélectionnez une ou plusieurs sauces pour les frites.",
                                style = MaterialTheme.typography.bodySmall,
                                color = CaisseMuted
                            )
                        }
                        items(
                            fritesSauceOptions.chunked(ProductSupplementColumnCount),
                            key = { row -> row.joinToString(prefix = "sauce-") { it.id } }
                        ) { sauceRow ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(7.dp)
                            ) {
                                sauceRow.forEach { sauce ->
                                    val price =
                                        ProductOptionCatalog.fritesSaucePrice(sauce.id, supplementPrices)
                                    CompactOptionCard(
                                        label = sauce.label,
                                        detail = if (price > 0) "+${formatEuros(price)}" else "Incluse",
                                        selected = sauce.id in selectedSauceIds,
                                        onClick = {
                                            selectedSauceIds = selectedSauceIds.toggleValue(sauce.id)
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                repeat(ProductSupplementColumnCount - sauceRow.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                    if (showDrinkPicker) {
                        item(key = "included-drink-title") {
                            Text(
                                if (pastaRequiresDrink) "Boisson incluse" else "Boisson du menu",
                                modifier = Modifier.padding(top = 8.dp),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                if (pastaRequiresDrink) {
                                    "Choisissez la canette offerte avec la pasta."
                                } else {
                                    "Choisissez la canette incluse dans le menu."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = CaisseMuted
                            )
                        }
                        items(
                            drinkOptions.chunked(ProductSupplementColumnCount),
                            key = { row -> row.joinToString(prefix = "drink-") { it.id } }
                        ) { drinkRow ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(7.dp)
                            ) {
                                drinkRow.forEach { drink ->
                                    CompactOptionCard(
                                        label = drink.label,
                                        detail = "Incluse",
                                        selected = selectedDrinkId == drink.id,
                                        onClick = { selectedDrinkId = drink.id },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                repeat(ProductSupplementColumnCount - drinkRow.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                    if (availableSupplements.isNotEmpty() && !isFritesProduct) {
                        item {
                            Text(
                                "Suppléments · ${supplementIds.size}/${availableSupplements.size} · +${formatEuros(supplementTotal)}",
                                modifier = Modifier.padding(top = 8.dp),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text("Tous les ingrédients de cette catégorie sont disponibles.", style = MaterialTheme.typography.bodySmall, color = CaisseMuted)
                        }
                        items(
                            availableSupplements.chunked(ProductSupplementColumnCount),
                            key = { row -> row.joinToString(prefix = "supp-") { it.id } }
                        ) { ingredientRow ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(7.dp)
                            ) {
                                ingredientRow.forEach { ingredient ->
                                    val price = ProductOptionCatalog.supplementPrice(
                                        product.category,
                                        ingredient.id,
                                        supplementPrices
                                    )
                                    CompactOptionCard(
                                        label = ingredient.label,
                                        detail = "+${formatEuros(price)}",
                                        selected = ingredient.id in supplementIds,
                                        onClick = { supplementIds = supplementIds.toggleValue(ingredient.id) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                repeat(ProductSupplementColumnCount - ingredientRow.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = WireframeBorder.copy(alpha = 0.25f)
                )
                val selectedDrink = drinkOptions.firstOrNull { it.id == selectedDrinkId }
                val selectedSide = sideOptions.firstOrNull { it.id == selectedSideId }
                val selectedBurgerReplacementSauce =
                    burgerReplacementSauceOptions.firstOrNull {
                        it.id == selectedBurgerReplacementSauceId
                    }
                val canConfirm = (!requiresDrinkSelection || selectedDrink != null) &&
                    (!requiresSideSelection || selectedSide != null)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Annuler")
                    }
                    Card(
                        onClick = {
                            if (canConfirm) {
                                val customization = ProductCustomization(
                                    removedIngredients = defaultIngredients
                                        .filter { it.id in removedIds }
                                        .map { IngredientSelection(it.id, it.label) },
                                    addedSupplements = buildList {
                                        addAll(
                                            availableSupplements
                                                .filter { it.id in supplementIds }
                                                .map { ingredient ->
                                                    SupplementSelection(
                                                        ingredient.id,
                                                        ingredient.label,
                                                        ProductOptionCatalog.supplementPrice(
                                                            product.category,
                                                            ingredient.id,
                                                            supplementPrices
                                                        )
                                                    )
                                                }
                                        )
                                        if (menuOptionSelected) {
                                            add(
                                                SupplementSelection(
                                                    ProductOptionCatalog.MENU_OPTION_ID,
                                                    ProductOptionCatalog.MENU_OPTION_LABEL,
                                                    ProductOptionCatalog.MENU_OPTION_PRICE_CENTIMES
                                                )
                                            )
                                        }
                                        addAll(
                                            fritesSauceOptions
                                                .filter { it.id in selectedSauceIds }
                                                .map { sauce ->
                                                    SupplementSelection(
                                                        sauce.id,
                                                        sauce.label,
                                                        ProductOptionCatalog.fritesSaucePrice(
                                                            sauce.id,
                                                            supplementPrices
                                                        )
                                                    )
                                                }
                                        )
                                        selectedBurgerReplacementSauce?.let { sauce ->
                                            add(
                                                SupplementSelection(
                                                    sauce.id,
                                                    sauce.label,
                                                    0
                                                )
                                            )
                                        }
                                    },
                                    includedSide = selectedSide?.let { side ->
                                        IngredientSelection(side.id, side.label)
                                    },
                                    includedDrink = selectedDrink?.let { drink ->
                                        IngredientSelection(drink.id, drink.label)
                                    }
                                )
                                onConfirm(customization, totalCentimes)
                            }
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (canConfirm) CaisseBlue else CaisseMuted
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (canConfirm) CaisseBlueDark else WireframeBorder
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when {
                                    canConfirm -> "Ajouter · ${formatEuros(totalCentimes)}"
                                    requiresSideSelection && selectedSide == null ->
                                        "Choisir un accompagnement"
                                    else -> "Choisir une boisson"
                                },
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactIngredientCard(
    ingredient: OptionIngredient,
    included: Boolean,
    onClick: () -> Unit,
    fontSize: TextUnit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(CompactCustomizationButtonHeight),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (included) WireframeGreenBackground else Color.White
        ),
        border = BorderStroke(1.dp, if (included) WireframeGreenBorder else WireframeBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp, vertical = 3.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = ingredient.label,
                style = MaterialTheme.typography.labelMedium,
                fontSize = fontSize,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = if (included) WireframeGreenText else CaisseMuted
            )
        }
    }
}

@Composable
private fun CompactOptionCard(
    label: String,
    detail: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(CompactCustomizationButtonHeight),
        shape = RoundedCornerShape(9.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) WireframeGreenBackground else WireframeNavBackground
        ),
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) WireframeGreenBorder else WireframeBorder)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(detail, style = MaterialTheme.typography.labelSmall, color = CaisseMuted)
        }
    }
}

private fun Set<String>.toggleValue(value: String): Set<String> =
    if (value in this) this - value else this + value

private fun productCompositionColumnCountFor(ingredientCount: Int): Int =
    maxOf(ProductCompactColumnCount, ingredientCount)

private fun productCompositionFontSizeFor(ingredientCount: Int): TextUnit = when {
    ingredientCount <= 4 -> 13.sp
    ingredientCount <= 6 -> 12.sp
    else -> 11.sp
}

private const val ProductCompactColumnCount = 6
private const val ProductSupplementColumnCount = ProductCompactColumnCount
private val CompactCustomizationButtonHeight = 48.dp
