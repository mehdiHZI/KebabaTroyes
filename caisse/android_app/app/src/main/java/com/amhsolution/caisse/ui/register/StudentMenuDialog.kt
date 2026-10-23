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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.amhsolution.caisse.domain.catalog.OptionIngredient
import com.amhsolution.caisse.domain.catalog.PizzaBase
import com.amhsolution.caisse.domain.catalog.PizzaCatalog
import com.amhsolution.caisse.domain.catalog.PizzaSize
import com.amhsolution.caisse.domain.catalog.ProductOptionCatalog
import com.amhsolution.caisse.domain.formatEuros
import com.amhsolution.caisse.domain.model.IngredientSelection
import com.amhsolution.caisse.domain.model.PizzaCustomization
import com.amhsolution.caisse.domain.model.PizzaIngredientSelection
import com.amhsolution.caisse.domain.model.Product
import com.amhsolution.caisse.domain.model.ProductCustomization
import com.amhsolution.caisse.domain.model.StudentMenuChoiceKind
import com.amhsolution.caisse.domain.model.StudentMenuCustomization
import com.amhsolution.caisse.domain.model.SupplementSelection
import com.amhsolution.caisse.ui.theme.CaisseMuted
import com.amhsolution.caisse.ui.theme.WireframeBorder
import com.amhsolution.caisse.ui.theme.WireframeGreenBackground
import com.amhsolution.caisse.ui.theme.WireframeGreenBorder
import com.amhsolution.caisse.ui.theme.WireframeGreenText
import com.amhsolution.caisse.ui.theme.WireframeNavBackground

private const val StudentPizzaColumnCount = 4

