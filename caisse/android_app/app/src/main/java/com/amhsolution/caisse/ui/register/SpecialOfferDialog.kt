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
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.amhsolution.caisse.domain.catalog.PizzaBase
import com.amhsolution.caisse.domain.catalog.PizzaCatalog
import com.amhsolution.caisse.domain.catalog.PizzaSize
import com.amhsolution.caisse.domain.catalog.ProductOptionCatalog
import com.amhsolution.caisse.domain.formatEuros
import com.amhsolution.caisse.domain.model.PizzaCustomization
import com.amhsolution.caisse.domain.model.PizzaIngredientSelection
import com.amhsolution.caisse.domain.model.PizzaSupplementSelection
import com.amhsolution.caisse.domain.model.Product
import com.amhsolution.caisse.domain.model.SpecialOfferCustomization
import com.amhsolution.caisse.domain.model.SpecialOfferPizzaSelection
import com.amhsolution.caisse.ui.theme.CaisseBlue
import com.amhsolution.caisse.ui.theme.CaisseBlueDark
import com.amhsolution.caisse.ui.theme.CaisseMuted
import com.amhsolution.caisse.ui.theme.WireframeBorder
import com.amhsolution.caisse.ui.theme.WireframeGreenBackground
import com.amhsolution.caisse.ui.theme.WireframeGreenBorder
import com.amhsolution.caisse.ui.theme.WireframeGreenText
import com.amhsolution.caisse.ui.theme.WireframeNavBackground

private data class OfferPizzaSlotState(
    val product: Product,
    val base: PizzaBase,
    val removedIngredientIds: Set<String> = emptySet(),
    val selectedSupplementIds: Set<String> = emptySet()
)

