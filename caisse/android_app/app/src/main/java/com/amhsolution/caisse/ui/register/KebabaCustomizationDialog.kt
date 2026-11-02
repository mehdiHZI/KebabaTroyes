package com.amhsolution.caisse.ui.register

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.amhsolution.caisse.domain.catalog.KebabaOption
import com.amhsolution.caisse.domain.catalog.KebabaOptionCatalog
import com.amhsolution.caisse.domain.formatEuros
import com.amhsolution.caisse.domain.model.IngredientSelection
import com.amhsolution.caisse.domain.model.Product
import com.amhsolution.caisse.domain.model.ProductCategory
import com.amhsolution.caisse.domain.model.ProductCustomization
import com.amhsolution.caisse.domain.model.SupplementSelection
import com.amhsolution.caisse.ui.theme.CaisseMuted
import com.amhsolution.caisse.ui.theme.KebabaBordeaux
import com.amhsolution.caisse.ui.theme.KebabaCard
import com.amhsolution.caisse.ui.theme.WireframeBorder
import com.amhsolution.caisse.ui.theme.WireframeGreenBackground
import com.amhsolution.caisse.ui.theme.WireframeGreenBorder
import com.amhsolution.caisse.ui.theme.WireframeGreenText
import com.amhsolution.caisse.ui.theme.WireframeNavBackground

