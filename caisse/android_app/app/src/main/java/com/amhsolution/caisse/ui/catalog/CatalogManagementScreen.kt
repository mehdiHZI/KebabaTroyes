package com.amhsolution.caisse.ui.catalog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.amhsolution.caisse.domain.catalog.ProductCatalogDefaults
import com.amhsolution.caisse.domain.formatEuros
import com.amhsolution.caisse.domain.formatEurosAmount
import com.amhsolution.caisse.domain.model.Product
import com.amhsolution.caisse.domain.model.ProductCategory
import com.amhsolution.caisse.domain.parseEurosInput
import com.amhsolution.caisse.ui.components.KebabaScreenHeader
import com.amhsolution.caisse.ui.theme.CaisseMuted
import com.amhsolution.caisse.ui.theme.KebabaDivider
import com.amhsolution.caisse.ui.theme.WireframeBorder
import com.amhsolution.caisse.ui.theme.WireframeGreenText
import kotlinx.coroutines.launch

@Composable
fun CatalogManagementScreen(
    products: List<Product>,
    onSave: suspend (Product) -> Result<Product>,
    onRefresh: suspend () -> Result<Int>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf<ProductCategory?>(null) }
    var editing by remember { mutableStateOf<Product?>(null) }
    var creating by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var statusOk by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val visible = remember(products, selectedCategory) {
        val sorted = products.sortedWith(
            compareBy<Product> { it.category.ordinal }.thenBy { it.name }
        )
        if (selectedCategory == null) sorted
        else sorted.filter { it.category == selectedCategory }
    }

    if (editing != null || creating) {
        val draft = editing ?: ProductCatalogDefaults.applyCategoryDefaults(
            Product(
                id = "",
                name = "",
                category = selectedCategory ?: ProductCategory.DWICH,
                priceCentimes = 1000
            )
        )
        ProductEditorScreen(
            initial = draft,
            isNew = creating,
            busy = busy,
            statusMessage = statusMessage,
            statusOk = statusOk,
            onCancel = {
                editing = null
                creating = false
                statusMessage = null
            },
            onSave = { product ->
                scope.launch {
                    busy = true
                    statusMessage = null
                    val result = onSave(product)
                    busy = false
                    result.onSuccess {
                        statusOk = true
                        statusMessage = "Enregistré sur le serveur."
                        editing = null
                        creating = false
                    }.onFailure { error ->
                        statusOk = false
                        statusMessage = error.message ?: "Échec enregistrement."
                    }
                }
            }
        )
        return
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
            KebabaScreenHeader(
                title = "Catalogue",
                subtitle = "Créer / modifier articles (sync VPS)",
                onBack = onBack,
                backLabel = "Retour",
                trailing = {
                    TextButton(
                        enabled = !busy,
                        onClick = {
                            scope.launch {
                                busy = true
                                statusMessage = null
                                val result = onRefresh()
                                busy = false
                                result.onSuccess { count ->
                                    statusOk = true
                                    statusMessage = "Catalogue synchronisé ($count articles)."
                                }.onFailure { error ->
                                    statusOk = false
                                    statusMessage = error.message ?: "Sync échouée."
                                }
                            }
                        }
                    ) {
                        Text("Sync")
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null },
                        label = { Text("Tous") }
                    )
                }
                items(ProductCategory.entries) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category.label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                statusMessage?.let { message ->
                    Text(
                        text = message,
                        color = if (statusOk) WireframeGreenText else MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                } ?: Spacer(modifier = Modifier.weight(1f))
                Button(
                    enabled = !busy,
                    onClick = {
                        statusMessage = null
                        creating = true
                    }
                ) {
                    Text("Nouvel article")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(visible, key = { it.id }) { product ->
                    CatalogProductRow(
                        product = product,
                        onClick = {
                            statusMessage = null
                            editing = product
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CatalogProductRow(
    product: Product,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(1.dp, KebabaDivider.copy(alpha = 0.65f)), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = product.name,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = buildString {
                    append(product.category.label)
                    append(" · ")
                    append(formatEuros(product.priceCentimes))
                    product.menuPriceCentimes?.let {
                        append(" / menu ")
                        append(formatEuros(it))
                    }
                    if (!product.enabled) append(" · désactivé")
                },
                color = CaisseMuted,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Text(
            text = if (product.enabled) "Actif" else "Off",
            color = if (product.enabled) WireframeGreenText else MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ProductEditorScreen(
    initial: Product,
    isNew: Boolean,
    busy: Boolean,
    statusMessage: String?,
    statusOk: Boolean,
    onCancel: () -> Unit,
    onSave: (Product) -> Unit
) {
    var name by remember(initial.id) { mutableStateOf(initial.name) }
    var category by remember(initial.id) { mutableStateOf(initial.category) }
    var priceText by remember(initial.id) {
        mutableStateOf(formatEurosAmount(initial.priceCentimes))
    }
    var menuEnabled by remember(initial.id) {
        mutableStateOf(initial.menuPriceCentimes != null)
    }
    var menuPriceText by remember(initial.id) {
        mutableStateOf(
            initial.menuPriceCentimes?.let { formatEurosAmount(it) }.orEmpty()
        )
    }
    var hasPain by remember(initial.id) { mutableStateOf(initial.hasPain) }
    var hasFrites by remember(initial.id) { mutableStateOf(initial.hasFrites) }
    var hasCrudites by remember(initial.id) { mutableStateOf(initial.hasCrudites) }
    var hasSupplements by remember(initial.id) { mutableStateOf(initial.hasSupplements) }
    var hasSauces by remember(initial.id) { mutableStateOf(initial.hasSauces) }
    var hasTakosMeats by remember(initial.id) { mutableStateOf(initial.hasTakosMeats) }
    var supportsDrinkPlus by remember(initial.id) { mutableStateOf(initial.supportsDrinkPlus) }
    var takosMeatCountText by remember(initial.id) {
        mutableStateOf(initial.takosMeatCount.takeIf { it > 0 }?.toString().orEmpty())
    }
    var enabled by remember(initial.id) { mutableStateOf(initial.enabled) }
    var localError by remember { mutableStateOf<String?>(null) }

    fun applyCategoryDefaults(next: ProductCategory) {
        category = next
        val flags = ProductCatalogDefaults.flagsFor(next)
        hasPain = flags.hasPain
        hasFrites = flags.hasFrites
        hasCrudites = flags.hasCrudites
        hasSupplements = flags.hasSupplements
        hasSauces = flags.hasSauces
        hasTakosMeats = flags.hasTakosMeats
        supportsDrinkPlus = flags.supportsDrinkPlus
        if (!flags.hasTakosMeats) takosMeatCountText = ""
        else if (takosMeatCountText.isBlank()) takosMeatCountText = "1"
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
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
                Text(
                    text = if (isNew) "Nouvel article" else "Modifier article",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onCancel, enabled = !busy) {
                    Text("Annuler")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = KebabaDivider.copy(alpha = 0.55f))
            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; localError = null },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Nom") }
                )

                Text("Catégorie", fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(ProductCategory.entries) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { applyCategoryDefaults(cat) },
                            label = { Text(cat.label) }
                        )
                    }
                }

                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it; localError = null },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Prix seul (€)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                FlagRow(
                    label = "Format Seul / Menu",
                    checked = menuEnabled,
                    onCheckedChange = {
                        menuEnabled = it
                        if (it && menuPriceText.isBlank()) {
                            val seul = parseEurosInput(priceText) ?: 0
                            menuPriceText = formatEurosAmount(seul + 300)
                        }
                    }
                )
                if (menuEnabled) {
                    OutlinedTextField(
                        value = menuPriceText,
                        onValueChange = { menuPriceText = it; localError = null },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Prix menu (€)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }

                Text("Options de personnalisation", fontWeight = FontWeight.SemiBold)
                FlagRow("Pain / dürüm", hasPain) { hasPain = it }
                FlagRow("Frites (étape menu)", hasFrites) { hasFrites = it }
                FlagRow("Crudités", hasCrudites) { hasCrudites = it }
                FlagRow("Suppléments", hasSupplements) { hasSupplements = it }
                FlagRow("Sauces", hasSauces) { hasSauces = it }
                FlagRow("Viandes Takos", hasTakosMeats) {
                    hasTakosMeats = it
                    if (it && takosMeatCountText.isBlank()) takosMeatCountText = "1"
                }
                if (hasTakosMeats) {
                    OutlinedTextField(
                        value = takosMeatCountText,
                        onValueChange = { takosMeatCountText = it.filter(Char::isDigit) },
                        modifier = Modifier.width(160.dp),
                        singleLine = true,
                        label = { Text("Nb viandes") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
                FlagRow("Upsell boisson +1 €", supportsDrinkPlus) { supportsDrinkPlus = it }
                FlagRow("Article actif", enabled) { enabled = it }

                (localError ?: statusMessage)?.let { message ->
                    Text(
                        text = message,
                        color = if (localError == null && statusOk) {
                            WireframeGreenText
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Button(
                enabled = !busy,
                modifier = Modifier.align(Alignment.End),
                onClick = {
                    val price = parseEurosInput(priceText)
                    if (name.isBlank()) {
                        localError = "Le nom est obligatoire."
                        return@Button
                    }
                    if (price == null || price <= 0) {
                        localError = "Prix seul invalide."
                        return@Button
                    }
                    val menuPrice = if (!menuEnabled) {
                        null
                    } else {
                        parseEurosInput(menuPriceText)
                    }
                    if (menuEnabled && (menuPrice == null || menuPrice <= 0)) {
                        localError = "Prix menu invalide."
                        return@Button
                    }
                    val meatCount = if (hasTakosMeats) {
                        takosMeatCountText.toIntOrNull()?.coerceIn(1, 5) ?: 1
                    } else {
                        0
                    }
                    onSave(
                        Product(
                            id = initial.id,
                            name = name.trim(),
                            category = category,
                            priceCentimes = price,
                            menuPriceCentimes = menuPrice,
                            hasPain = hasPain,
                            hasFrites = hasFrites,
                            hasCrudites = hasCrudites,
                            hasSupplements = hasSupplements,
                            hasSauces = hasSauces,
                            hasTakosMeats = hasTakosMeats,
                            supportsDrinkPlus = supportsDrinkPlus,
                            takosMeatCount = meatCount,
                            enabled = enabled,
                            updatedAtEpochMillis = System.currentTimeMillis()
                        )
                    )
                }
            ) {
                Text("Enregistrer")
            }
        }
    }
}

@Composable
private fun FlagRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