@Composable
fun SpecialOfferDialog(
    product: Product,
    supplementPrices: Map<String, Int>,
    onDismiss: () -> Unit,
    onConfirm: (SpecialOfferCustomization) -> Unit
) {
    val pizzaCount = if (product.id.contains("trio")) 3 else 2
    val offerSize = if (product.id.contains("senior")) PizzaSize.SENIOR else PizzaSize.JUNIOR
    val pizzaSizeLabel = if (offerSize == PizzaSize.SENIOR) "Senior" else "Junior"
    val requiresBottleChoice = ProductOptionCatalog.specialOfferIncludesBottle(product.id)
    val requiresCanChoice = ProductOptionCatalog.specialOfferIncludesCans(product.id)
    val bottleOptions = remember { ProductOptionCatalog.specialOfferBottleDrinkOptions() }
    val canOptions = remember { ProductOptionCatalog.specialOfferCanDrinkOptions() }
    var selectedBottleId by remember(product.id) { mutableStateOf<String?>(null) }
    var selectedCanIds by remember(product.id) {
        mutableStateOf(listOf<String?>(null, null))
    }
    var activeCanSlot by remember(product.id) { mutableIntStateOf(0) }
    val selectedBottle = bottleOptions.firstOrNull { it.id == selectedBottleId }
    val selectedCans = selectedCanIds.map { id -> canOptions.firstOrNull { it.id == id } }
    val drinkLabel = when {
        requiresBottleChoice -> selectedBottle?.label.orEmpty()
        requiresCanChoice -> {
            val labels = selectedCans.mapNotNull { it?.label }
            when {
                labels.size < 2 -> ""
                labels[0] == labels[1] -> "2× ${labels[0]}"
                else -> labels.joinToString(" + ")
            }
        }
        else -> ""
    }
    val drinkSummary = when {
        requiresBottleChoice -> selectedBottle?.label ?: "bouteille 1,5 L à choisir"
        requiresCanChoice -> drinkLabel.ifBlank { "2 canettes à choisir" }
        else -> drinkLabel
    }
    val canConfirmDrink = when {
        requiresBottleChoice -> selectedBottle != null
        requiresCanChoice -> selectedCans.all { it != null }
        else -> true
    }
    val catalogPizzas = remember { PizzaCatalog.products }
    var activeSlot by remember(product.id) { mutableIntStateOf(0) }
    var slots by remember(product.id) {
        mutableStateOf(
            List(pizzaCount) { index ->
                val pizza = catalogPizzas.getOrElse(index) { catalogPizzas.first() }
                val recipe = PizzaCatalog.recipeFor(pizza.id)
                OfferPizzaSlotState(
                    product = pizza,
                    base = recipe?.defaultBase ?: PizzaBase.TOMATE
                )
            }
        )
    }
    val active = slots[activeSlot]
    val recipe = remember(active.product.id) { PizzaCatalog.recipeFor(active.product.id) }
    val defaultIngredients = remember(recipe) {
        recipe?.ingredientIds?.mapNotNull(PizzaCatalog::ingredientFor).orEmpty()
    }
    val availableSupplements = PizzaCatalog.ingredients
    val supplementsTotal = slots.sumOf { slot ->
        slot.selectedSupplementIds.sumOf { id ->
            supplementPrices[id] ?: PizzaCatalog.DEFAULT_SUPPLEMENT_PRICE_CENTIMES
        }
    }
    val totalCentimes = product.priceCentimes + supplementsTotal
    val canConfirm = canConfirmDrink

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .widthIn(max = 820.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, WireframeBorder)
        ) {
            Column(Modifier.fillMaxSize().padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            product.name.substringBefore(" ·"),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "$pizzaCount pizzas $pizzaSizeLabel · $drinkSummary",
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
                    Modifier.padding(vertical = 10.dp),
                    color = WireframeBorder.copy(alpha = 0.25f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    slots.forEachIndexed { index, slot ->
                        Card(
                            onClick = { activeSlot = index },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(9.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (activeSlot == index) {
                                    WireframeGreenBackground
                                } else {
                                    WireframeNavBackground
                                }
                            ),
                            border = BorderStroke(
                                if (activeSlot == index) 2.dp else 1.dp,
                                if (activeSlot == index) WireframeGreenBorder else WireframeBorder
                            )
                        ) {
                            Column(
                                Modifier.fillMaxSize().padding(horizontal = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    "Pizza ${index + 1}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CaisseMuted
                                )
                                Text(
                                    slot.product.name,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier.weight(1f).padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(
                            "Choisir la pizza ${activeSlot + 1}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    items(catalogPizzas.chunked(OfferPizzaNameColumnCount)) { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(7.dp)
                        ) {
                            row.forEach { pizza ->
                                val selected = active.product.id == pizza.id
                                Card(
                                    onClick = {
                                        val newRecipe = PizzaCatalog.recipeFor(pizza.id)
                                        slots = slots.toMutableList().also {
                                            it[activeSlot] = OfferPizzaSlotState(
                                                product = pizza,
                                                base = newRecipe?.defaultBase ?: PizzaBase.TOMATE
                                            )
                                        }
                                    },
                                    modifier = Modifier.weight(1f).height(44.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (selected) {
                                            WireframeGreenBackground
                                        } else {
                                            WireframeNavBackground
                                        }
                                    ),
                                    border = BorderStroke(
                                        if (selected) 2.dp else 1.dp,
                                        if (selected) WireframeGreenBorder else WireframeBorder
                                    )
                                ) {
                                    Box(
                                        Modifier.fillMaxSize().padding(4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            pizza.name,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            textAlign = TextAlign.Center,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                            repeat(OfferPizzaNameColumnCount - row.size) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }

                    item {
                        Text(
                            "Base",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PizzaBase.entries.forEach { base ->
                                val selected = active.base == base
                                Card(
                                    onClick = {
                                        slots = slots.toMutableList().also {
                                            it[activeSlot] = active.copy(base = base)
                                        }
                                    },
                                    modifier = Modifier.weight(1f).height(44.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (selected) {
                                            WireframeGreenBackground
                                        } else {
                                            WireframeNavBackground
                                        }
                                    ),
                                    border = BorderStroke(
                                        if (selected) 2.dp else 1.dp,
                                        if (selected) WireframeGreenBorder else WireframeBorder
                                    )
                                ) {
                                    Box(
                                        Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(base.label, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            "Composition incluse",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                        Text(
                            "Touchez un ingrédient pour le retirer ou le remettre.",
                            style = MaterialTheme.typography.bodySmall,
                            color = CaisseMuted
                        )
                    }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(7.dp)
                        ) {
                            defaultIngredients.forEach { ingredient ->
                                val included = ingredient.id !in active.removedIngredientIds
                                Card(
                                    onClick = {
                                        val next = if (ingredient.id in active.removedIngredientIds) {
                                            active.removedIngredientIds - ingredient.id
                                        } else {
                                            active.removedIngredientIds + ingredient.id
                                        }
                                        slots = slots.toMutableList().also {
                                            it[activeSlot] = active.copy(removedIngredientIds = next)
                                        }
                                    },
                                    modifier = Modifier.weight(1f).height(44.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (included) {
                                            WireframeGreenBackground
                                        } else {
                                            Color.White
                                        }
                                    ),
                                    border = BorderStroke(
                                        1.dp,
                                        if (included) WireframeGreenBorder else WireframeBorder
                                    )
                                ) {
                                    Box(
                                        Modifier.fillMaxSize().padding(4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            ingredient.label,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (included) WireframeGreenText else CaisseMuted,
                                            textAlign = TextAlign.Center,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (requiresBottleChoice) {
                        item {
                            Text(
                                "Boisson 1,5 L",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                bottleOptions.forEach { drink ->
                                    val selected = selectedBottleId == drink.id
                                    Card(
                                        onClick = { selectedBottleId = drink.id },
                                        modifier = Modifier.weight(1f).height(48.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (selected) {
                                                WireframeGreenBackground
                                            } else {
                                                WireframeNavBackground
                                            }
                                        ),
                                        border = BorderStroke(
                                            if (selected) 2.dp else 1.dp,
                                            if (selected) WireframeGreenBorder else WireframeBorder
                                        )
                                    ) {
                                        Box(
                                            Modifier.fillMaxSize().padding(horizontal = 6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                drink.label,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (requiresCanChoice) {
                        item {
                            Text(
                                "2 canettes",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                            Text(
                                "Choisissez chaque canette.",
                                style = MaterialTheme.typography.bodySmall,
                                color = CaisseMuted
                            )
                        }
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                selectedCans.forEachIndexed { index, can ->
                                    Card(
                                        onClick = { activeCanSlot = index },
                                        modifier = Modifier.weight(1f).height(52.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (activeCanSlot == index) {
                                                WireframeGreenBackground
                                            } else {
                                                WireframeNavBackground
                                            }
                                        ),
                                        border = BorderStroke(
                                            if (activeCanSlot == index) 2.dp else 1.dp,
                                            if (activeCanSlot == index) {
                                                WireframeGreenBorder
                                            } else {
                                                WireframeBorder
                                            }
                                        )
                                    ) {
                                        Column(
                                            Modifier.fillMaxSize().padding(horizontal = 6.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                "Canette ${index + 1}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = CaisseMuted
                                            )
                                            Text(
                                                can?.label ?: "À choisir",
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        items(canOptions.chunked(OfferPizzaNameColumnCount)) { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(7.dp)
                            ) {
                                row.forEach { drink ->
                                    val selected = selectedCanIds[activeCanSlot] == drink.id
                                    Card(
                                        onClick = {
                                            val next = selectedCanIds.toMutableList().also {
                                                it[activeCanSlot] = drink.id
                                            }
                                            selectedCanIds = next
                                            if (activeCanSlot == 0 && next[1] == null) {
                                                activeCanSlot = 1
                                            }
                                        },
                                        modifier = Modifier.weight(1f).height(44.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (selected) {
                                                WireframeGreenBackground
                                            } else {
                                                WireframeNavBackground
                                            }
                                        ),
                                        border = BorderStroke(
                                            if (selected) 2.dp else 1.dp,
                                            if (selected) WireframeGreenBorder else WireframeBorder
                                        )
                                    ) {
                                        Box(
                                            Modifier.fillMaxSize().padding(4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                drink.label,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                textAlign = TextAlign.Center,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                                repeat(OfferPizzaNameColumnCount - row.size) {
                                    Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    item {
                        val supplementTotalForSlot = active.selectedSupplementIds.sumOf { id ->
                            supplementPrices[id] ?: PizzaCatalog.DEFAULT_SUPPLEMENT_PRICE_CENTIMES
                        }
                        Text(
                            "Suppléments · ${active.selectedSupplementIds.size}/${availableSupplements.size} · " +
                                "+${formatEuros(supplementTotalForSlot)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                    items(availableSupplements.chunked(OfferPizzaNameColumnCount)) { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(7.dp)
                        ) {
                            row.forEach { ingredient ->
                                val selected = ingredient.id in active.selectedSupplementIds
                                val price = supplementPrices[ingredient.id]
                                    ?: PizzaCatalog.DEFAULT_SUPPLEMENT_PRICE_CENTIMES
                                Card(
                                    onClick = {
                                        val next = if (ingredient.id in active.selectedSupplementIds) {
                                            active.selectedSupplementIds - ingredient.id
                                        } else {
                                            active.selectedSupplementIds + ingredient.id
                                        }
                                        slots = slots.toMutableList().also {
                                            it[activeSlot] = active.copy(selectedSupplementIds = next)
                                        }
                                    },
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (selected) {
                                            WireframeGreenBackground
                                        } else {
                                            WireframeNavBackground
                                        }
                                    ),
                                    border = BorderStroke(
                                        if (selected) 2.dp else 1.dp,
                                        if (selected) WireframeGreenBorder else WireframeBorder
                                    )
                                ) {
                                    Column(
                                        Modifier.fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            ingredient.label,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            if (selected) "Ajouté · +${formatEuros(price)}"
                                            else "+${formatEuros(price)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = CaisseMuted
                                        )
                                    }
                                }
                            }
                            repeat(OfferPizzaNameColumnCount - row.size) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }

                HorizontalDivider(
                    Modifier.padding(vertical = 10.dp),
                    color = WireframeBorder.copy(alpha = 0.25f)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Annuler")
                    }
                    Card(
                        onClick = {
                            if (canConfirm) {
                                val pizzas = slots.map { slot ->
                                    val slotRecipe = PizzaCatalog.recipeFor(slot.product.id)
                                    val included = slotRecipe?.ingredientIds
                                        ?.mapNotNull(PizzaCatalog::ingredientFor)
                                        .orEmpty()
                                    SpecialOfferPizzaSelection(
                                        productId = slot.product.id,
                                        productName = slot.product.name,
                                        customization = PizzaCustomization(
                                            size = offerSize,
                                            base = slot.base,
                                            removedIngredients = included
                                                .filter { it.id in slot.removedIngredientIds }
                                                .map { PizzaIngredientSelection(it.id, it.label) },
                                            addedSupplements = availableSupplements
                                                .filter { it.id in slot.selectedSupplementIds }
                                                .map { ingredient ->
                                                    PizzaSupplementSelection(
                                                        id = ingredient.id,
                                                        label = ingredient.label,
                                                        priceCentimes = supplementPrices[ingredient.id]
                                                            ?: PizzaCatalog.DEFAULT_SUPPLEMENT_PRICE_CENTIMES
                                                    )
                                                }
                                        )
                                    )
                                }
                                onConfirm(
                                    SpecialOfferCustomization(
                                        pizzas = pizzas,
                                        drinkLabel = drinkLabel
                                    )
                                )
                            }
                        },
                        modifier = Modifier.weight(1.5f).height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (canConfirm) CaisseBlue else CaisseMuted
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (canConfirm) CaisseBlueDark else WireframeBorder
                        )
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                if (canConfirm) {
                                    "Ajouter · ${formatEuros(totalCentimes)}"
                                } else {
                                    "Choisir une boisson"
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

private const val OfferPizzaNameColumnCount = 4