private val CRUDITE_INGREDIENTS = listOf(
    "salade", "tomates", "oignon-rouge", "choux-rouge", "concombre", "carotte"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun KebabaCustomizationDialog(
    product: Product,
    onDismiss: () -> Unit,
    onConfirm: (ProductCustomization, Int) -> Unit
) {
    val hasPain = KebabaOptionCatalog.hasPain(product)
    val hasFrites = KebabaOptionCatalog.hasFritesEtape(product)
    val hasCrudites = KebabaOptionCatalog.hasCrudites(product)
    val hasSauces = KebabaOptionCatalog.hasSauces(product)
    val hasSupplements = KebabaOptionCatalog.hasSupplements(product)
    val hasTakosMeats = KebabaOptionCatalog.hasTakosMeats(product)
    val takosMeatCount = KebabaOptionCatalog.takosRequiredMeatCount(product)
    val supportsMenu = KebabaOptionCatalog.supportsMenuToggle(product)
    val supportsDrinkPlus = KebabaOptionCatalog.supportsDrinkPlus(product)
    val menuPrice = KebabaOptionCatalog.menuPriceCentimes(product)
    val isCategoryMenu = product.category == ProductCategory.MENUS

    var asMenu by remember(product.id) { mutableStateOf(false) }
    var painId by remember(product.id) {
        mutableStateOf(KebabaOptionCatalog.pains.firstOrNull()?.id.orEmpty())
    }
    var fritesId by remember(product.id) {
        mutableStateOf(KebabaOptionCatalog.fritesEtape.firstOrNull()?.id.orEmpty())
    }
    var menuDrinkId by remember(product.id) {
        mutableStateOf(KebabaOptionCatalog.menuDrinks.firstOrNull()?.id.orEmpty())
    }
    var cruditeIds by remember(product.id) { mutableStateOf(setOf("complet") + CRUDITE_INGREDIENTS) }
    var sauceIds by remember(product.id) { mutableStateOf(setOf<String>()) }
    var supplementIds by remember(product.id) { mutableStateOf(setOf<String>()) }
    var meatIds by remember(product.id) { mutableStateOf(setOf<String>()) }
    var takosMeatIds by remember(product.id) { mutableStateOf(setOf<String>()) }
    /** Bowl / assiette : null = sans boisson ; sinon id soft +1 €. */
    var bowlDrinkId by remember(product.id) { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    val showMenuFormatStep = supportsMenu && menuPrice != null
    val showFritesStep = hasFrites && (asMenu || isCategoryMenu)
    val showMenuDrinkStep = showFritesStep && !isCategoryMenu
    val showMeatPicker =
        KebabaOptionCatalog.SUP_VIANDE_ID in supplementIds || meatIds.isNotEmpty()

    val menuExtra =
        if (asMenu && menuPrice != null) menuPrice - product.priceCentimes else 0
    val fritesExtra =
        if (showFritesStep) {
            KebabaOptionCatalog.fritesEtape.firstOrNull { it.id == fritesId }?.priceCentimes ?: 0
        } else {
            0
        }
    val supplementsExtra = supplementIds
        .filterNot { it == KebabaOptionCatalog.SUP_VIANDE_ID }
        .sumOf { id ->
            KebabaOptionCatalog.supplements.firstOrNull { it.id == id }?.priceCentimes ?: 0
        } + meatIds.size * KebabaOptionCatalog.SUP_VIANDE_CENTS
    val saucesExtra = KebabaOptionCatalog.sauceExtraCentimes(sauceIds.size)
    val drinkExtra =
        if (supportsDrinkPlus && bowlDrinkId != null) {
            KebabaOptionCatalog.BOWL_ASSIETTE_DRINK_CENTS
        } else {
            0
        }
    val total = product.priceCentimes + menuExtra + fritesExtra + supplementsExtra + saucesExtra + drinkExtra

    var stepNumber = 0
    fun nextStepLabel(label: String): String {
        stepNumber += 1
        return "$stepNumber · $label"
    }

    fun toggleCrudite(id: String) {
        error = null
        cruditeIds = when (id) {
            "sans-crudites" ->
                if ("sans-crudites" in cruditeIds) emptySet() else setOf("sans-crudites")
            "complet" ->
                if ("complet" in cruditeIds) emptySet()
                else CRUDITE_INGREDIENTS.toSet() + "complet"
            else -> {
                var next = cruditeIds - "sans-crudites" - "complet"
                next = if (id in next) next - id else next + id
                if (next.containsAll(CRUDITE_INGREDIENTS)) next + "complet" else next
            }
        }
    }

    fun toggleSauce(id: String) {
        error = null
        sauceIds = if (id in sauceIds) sauceIds - id else sauceIds + id
    }

    fun toggleSupplement(id: String) {
        error = null
        if (id == KebabaOptionCatalog.SUP_VIANDE_ID) {
            if (id in supplementIds) {
                supplementIds = supplementIds - id
                meatIds = emptySet()
            } else {
                supplementIds = supplementIds + id
            }
            return
        }
        supplementIds = if (id in supplementIds) supplementIds - id else supplementIds + id
    }

    fun toggleMeat(id: String) {
        error = null
        meatIds = if (id in meatIds) meatIds - id else meatIds + id
        supplementIds = if (meatIds.isEmpty()) {
            supplementIds - KebabaOptionCatalog.SUP_VIANDE_ID
        } else {
            supplementIds + KebabaOptionCatalog.SUP_VIANDE_ID
        }
    }

    fun toggleTakosMeat(id: String) {
        error = null
        takosMeatIds = when {
            id in takosMeatIds -> takosMeatIds - id
            takosMeatIds.size >= takosMeatCount -> takosMeatIds
            else -> takosMeatIds + id
        }
    }

    fun validate(): Boolean {
        if (hasPain && painId.isBlank()) {
            error = "Choisissez un pain."
            return false
        }
        if (hasTakosMeats && takosMeatIds.size != takosMeatCount) {
            error = "Choisissez $takosMeatCount viande${if (takosMeatCount > 1) "s" else ""}."
            return false
        }
        if (hasCrudites && cruditeIds.isEmpty()) {
            error = "Sélectionnez vos garnitures (ou sans crudités)."
            return false
        }
        if (KebabaOptionCatalog.SUP_VIANDE_ID in supplementIds && meatIds.isEmpty()) {
            error = "Choisissez au moins une viande en supplément."
            return false
        }
        if (showFritesStep && fritesId.isBlank()) {
            error = "Choisissez vos frites."
            return false
        }
        if (showMenuDrinkStep && menuDrinkId.isBlank()) {
            error = "Choisissez une boisson pour le menu."
            return false
        }
        if (hasSauces && sauceIds.isEmpty()) {
            error = "Choisissez au moins une sauce."
            return false
        }
        error = null
        return true
    }

    fun buildCustomization(): ProductCustomization {
        val added = mutableListOf<SupplementSelection>()
        if (hasPain) {
            val pain = KebabaOptionCatalog.pains.first { it.id == painId }
            added += SupplementSelection(pain.id, "Pain ${pain.label}", 0)
        }
        if (asMenu && menuExtra > 0) {
            added += SupplementSelection(
                KebabaOptionCatalog.MENU_OPTION_ID,
                "Menu (frites + boisson)",
                menuExtra
            )
        }
        takosMeatIds.forEach { id ->
            val meat = KebabaOptionCatalog.takosMeats.first { it.id == id }
            added += SupplementSelection(
                id = "takos-${meat.id}",
                label = meat.label,
                priceCentimes = 0
            )
        }
        supplementIds
            .filterNot { it == KebabaOptionCatalog.SUP_VIANDE_ID }
            .forEach { id ->
                val s = KebabaOptionCatalog.supplements.first { it.id == id }
                added += SupplementSelection(s.id, s.label, s.priceCentimes)
            }
        meatIds.forEach { id ->
            val meat = KebabaOptionCatalog.supplementMeats.first { it.id == id }
            added += SupplementSelection(
                id = "sup-viande-${meat.id}",
                label = "Viande ${meat.label}",
                priceCentimes = KebabaOptionCatalog.SUP_VIANDE_CENTS
            )
        }
        if (showFritesStep) {
            val f = KebabaOptionCatalog.fritesEtape.first { it.id == fritesId }
            added += SupplementSelection(f.id, "Frites ${f.label}", f.priceCentimes)
        }
        sauceIds.forEachIndexed { index, id ->
            val s = KebabaOptionCatalog.sauces.first { it.id == id }
            val extra = if (index >= KebabaOptionCatalog.MAX_INCLUDED_SAUCES) {
                KebabaOptionCatalog.EXTRA_SAUCE_CENTS
            } else {
                0
            }
            added += SupplementSelection(
                s.id,
                if (extra > 0) "${s.label} (+${formatEuros(extra)})" else s.label,
                extra
            )
        }
        val removed = if ("sans-crudites" in cruditeIds) {
            listOf(IngredientSelection("sans-crudites", "Sans les crudités"))
        } else {
            CRUDITE_INGREDIENTS
                .filter { it !in cruditeIds }
                .map { id ->
                    val label = KebabaOptionCatalog.crudites.first { it.id == id }.label
                    IngredientSelection(id, label)
                }
        }
        val drink = when {
            showMenuDrinkStep -> {
                val d = KebabaOptionCatalog.menuDrinks.first { it.id == menuDrinkId }
                IngredientSelection(d.id, d.label)
            }
            supportsDrinkPlus && bowlDrinkId != null -> {
                val d = KebabaOptionCatalog.menuDrinks.first { it.id == bowlDrinkId }
                IngredientSelection(
                    id = d.id,
                    label = "${d.label} (+${formatEuros(KebabaOptionCatalog.BOWL_ASSIETTE_DRINK_CENTS)})"
                )
            }
            else -> null
        }
        return ProductCustomization(
            removedIngredients = removed,
            addedSupplements = added,
            includedDrink = drink
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 720.dp)
                .fillMaxWidth(0.95f),
            color = KebabaCard,
            border = BorderStroke(1.5.dp, KebabaBordeaux)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = KebabaBordeaux
                )
                Text(
                    text = "Personnalisation Kebaba",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CaisseMuted
                )

                if (showMenuFormatStep && menuPrice != null) {
                    StepTitle(nextStepLabel("Seul ou Menu"))
                    Text(
                        text = "Seul ${formatEuros(product.priceCentimes)} · Menu ${formatEuros(menuPrice)} (frites + boisson)",
                        style = MaterialTheme.typography.bodySmall,
                        color = CaisseMuted
                    )
                    OptionChips(
                        options = KebabaOptionCatalog.formatOptions,
                        selectedIds = setOf(if (asMenu) "format-menu" else "format-seul"),
                        onToggle = { id ->
                            error = null
                            asMenu = id == "format-menu"
                        }
                    )
                }

                if (hasPain) {
                    StepTitle(nextStepLabel("Choisi ton pain"))
                    OptionChips(
                        options = KebabaOptionCatalog.pains,
                        selectedIds = setOf(painId),
                        onToggle = { painId = it }
                    )
                }

                if (hasTakosMeats) {
                    StepTitle(nextStepLabel("Choisi tes viandes"))
                    Text(
                        text = "${takosMeatIds.size} / $takosMeatCount sélectionnée${if (takosMeatCount > 1) "s" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = CaisseMuted
                    )
                    OptionChips(
                        options = KebabaOptionCatalog.takosMeats,
                        selectedIds = takosMeatIds,
                        onToggle = { toggleTakosMeat(it) }
                    )
                }

                if (hasCrudites) {
                    StepTitle(nextStepLabel("Choisi tes garnitures"))
                    OptionChips(
                        options = KebabaOptionCatalog.crudites,
                        selectedIds = cruditeIds,
                        onToggle = { toggleCrudite(it) }
                    )
                }

                if (hasSupplements) {
                    StepTitle(nextStepLabel("Choisi tes suppléments"))
                    OptionChips(
                        options = KebabaOptionCatalog.supplements,
                        selectedIds = supplementIds,
                        onToggle = { toggleSupplement(it) },
                        showPrice = true
                    )
                    if (showMeatPicker) {
                        Text(
                            text = "Viandes (+${formatEuros(KebabaOptionCatalog.SUP_VIANDE_CENTS)} chacune · autant que tu veux)",
                            style = MaterialTheme.typography.bodySmall,
                            color = CaisseMuted
                        )
                        OptionChips(
                            options = KebabaOptionCatalog.supplementMeats,
                            selectedIds = meatIds,
                            onToggle = { toggleMeat(it) },
                            showPrice = true
                        )
                    }
                }

                if (showFritesStep) {
                    StepTitle(nextStepLabel("Choisi tes frites"))
                    OptionChips(
                        options = KebabaOptionCatalog.fritesEtape,
                        selectedIds = setOf(fritesId),
                        onToggle = { fritesId = it },
                        showPrice = true
                    )
                }

                if (showMenuDrinkStep) {
                    StepTitle(nextStepLabel("Choisi ta boisson"))
                    OptionChips(
                        options = KebabaOptionCatalog.menuDrinks,
                        selectedIds = setOf(menuDrinkId),
                        onToggle = { menuDrinkId = it }
                    )
                }

                if (hasSauces) {
                    StepTitle(nextStepLabel("Choisi 2 sauces maximum"))
                    Text(
                        text = "${KebabaOptionCatalog.MAX_INCLUDED_SAUCES} incluses · +${formatEuros(KebabaOptionCatalog.EXTRA_SAUCE_CENTS)} ensuite",
                        style = MaterialTheme.typography.bodySmall,
                        color = CaisseMuted
                    )
                    OptionChips(
                        options = KebabaOptionCatalog.sauces,
                        selectedIds = sauceIds,
                        onToggle = { toggleSauce(it) }
                    )
                }

                if (supportsDrinkPlus) {
                    StepTitle(nextStepLabel("Boisson"))
                    Text(
                        text = "Optionnelle · +${formatEuros(KebabaOptionCatalog.BOWL_ASSIETTE_DRINK_CENTS)} si tu en choisis une",
                        style = MaterialTheme.typography.bodySmall,
                        color = CaisseMuted
                    )
                    OptionChips(
                        options = listOf(
                            KebabaOption("sans-boisson", "Sans boisson", 0)
                        ) + KebabaOptionCatalog.menuDrinks.map {
                            it.copy(priceCentimes = KebabaOptionCatalog.BOWL_ASSIETTE_DRINK_CENTS)
                        },
                        selectedIds = setOf(bowlDrinkId ?: "sans-boisson"),
                        onToggle = { id ->
                            error = null
                            bowlDrinkId = if (id == "sans-boisson") null else id
                        },
                        showPrice = true
                    )
                }

                error?.let {
                    Text(it, color = Color(0xFFB42318), fontWeight = FontWeight.SemiBold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
                ) {
                    TextButton(onClick = onDismiss) { Text("Annuler") }
                    TextButton(
                        onClick = {
                            if (!validate()) return@TextButton
                            onConfirm(buildCustomization(), total)
                        }
                    ) {
                        Text(
                            "Ajouter · ${formatEuros(total)}",
                            fontWeight = FontWeight.Bold,
                            color = KebabaBordeaux
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StepTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = KebabaBordeaux
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OptionChips(
    options: List<KebabaOption>,
    selectedIds: Set<String>,
    onToggle: (String) -> Unit,
    showPrice: Boolean = false
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            val selected = option.id in selectedIds
            Card(
                onClick = { onToggle(option.id) },
                colors = CardDefaults.cardColors(
                    containerColor = if (selected) WireframeGreenBackground else WireframeNavBackground
                ),
                border = BorderStroke(
                    1.dp,
                    if (selected) WireframeGreenBorder else WireframeBorder.copy(alpha = 0.35f)
                )
            ) {
                Text(
                    text = buildString {
                        append(option.label)
                        if (showPrice && option.priceCentimes > 0) {
                            append(" +")
                            append(formatEuros(option.priceCentimes))
                        }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    color = if (selected) WireframeGreenText else KebabaBordeaux,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(2.dp))
}
