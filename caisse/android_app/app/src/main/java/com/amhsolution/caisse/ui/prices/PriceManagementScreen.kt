package com.amhsolution.caisse.ui.prices

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.amhsolution.caisse.domain.catalog.SampleCatalog
import com.amhsolution.caisse.domain.catalog.PizzaCatalog
import com.amhsolution.caisse.domain.catalog.ProductOptionCatalog
import com.amhsolution.caisse.domain.formatEuros
import com.amhsolution.caisse.domain.formatEurosAmount
import com.amhsolution.caisse.domain.model.Product
import com.amhsolution.caisse.domain.model.ProductCategory
import com.amhsolution.caisse.domain.parseEurosInput
import com.amhsolution.caisse.ui.theme.AMHCaisseTheme
import com.amhsolution.caisse.ui.theme.CaisseBlue
import com.amhsolution.caisse.ui.theme.CaisseBlueDark
import com.amhsolution.caisse.ui.theme.CaisseMuted
import com.amhsolution.caisse.ui.theme.WireframeBorder
import com.amhsolution.caisse.ui.theme.WireframeGreenText
import com.amhsolution.caisse.ui.theme.WireframeNavBackground
import com.amhsolution.caisse.ui.theme.WireframeYellowBackground

private val PriceRowHeight = 56.dp
private val PriceInputWidth = 96.dp

private enum class PricePanel {
    PRODUCTS,
    PIZZA_SIZE_OFFSETS,
    PIZZA_SUPPLEMENTS,
    PRODUCT_SUPPLEMENTS
}

