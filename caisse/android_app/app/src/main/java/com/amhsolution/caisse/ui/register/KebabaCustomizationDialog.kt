package com.amhsolution.caisse.ui.register

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.amhsolution.caisse.R
import com.amhsolution.caisse.domain.catalog.KebabaOption
import com.amhsolution.caisse.domain.catalog.KebabaOptionCatalog
import com.amhsolution.caisse.domain.formatEuros
import com.amhsolution.caisse.domain.model.IngredientSelection
import com.amhsolution.caisse.domain.model.Product
import com.amhsolution.caisse.domain.model.ProductCategory
import com.amhsolution.caisse.domain.model.ProductCustomization
import com.amhsolution.caisse.domain.model.SupplementSelection
import com.amhsolution.caisse.ui.theme.CaisseRed
import com.amhsolution.caisse.ui.theme.KebabaBordeaux
import com.amhsolution.caisse.ui.theme.KebabaBordeauxDeep
import com.amhsolution.caisse.ui.theme.KebabaCard
import com.amhsolution.caisse.ui.theme.KebabaCream
import com.amhsolution.caisse.ui.theme.KebabaCreamDeep
import com.amhsolution.caisse.ui.theme.KebabaFezRed
import com.amhsolution.caisse.ui.theme.KebabaInk
import com.amhsolution.caisse.ui.theme.KebabaInkMuted

private val CRUDITE_INGREDIENTS = listOf(
    "salade", "tomates", "oignon-rouge", "choux-rouge", "concombre", "carotte"
)

private val DialogShape = RoundedCornerShape(20.dp)
private val ChipShape = RoundedCornerShape(14.dp)

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
    var cruditeIds by remember(product.id) { mutableStateOf(setOf<String>()) }
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

    val stepLabels = buildList {
        if (showMenuFormatStep && menuPrice != null) add("Seul ou Menu")
        if (hasPain) add("Choisi ton pain")
        if (hasTakosMeats) add("Choisi tes viandes")
        if (hasCrudites) add("Choisi tes garnitures")
        if (hasSupplements) add("Choisi tes suppléments")
        if (showFritesStep) add("Choisi tes frites")
        if (showMenuDrinkStep) add("Choisi ta boisson")
        if (hasSauces) add("Choisi tes sauces")
        if (supportsDrinkPlus) add("Boisson")
    }
    val stepNumberByLabel = stepLabels.mapIndexed { index, label -> label to (index + 1) }.toMap()
    fun stepNo(label: String): Int = stepNumberByLabel.getValue(label)

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
                .widthIn(max = 760.dp)
                .fillMaxWidth(0.96f),
            shape = DialogShape,
            color = KebabaCream,
            border = BorderStroke(1.dp, KebabaBordeaux.copy(alpha = 0.18f)),
            shadowElevation = 10.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                DialogBrandHeader(productName = product.name)

                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 22.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    if (showMenuFormatStep && menuPrice != null) {
                        StepBlock(
                            number = stepNo("Seul ou Menu"),
                            title = "Seul ou Menu",
                            hint = "Seul ${formatEuros(product.priceCentimes)} · Menu ${formatEuros(menuPrice)} (frites + boisson)"
                        ) {
                            OptionChips(
                                options = KebabaOptionCatalog.formatOptions,
                                selectedIds = setOf(if (asMenu) "format-menu" else "format-seul"),
                                onToggle = { id ->
                                    error = null
                                    asMenu = id == "format-menu"
                                }
                            )
                        }
                    }

                    if (hasPain) {
                        StepBlock(number = stepNo("Choisi ton pain"), title = "Choisi ton pain") {
                            OptionChips(
                                options = KebabaOptionCatalog.pains,
                                selectedIds = setOf(painId),
                                onToggle = { painId = it }
                            )
                        }
                    }

                    if (hasTakosMeats) {
                        StepBlock(
                            number = stepNo("Choisi tes viandes"),
                            title = "Choisi tes viandes",
                            hint = "${takosMeatIds.size} / $takosMeatCount sélectionnée${if (takosMeatCount > 1) "s" else ""}"
                        ) {
                            OptionChips(
                                options = KebabaOptionCatalog.takosMeats,
                                selectedIds = takosMeatIds,
                                onToggle = { toggleTakosMeat(it) }
                            )
                        }
                    }

                    if (hasCrudites) {
                        StepBlock(
                            number = stepNo("Choisi tes garnitures"),
                            title = "Choisi tes garnitures",
                            hint = "Aucune pré-sélection — choisis ce que tu veux"
                        ) {
                            OptionChips(
                                options = KebabaOptionCatalog.crudites,
                                selectedIds = cruditeIds,
                                onToggle = { toggleCrudite(it) }
                            )
                        }
                    }

                    if (hasSupplements) {
                        StepBlock(
                            number = stepNo("Choisi tes suppléments"),
                            title = "Choisi tes suppléments",
                            hint = "Optionnel"
                        ) {
                            OptionChips(
                                options = KebabaOptionCatalog.supplements,
                                selectedIds = supplementIds,
                                onToggle = { toggleSupplement(it) },
                                showPrice = true
                            )
                            if (showMeatPicker) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Viandes (+${formatEuros(KebabaOptionCatalog.SUP_VIANDE_CENTS)} chacune)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = KebabaInkMuted
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OptionChips(
                                    options = KebabaOptionCatalog.supplementMeats,
                                    selectedIds = meatIds,
                                    onToggle = { toggleMeat(it) },
                                    showPrice = true
                                )
                            }
                        }
                    }

                    if (showFritesStep) {
                        StepBlock(number = stepNo("Choisi tes frites"), title = "Choisi tes frites") {
                            OptionChips(
                                options = KebabaOptionCatalog.fritesEtape,
                                selectedIds = setOf(fritesId),
                                onToggle = { fritesId = it },
                                showPrice = true
                            )
                        }
                    }

                    if (showMenuDrinkStep) {
                        StepBlock(number = stepNo("Choisi ta boisson"), title = "Choisi ta boisson") {
                            OptionChips(
                                options = KebabaOptionCatalog.menuDrinks,
                                selectedIds = setOf(menuDrinkId),
                                onToggle = { menuDrinkId = it }
                            )
                        }
                    }

                    if (hasSauces) {
                        StepBlock(
                            number = stepNo("Choisi tes sauces"),
                            title = "Choisi tes sauces",
                            hint = "Optionnel · ${KebabaOptionCatalog.MAX_INCLUDED_SAUCES} incluses · +${formatEuros(KebabaOptionCatalog.EXTRA_SAUCE_CENTS)} ensuite"
                        ) {
                            OptionChips(
                                options = KebabaOptionCatalog.sauces,
                                selectedIds = sauceIds,
                                onToggle = { toggleSauce(it) }
                            )
                        }
                    }

                    if (supportsDrinkPlus) {
                        StepBlock(
                            number = stepNo("Boisson"),
                            title = "Boisson",
                            hint = "Optionnelle · +${formatEuros(KebabaOptionCatalog.BOWL_ASSIETTE_DRINK_CENTS)} si tu en choisis une"
                        ) {
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
                    }

                    error?.let {
                        Text(
                            text = it,
                            color = CaisseRed,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                }

                DialogFooter(
                    total = total,
                    onDismiss = onDismiss,
                    onConfirm = {
                        if (!validate()) return@DialogFooter
                        onConfirm(buildCustomization(), total)
                    }
                )
            }
        }
    }
}