@Composable
fun StudentMenuDialog(
    product: Product,
    onDismiss: () -> Unit,
    onConfirm: (StudentMenuCustomization) -> Unit
) {
    val pizzaOptions = remember { ProductOptionCatalog.studentMenuPizzaOptions() }
    val burger = remember { ProductOptionCatalog.studentMenuBurger() }
    val sandwich = remember { ProductOptionCatalog.studentMenuSandwich() }
    val drinkOptions = remember { ProductOptionCatalog.studentMenuDrinkOptions() }
    val burgerReplacementSauceOptions =
        remember { ProductOptionCatalog.burgerReplacementSauceOptions }

    var choiceKind by remember(product.id) { mutableStateOf<StudentMenuChoiceKind?>(null) }
    var selectedPizza by remember(product.id) {
        mutableStateOf(pizzaOptions.firstOrNull())
    }
    var selectedBase by remember(product.id) {
        mutableStateOf(
            selectedPizza?.let { PizzaCatalog.recipeFor(it.id)?.defaultBase } ?: PizzaBase.TOMATE
        )
    }
    var removedPizzaIngredientIds by remember(product.id) { mutableStateOf<Set<String>>(emptySet()) }
    var removedBurgerIngredientIds by remember(product.id) { mutableStateOf<Set<String>>(emptySet()) }
    var removedSandwichIngredientIds by remember(product.id) { mutableStateOf<Set<String>>(emptySet()) }
    var selectedBurgerReplacementSauceId by remember(product.id) { mutableStateOf<String?>(null) }
    var selectedDrinkId by remember(product.id) { mutableStateOf<String?>(null) }

    val pizzaRecipe = selectedPizza?.id?.let(PizzaCatalog::recipeFor)
    val pizzaIngredients = remember(pizzaRecipe) {
        pizzaRecipe?.ingredientIds?.mapNotNull(PizzaCatalog::ingredientFor).orEmpty()
    }
    val burgerIngredients = remember(burger?.id) {
        burger?.let { item ->
            ProductOptionCatalog.recipeFor(item.id)?.ingredientIds?.mapNotNull { id ->
                ProductOptionCatalog.ingredientFor(item.category, id)
            }.orEmpty()
        }.orEmpty()
    }
    val sandwichIngredients = remember(sandwich?.id) {
        sandwich?.let { item ->
            ProductOptionCatalog.recipeFor(item.id)?.ingredientIds?.mapNotNull { id ->
                ProductOptionCatalog.ingredientFor(item.category, id)
            }.orEmpty()
        }.orEmpty()
    }
    val defaultBurgerSauce = remember(burger) {
        burger?.let(ProductOptionCatalog::burgerDefaultSauce)
    }
    val burgerSauceRemoved =
        defaultBurgerSauce != null && defaultBurgerSauce.id in removedBurgerIngredientIds
    val selectedDrink = drinkOptions.firstOrNull { it.id == selectedDrinkId }
    val totalCentimes = when (choiceKind) {
        StudentMenuChoiceKind.PIZZA -> ProductOptionCatalog.STUDENT_MENU_PIZZA_PRICE_CENTIMES
        StudentMenuChoiceKind.BURGER,
        StudentMenuChoiceKind.SANDWICH -> ProductOptionCatalog.STUDENT_MENU_CLASSICO_PRICE_CENTIMES
        null -> ProductOptionCatalog.STUDENT_MENU_PIZZA_PRICE_CENTIMES
    }
    val canConfirm = when (choiceKind) {
        StudentMenuChoiceKind.PIZZA -> selectedPizza != null && selectedDrink != null
        StudentMenuChoiceKind.BURGER -> burger != null && selectedDrink != null
        StudentMenuChoiceKind.SANDWICH -> sandwich != null && selectedDrink != null
        null -> false
    }

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
                            "Menu étudiant",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Pizza 8 € · Il Classico / Classico 7,50 € · + boisson · sans supplément",
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

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(
                            "1. Choisir le plat",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StudentChoiceChip(
                                label = "Pizza S",
                                selected = choiceKind == StudentMenuChoiceKind.PIZZA,
                                onClick = {
                                    choiceKind = StudentMenuChoiceKind.PIZZA
                                    removedPizzaIngredientIds = emptySet()
                                    removedBurgerIngredientIds = emptySet()
                                    removedSandwichIngredientIds = emptySet()
                                    selectedBurgerReplacementSauceId = null
                                },
                                modifier = Modifier.weight(1f)
                            )
                            StudentChoiceChip(
                                label = "Il Classico",
                                selected = choiceKind == StudentMenuChoiceKind.BURGER,
                                onClick = {
                                    choiceKind = StudentMenuChoiceKind.BURGER
                                    removedPizzaIngredientIds = emptySet()
                                    removedBurgerIngredientIds = emptySet()
                                    removedSandwichIngredientIds = emptySet()
                                    selectedBurgerReplacementSauceId = null
                                },
                                modifier = Modifier.weight(1f),
                                enabled = burger != null
                            )
                            StudentChoiceChip(
                                label = "Classico",
                                selected = choiceKind == StudentMenuChoiceKind.SANDWICH,
                                onClick = {
                                    choiceKind = StudentMenuChoiceKind.SANDWICH
                                    removedPizzaIngredientIds = emptySet()
                                    removedBurgerIngredientIds = emptySet()
                                    removedSandwichIngredientIds = emptySet()
                                    selectedBurgerReplacementSauceId = null
                                },
                                modifier = Modifier.weight(1f),
                                enabled = sandwich != null
                            )
                        }
                    }

                    if (choiceKind == StudentMenuChoiceKind.PIZZA) {
                        item {
                            Text(
                                "2. Pizza (hors Norvegese) · taille S",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                        items(pizzaOptions.chunked(StudentPizzaColumnCount)) { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(7.dp)
                            ) {
                                row.forEach { pizza ->
                                    val selected = selectedPizza?.id == pizza.id
                                    StudentChoiceChip(
                                        label = pizza.name,
                                        selected = selected,
                                        onClick = {
                                            selectedPizza = pizza
                                            selectedBase =
                                                PizzaCatalog.recipeFor(pizza.id)?.defaultBase
                                                    ?: PizzaBase.TOMATE
                                            removedPizzaIngredientIds = emptySet()
                                        },
                                        modifier = Modifier.weight(1f).height(44.dp)
                                    )
                                }
                                repeat(StudentPizzaColumnCount - row.size) {
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
                                    StudentChoiceChip(
                                        label = base.label,
                                        selected = selectedBase == base,
                                        onClick = { selectedBase = base },
                                        modifier = Modifier.weight(1f).height(44.dp)
                                    )
                                }
                            }
                        }

                        item {
                            Text(
                                "Composition (retirer possible · pas de supplément)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                        items(pizzaIngredients.chunked(StudentPizzaColumnCount)) { row ->
                            StudentIngredientRow(
                                ingredients = row.map { OptionIngredient(it.id, it.label) },
                                removedIds = removedPizzaIngredientIds,
                                onToggle = { id ->
                                    removedPizzaIngredientIds =
                                        removedPizzaIngredientIds.toggleValue(id)
                                }
                            )
                        }
                    }

                    if (choiceKind == StudentMenuChoiceKind.BURGER) {
                        item {
                            Text(
                                "2. Composition Il Classico (retirer possible · pas de supplément)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                        items(burgerIngredients.chunked(StudentPizzaColumnCount)) { row ->
                            StudentIngredientRow(
                                ingredients = row,
                                removedIds = removedBurgerIngredientIds,
                                onToggle = { id ->
                                    val next = removedBurgerIngredientIds.toggleValue(id)
                                    removedBurgerIngredientIds = next
                                    if (
                                        defaultBurgerSauce != null &&
                                        id == defaultBurgerSauce.id &&
                                        id !in next
                                    ) {
                                        selectedBurgerReplacementSauceId = null
                                    }
                                }
                            )
                        }
                        if (burgerSauceRemoved) {
                            item {
                                Text(
                                    "Sauce de remplacement (optionnel)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 6.dp)
                                )
                                Text(
                                    "Remplace ${defaultBurgerSauce?.label.orEmpty()}.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CaisseMuted
                                )
                            }
                            items(
                                burgerReplacementSauceOptions.chunked(StudentPizzaColumnCount)
                            ) { row ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                                ) {
                                    row.forEach { sauce ->
                                        StudentChoiceChip(
                                            label = sauce.label,
                                            selected =
                                                selectedBurgerReplacementSauceId == sauce.id,
                                            onClick = {
                                                selectedBurgerReplacementSauceId =
                                                    if (selectedBurgerReplacementSauceId == sauce.id) {
                                                        null
                                                    } else {
                                                        sauce.id
                                                    }
                                            },
                                            modifier = Modifier.weight(1f).height(44.dp)
                                        )
                                    }
                                    repeat(StudentPizzaColumnCount - row.size) {
                                        Spacer(Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }

                    if (choiceKind == StudentMenuChoiceKind.SANDWICH) {
                        item {
                            Text(
                                "2. Composition Classico focaccia (retirer possible · pas de supplément)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                        items(sandwichIngredients.chunked(StudentPizzaColumnCount)) { row ->
                            StudentIngredientRow(
                                ingredients = row,
                                removedIds = removedSandwichIngredientIds,
                                onToggle = { id ->
                                    removedSandwichIngredientIds =
                                        removedSandwichIngredientIds.toggleValue(id)
                                }
                            )
                        }
                    }

                    item {
                        Text(
                            when (choiceKind) {
                                StudentMenuChoiceKind.PIZZA,
                                StudentMenuChoiceKind.BURGER,
                                StudentMenuChoiceKind.SANDWICH -> "3. Boisson"
                                null -> "2. Boisson"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                    items(drinkOptions.chunked(StudentPizzaColumnCount)) { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(7.dp)
                        ) {
                            row.forEach { drink ->
                                StudentChoiceChip(
                                    label = drink.label,
                                    selected = selectedDrinkId == drink.id,
                                    onClick = { selectedDrinkId = drink.id },
                                    modifier = Modifier.weight(1f).height(44.dp)
                                )
                            }
                            repeat(StudentPizzaColumnCount - row.size) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }

                HorizontalDivider(
                    Modifier.padding(vertical = 10.dp),
                    color = WireframeBorder.copy(alpha = 0.25f)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Annuler")
                    }
                    TextButton(
                        onClick = {
                            val kind = choiceKind ?: return@TextButton
                            val drink = selectedDrink ?: return@TextButton
                            val customization = when (kind) {
                                StudentMenuChoiceKind.PIZZA -> {
                                    val pizza = selectedPizza ?: return@TextButton
                                    StudentMenuCustomization(
                                        choiceKind = kind,
                                        itemProductId = pizza.id,
                                        itemProductName = pizza.name,
                                        drinkId = drink.id,
                                        drinkLabel = drink.label,
                                        pizzaCustomization = PizzaCustomization(
                                            size = PizzaSize.JUNIOR,
                                            base = selectedBase,
                                            removedIngredients = pizzaIngredients
                                                .filter { it.id in removedPizzaIngredientIds }
                                                .map {
                                                    PizzaIngredientSelection(it.id, it.label)
                                                }
                                        )
                                    )
                                }
                                StudentMenuChoiceKind.BURGER -> {
                                    val item = burger ?: return@TextButton
                                    val replacement = burgerReplacementSauceOptions
                                        .firstOrNull { it.id == selectedBurgerReplacementSauceId }
                                    StudentMenuCustomization(
                                        choiceKind = kind,
                                        itemProductId = item.id,
                                        itemProductName = item.name,
                                        drinkId = drink.id,
                                        drinkLabel = drink.label,
                                        productCustomization = ProductCustomization(
                                            removedIngredients = burgerIngredients
                                                .filter { it.id in removedBurgerIngredientIds }
                                                .map { IngredientSelection(it.id, it.label) },
                                            addedSupplements = listOfNotNull(
                                                replacement?.let { sauce ->
                                                    SupplementSelection(sauce.id, sauce.label, 0)
                                                }
                                            )
                                        )
                                    )
                                }
                                StudentMenuChoiceKind.SANDWICH -> {
                                    val item = sandwich ?: return@TextButton
                                    StudentMenuCustomization(
                                        choiceKind = kind,
                                        itemProductId = item.id,
                                        itemProductName = item.name,
                                        drinkId = drink.id,
                                        drinkLabel = drink.label,
                                        productCustomization = ProductCustomization(
                                            removedIngredients = sandwichIngredients
                                                .filter { it.id in removedSandwichIngredientIds }
                                                .map { IngredientSelection(it.id, it.label) }
                                        )
                                    )
                                }
                            }
                            onConfirm(customization)
                        },
                        enabled = canConfirm
                    ) {
                        Text("Ajouter")
                    }
                }
            }
        }
    }
}

@Composable
private fun StudentIngredientRow(
    ingredients: List<OptionIngredient>,
    removedIds: Set<String>,
    onToggle: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        ingredients.forEach { ingredient ->
            val removed = ingredient.id in removedIds
            StudentChoiceChip(
                label = if (removed) "Sans ${ingredient.label}" else ingredient.label,
                selected = !removed,
                onClick = { onToggle(ingredient.id) },
                modifier = Modifier.weight(1f).height(44.dp)
            )
        }
        repeat(StudentPizzaColumnCount - ingredients.size) {
            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun StudentChoiceChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Card(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                !enabled -> WireframeNavBackground.copy(alpha = 0.5f)
                selected -> WireframeGreenBackground
                else -> WireframeNavBackground
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
                label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun Set<String>.toggleValue(value: String): Set<String> =
    if (value in this) this - value else this + value