@Composable
fun PriceManagementScreen(
    products: List<Product>,
    onUpdatePrice: (productId: String, priceCentimes: Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    pizzaSupplementPrices: Map<String, Int> = PizzaCatalog.defaultSupplementPrices,
    onUpdatePizzaSupplementPrice: (ingredientId: String, priceCentimes: Int) -> Unit = { _, _ -> },
    productSupplementPrices: Map<String, Int> = ProductOptionCatalog.defaultSupplementPrices,
    onUpdateProductSupplementPrice: (priceKey: String, priceCentimes: Int) -> Unit = { _, _ -> },
    pizzaSizeOffsets: Map<String, Int> = PizzaCatalog.defaultSizeOffsets,
    onUpdatePizzaSizeOffset: (sizeKey: String, offsetCentimes: Int) -> Unit = { _, _ -> }
) {
    var selectedCategory by remember { mutableStateOf<ProductCategory?>(null) }
    var selectedPanel by remember { mutableStateOf(PricePanel.PRODUCTS) }
    var selectedSupplementCategory by remember {
        mutableStateOf<ProductCategory?>(null)
    }

    val visibleProducts = remember(products, selectedCategory, selectedPanel) {
        val sorted = products.sortedWith(
            compareBy<Product> { it.category.ordinal }.thenBy { it.name }
        )
        if (selectedPanel != PricePanel.PRODUCTS) emptyList()
        else if (selectedCategory == null) sorted
        else sorted.filter { it.category == selectedCategory }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Prix",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = when {
                            selectedPanel == PricePanel.PIZZA_SIZE_OFFSETS ->
                                "Anciennes majorations taille (non utilisées Kebaba)"
                            selectedPanel == PricePanel.PIZZA_SUPPLEMENTS ||
                                selectedPanel == PricePanel.PRODUCT_SUPPLEMENTS ->
                                "Modifier le prix unitaire des suppléments"
                            else -> "Modifier les prix des articles"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = CaisseMuted
                    )
                }

                TextButton(onClick = onBack) {
                    Text("Retour accueil")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = WireframeBorder.copy(alpha = 0.25f))
            Spacer(modifier = Modifier.height(14.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    CategoryChip(
                        label = "Tous",
                        selected = selectedPanel == PricePanel.PRODUCTS && selectedCategory == null,
                        onClick = {
                            selectedPanel = PricePanel.PRODUCTS
                            selectedCategory = null
                            selectedSupplementCategory = null
                        }
                    )
                }
                items(ProductCategory.entries.toList(), key = { it.name }) { category ->
                    CategoryChip(
                        label = category.label,
                        selected = selectedPanel == PricePanel.PRODUCTS &&
                            selectedCategory == category,
                        onClick = {
                            selectedPanel = PricePanel.PRODUCTS
                            selectedCategory = category
                            selectedSupplementCategory = null
                        }
                    )
                }
                item {
                    CategoryChip(
                        label = "Majoration M / L",
                        selected = selectedPanel == PricePanel.PIZZA_SIZE_OFFSETS,
                        onClick = {
                            selectedPanel = PricePanel.PIZZA_SIZE_OFFSETS
                            selectedCategory = null
                            selectedSupplementCategory = null
                        }
                    )
                }
                items(
                    listOf(
                        ProductCategory.KLASSIK,
                        ProductCategory.DWICH,
                        ProductCategory.KREATIONS,
                        ProductCategory.BOWLS,
                        ProductCategory.ASSIETTES,
                        ProductCategory.TAKOS,
                        ProductCategory.FRITES
                    ),
                    key = { "supplement-${it.name}" }
                ) { category ->
                    CategoryChip(
                        label = "Suppl. ${category.label}",
                        selected = selectedPanel == PricePanel.PRODUCT_SUPPLEMENTS &&
                            selectedSupplementCategory == category,
                        onClick = {
                            selectedPanel = PricePanel.PRODUCT_SUPPLEMENTS
                            selectedCategory = null
                            selectedSupplementCategory = category
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            PriceTableHeader(
                secondColumnLabel = when {
                    selectedPanel == PricePanel.PIZZA_SIZE_OFFSETS -> "Majoration"
                    selectedPanel == PricePanel.PIZZA_SUPPLEMENTS ||
                        selectedPanel == PricePanel.PRODUCT_SUPPLEMENTS -> "Type"
                    else -> "Categorie"
                }
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                val showSizeOffsets = selectedPanel == PricePanel.PIZZA_SIZE_OFFSETS

                if (showSizeOffsets) {
                    items(
                        items = listOf(
                            PizzaCatalog.SIZE_OFFSET_SENIOR_KEY to "Taille M (+ sur base S)",
                            PizzaCatalog.SIZE_OFFSET_MEGA_KEY to "Taille L (+ sur base S)"
                        ),
                        key = { "size-offset-${it.first}" }
                    ) { (sizeKey, label) ->
                        SupplementPriceRow(
                            id = sizeKey,
                            label = label,
                            typeLabel = "Majoration",
                            priceCentimes = pizzaSizeOffsets[sizeKey]
                                ?: PizzaCatalog.defaultSizeOffsets.getValue(sizeKey),
                            allowZero = true,
                            onSavePrice = onUpdatePizzaSizeOffset
                        )
                        HorizontalDivider(color = WireframeBorder.copy(alpha = 0.2f))
                    }
                }

                when (selectedPanel) {
                    PricePanel.PIZZA_SIZE_OFFSETS -> Unit
                    PricePanel.PIZZA_SUPPLEMENTS -> {
                        items(
                            items = PizzaCatalog.ingredients,
                            key = { "supplement-${it.id}" },
                            contentType = { "supplement_price_row" }
                        ) { ingredient ->
                            SupplementPriceRow(
                                id = ingredient.id,
                                label = ingredient.label,
                                typeLabel = "Supplément pizza",
                                priceCentimes = pizzaSupplementPrices[ingredient.id]
                                    ?: PizzaCatalog.DEFAULT_SUPPLEMENT_PRICE_CENTIMES,
                                onSavePrice = onUpdatePizzaSupplementPrice
                            )
                            HorizontalDivider(color = WireframeBorder.copy(alpha = 0.2f))
                        }
                    }
                    PricePanel.PRODUCT_SUPPLEMENTS -> {
                        val category = selectedSupplementCategory!!
                        items(
                            items = ProductOptionCatalog.ingredientsFor(category),
                            key = { "supplement-${category.name}-${it.id}" },
                            contentType = { "supplement_price_row" }
                        ) { ingredient ->
                            val priceKey = ProductOptionCatalog.priceKey(category, ingredient.id)
                            SupplementPriceRow(
                                id = priceKey,
                                label = ingredient.label,
                                typeLabel = "Supplément ${category.label}",
                                priceCentimes = productSupplementPrices[priceKey]
                                    ?: ProductOptionCatalog.DEFAULT_SUPPLEMENT_PRICE_CENTIMES,
                                onSavePrice = onUpdateProductSupplementPrice
                            )
                            HorizontalDivider(color = WireframeBorder.copy(alpha = 0.2f))
                        }
                    }
                    PricePanel.PRODUCTS -> {
                        items(
                            items = visibleProducts,
                            key = { it.id },
                            contentType = { "product_price_row" }
                        ) { product ->
                            ProductPriceRow(
                                product = product,
                                onSavePrice = onUpdatePrice
                            )
                            HorizontalDivider(color = WireframeBorder.copy(alpha = 0.2f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(999.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) CaisseBlue else WireframeYellowBackground
        ),
        border = BorderStroke(1.dp, if (selected) CaisseBlueDark else WireframeBorder)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun PriceTableHeader(secondColumnLabel: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
        color = WireframeNavBackground,
        border = BorderStroke(1.dp, WireframeBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Article",
                modifier = Modifier.weight(1.4f),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = secondColumnLabel,
                modifier = Modifier.weight(0.8f),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Prix actuel",
                modifier = Modifier.weight(0.7f),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Nouveau prix",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(88.dp))
        }
    }
}

@Composable
private fun CompactPriceField(
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isError) MaterialTheme.colorScheme.error else WireframeBorder
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            color = MaterialTheme.colorScheme.onSurface
        ),
        cursorBrush = SolidColor(CaisseBlue),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier
            .width(PriceInputWidth)
            .height(40.dp)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 10.dp)
    )
}

@Composable
private fun EditablePriceRow(
    label: String,
    typeLabel: String,
    priceCentimes: Int,
    rowKey: String,
    onSavePrice: (Int) -> Unit,
    allowZero: Boolean = false
) {
    var priceInput by remember(rowKey) {
        mutableStateOf(formatEurosAmount(priceCentimes))
    }
    var isError by remember(rowKey) { mutableStateOf(false) }
    val savedLabel = remember(priceCentimes) { formatEurosAmount(priceCentimes) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(PriceRowHeight)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1.4f),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = typeLabel,
            modifier = Modifier.weight(0.8f),
            style = MaterialTheme.typography.bodyMedium,
            color = CaisseMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = formatEuros(priceCentimes),
            modifier = Modifier.weight(0.7f),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = WireframeGreenText,
            maxLines = 1
        )
        CompactPriceField(
            value = priceInput,
            onValueChange = {
                priceInput = it
                isError = false
            },
            isError = isError,
            modifier = Modifier.weight(1f, fill = false)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Card(
            onClick = {
                val parsed = parseEurosInput(priceInput)
                val invalid = parsed == null || (if (allowZero) parsed < 0 else parsed <= 0)
                if (invalid) {
                    isError = true
                } else {
                    onSavePrice(parsed!!)
                    isError = false
                    if (formatEurosAmount(parsed) != savedLabel) {
                        priceInput = formatEurosAmount(parsed)
                    }
                }
            },
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = CaisseBlue),
            border = BorderStroke(1.dp, CaisseBlueDark)
        ) {
            Text(
                text = "OK",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun SupplementPriceRow(
    id: String,
    label: String,
    typeLabel: String,
    priceCentimes: Int,
    onSavePrice: (id: String, priceCentimes: Int) -> Unit,
    allowZero: Boolean = false
) {
    EditablePriceRow(
        label = label,
        typeLabel = typeLabel,
        priceCentimes = priceCentimes,
        rowKey = id,
        allowZero = allowZero,
        onSavePrice = { cents -> onSavePrice(id, cents) }
    )
}

@Composable
private fun ProductPriceRow(
    product: Product,
    onSavePrice: (productId: String, priceCentimes: Int) -> Unit
) {
    EditablePriceRow(
        label = product.name,
        typeLabel = product.category.label,
        priceCentimes = product.priceCentimes,
        rowKey = product.id,
        onSavePrice = { cents -> onSavePrice(product.id, cents) }
    )
}

@Preview(showBackground = true, widthDp = 1280, heightDp = 800)
@Composable
private fun PriceManagementScreenPreview() {
    AMHCaisseTheme {
        PriceManagementScreen(
            products = SampleCatalog.products,
            onUpdatePrice = { _, _ -> },
            onBack = {}
        )
    }
}
