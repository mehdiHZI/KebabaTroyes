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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.amhsolution.caisse.domain.catalog.PizzaBase
import com.amhsolution.caisse.domain.catalog.PizzaCatalog
import com.amhsolution.caisse.domain.catalog.PizzaIngredient
import com.amhsolution.caisse.domain.catalog.PizzaSize
import com.amhsolution.caisse.domain.formatEuros
import com.amhsolution.caisse.domain.model.PizzaCustomization
import com.amhsolution.caisse.domain.model.PizzaIngredientSelection
import com.amhsolution.caisse.domain.model.PizzaSupplementSelection
import com.amhsolution.caisse.domain.model.Product
import com.amhsolution.caisse.ui.theme.CaisseBlue
import com.amhsolution.caisse.ui.theme.CaisseBlueDark
import com.amhsolution.caisse.ui.theme.CaisseMuted
import com.amhsolution.caisse.ui.theme.WireframeBorder
import com.amhsolution.caisse.ui.theme.WireframeGreenBackground
import com.amhsolution.caisse.ui.theme.WireframeGreenBorder
import com.amhsolution.caisse.ui.theme.WireframeGreenText
import com.amhsolution.caisse.ui.theme.WireframeNavBackground

@Composable
fun PizzaCustomizationDialog(
    product: Product,
    supplementPrices: Map<String, Int>,
    onDismiss: () -> Unit,
    onConfirm: (PizzaCustomization, Int) -> Unit,
    sizeOffsets: Map<String, Int> = PizzaCatalog.defaultSizeOffsets
) {
    val recipe = remember(product.id) { PizzaCatalog.recipeFor(product.id) } ?: return
    var selectedSize by remember(product.id) { mutableStateOf(PizzaSize.JUNIOR) }
    var selectedBase by remember(product.id) { mutableStateOf(recipe.defaultBase) }
    var removedIngredientIds by remember(product.id) { mutableStateOf<Set<String>>(emptySet()) }
    var selectedSupplementIds by remember(product.id) { mutableStateOf<Set<String>>(emptySet()) }

    val defaultIngredients = remember(recipe) {
        recipe.ingredientIds.mapNotNull(PizzaCatalog::ingredientFor)
    }
    val compositionColumnCount = compositionColumnCountFor(defaultIngredients.size)
    val compositionFontSize = compositionFontSizeFor(defaultIngredients.size)
    val availableSupplements = PizzaCatalog.ingredients
    val supplementTotalCentimes = selectedSupplementIds.sumOf {
        supplementPrices[it] ?: PizzaCatalog.DEFAULT_SUPPLEMENT_PRICE_CENTIMES
    }
    val totalCentimes = PizzaCatalog.customizedPriceFor(
        product = product,
        size = selectedSize,
        supplementIds = selectedSupplementIds,
        supplementPrices = supplementPrices,
        sizeOffsets = sizeOffsets
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .widthIn(max = 820.dp),
            shape = RoundedCornerShape(18.dp),
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
                    Column {
                        Text(
                            text = product.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Personnalisation de la pizza",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CaisseMuted
                        )
                    }
                    Text(
                        text = formatEuros(totalCentimes),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = WireframeGreenText
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 14.dp),
                    color = WireframeBorder.copy(alpha = 0.25f)
                )

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        SectionTitle("Taille")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PizzaSize.entries.forEach { size ->
                                SelectionCard(
                                    label = size.label,
                                    detail = formatEuros(
                                        PizzaCatalog.priceFor(product, size, sizeOffsets)
                                    ),
                                    selected = size == selectedSize,
                                    onClick = { selectedSize = size },
                                    labelFontSize = 12.sp,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            repeat(PizzaCompactColumnCount - PizzaSize.entries.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    item {
                        SectionTitle("Base")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PizzaBase.entries.forEach { base ->
                                SelectionCard(
                                    label = base.label,
                                    selected = base == selectedBase,
                                    onClick = { selectedBase = base },
                                    labelFontSize = 12.sp,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            repeat(PizzaCompactColumnCount - PizzaBase.entries.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    item {
                        SectionTitle("Composition incluse")
                        Text(
                            text = "Touchez un ingrédient pour le retirer ou le remettre.",
                            style = MaterialTheme.typography.bodySmall,
                            color = CaisseMuted,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    item(key = "included-composition") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            defaultIngredients.forEach { ingredient ->
                                IngredientToggleCard(
                                    ingredient = ingredient,
                                    selected = ingredient.id !in removedIngredientIds,
                                    onClick = {
                                        removedIngredientIds = removedIngredientIds.toggle(ingredient.id)
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

                    item {
                        SectionTitle(
                            "Suppléments · ${selectedSupplementIds.size}/${availableSupplements.size} · " +
                                "+${formatEuros(supplementTotalCentimes)}"
                        )
                        Text(
                            text = "Les tarifs se modifient dans l'onglet Prix.",
                            style = MaterialTheme.typography.bodySmall,
                            color = CaisseMuted,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    items(
                        availableSupplements.chunked(PizzaSupplementColumnCount),
                        key = { row -> "supplements-${row.joinToString { it.id }}" }
                    ) { ingredientRow ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ingredientRow.forEach { ingredient ->
                                val supplementPrice = supplementPrices[ingredient.id]
                                    ?: PizzaCatalog.DEFAULT_SUPPLEMENT_PRICE_CENTIMES
                                SelectionCard(
                                    label = ingredient.label,
                                    detail = if (ingredient.id in selectedSupplementIds) {
                                        "Ajouté · +${formatEuros(supplementPrice)}"
                                    } else {
                                        "+${formatEuros(supplementPrice)}"
                                    },
                                    selected = ingredient.id in selectedSupplementIds,
                                    onClick = {
                                        selectedSupplementIds = selectedSupplementIds.toggle(ingredient.id)
                                    },
                                    height = CompactPizzaOptionHeight,
                                    labelFontSize = 12.sp,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            repeat(PizzaSupplementColumnCount - ingredientRow.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 14.dp),
                    color = WireframeBorder.copy(alpha = 0.25f)
                )

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
                            val customization = PizzaCustomization(
                                size = selectedSize,
                                base = selectedBase,
                                removedIngredients = defaultIngredients
                                    .filter { it.id in removedIngredientIds }
                                    .map { PizzaIngredientSelection(it.id, it.label) },
                                addedSupplements = availableSupplements
                                    .filter { it.id in selectedSupplementIds }
                                    .map { ingredient ->
                                        PizzaSupplementSelection(
                                            id = ingredient.id,
                                            label = ingredient.label,
                                            priceCentimes = supplementPrices[ingredient.id]
                                                ?: PizzaCatalog.DEFAULT_SUPPLEMENT_PRICE_CENTIMES
                                        )
                                    }
                            )
                            onConfirm(customization, totalCentimes)
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CaisseBlue),
                        border = BorderStroke(1.dp, CaisseBlueDark)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "Ajouter · ${formatEuros(totalCentimes)}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 8.dp, bottom = 6.dp)
    )
}

@Composable
private fun SelectionCard(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    detail: String? = null,
    height: Dp = CompactPizzaOptionHeight,
    labelFontSize: TextUnit = TextUnit.Unspecified
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(height),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) WireframeGreenBackground else WireframeNavBackground
        ),
        border = BorderStroke(
            if (selected) 2.dp else 1.dp,
            if (selected) WireframeGreenBorder else WireframeBorder
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                fontSize = labelFontSize,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            detail?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = CaisseMuted,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun IngredientToggleCard(
    ingredient: PizzaIngredient,
    selected: Boolean,
    onClick: () -> Unit,
    fontSize: TextUnit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(CompactPizzaOptionHeight),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) WireframeGreenBackground else Color.White
        ),
        border = BorderStroke(
            1.dp,
            if (selected) WireframeGreenBorder else WireframeBorder
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp, vertical = 3.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = ingredient.label,
                style = MaterialTheme.typography.labelMedium,
                fontSize = fontSize,
                fontWeight = FontWeight.SemiBold,
                color = if (selected) WireframeGreenText else CaisseMuted,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun Set<String>.toggle(value: String): Set<String> =
    if (value in this) this - value else this + value

private fun compositionColumnCountFor(ingredientCount: Int): Int = when {
    ingredientCount <= 4 -> 4
    ingredientCount == 5 -> 5
    else -> ingredientCount
}

private fun compositionFontSizeFor(ingredientCount: Int): TextUnit = when {
    ingredientCount <= 4 -> 13.sp
    ingredientCount <= 6 -> 12.sp
    else -> 11.sp
}

private const val PizzaCompactColumnCount = 6
private const val PizzaSupplementColumnCount = PizzaCompactColumnCount
private val CompactPizzaOptionHeight = 48.dp