@Composable
private fun DialogBrandHeader(productName: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(KebabaCard)
            .padding(horizontal = 22.dp, vertical = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.logo_kebaba_mark),
                contentDescription = "Logo Kebaba",
                modifier = Modifier.size(width = 56.dp, height = 40.dp),
                contentScale = ContentScale.Fit
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = productName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = KebabaInk
                )
                Text(
                    text = "Personnalisation",
                    style = MaterialTheme.typography.bodyMedium,
                    color = KebabaInkMuted
                )
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(color = KebabaBordeaux.copy(alpha = 0.12f))
    }
}

@Composable
private fun StepBlock(
    number: Int,
    title: String,
    hint: String? = null,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(KebabaFezRed),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = number.toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = KebabaInk
            )
        }
        hint?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = KebabaInkMuted,
                modifier = Modifier.padding(start = 38.dp)
            )
        }
        Box(modifier = Modifier.padding(start = 38.dp)) {
            content()
        }
    }
}

@Composable
private fun DialogFooter(
    total: Int,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(KebabaCard)
            .padding(horizontal = 22.dp, vertical = 14.dp)
    ) {
        HorizontalDivider(
            color = KebabaBordeaux.copy(alpha = 0.12f),
            modifier = Modifier.padding(bottom = 14.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.height(48.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, KebabaBordeaux.copy(alpha = 0.35f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = KebabaInk)
            ) {
                Text("Annuler", fontWeight = FontWeight.SemiBold)
            }
            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = KebabaBordeaux,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Ajouter · ${formatEuros(total)}",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
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
                shape = ChipShape,
                colors = CardDefaults.cardColors(
                    containerColor = if (selected) KebabaBordeaux else KebabaCard
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (selected) KebabaBordeauxDeep else KebabaCreamDeep
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = if (selected) 2.dp else 0.dp
                )
            ) {
                Text(
                    text = buildString {
                        append(option.label)
                        if (showPrice && option.priceCentimes > 0) {
                            append("  +")
                            append(formatEuros(option.priceCentimes))
                        }
                    },
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    color = if (selected) Color.White else KebabaInk,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
