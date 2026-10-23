package com.amhsolution.caisse.ui.register

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amhsolution.caisse.domain.catalog.KebabaOptionCatalog
import com.amhsolution.caisse.domain.catalog.SampleCatalog
import com.amhsolution.caisse.domain.catalog.PizzaCatalog
import com.amhsolution.caisse.domain.catalog.ProductOptionCatalog
import com.amhsolution.caisse.domain.formatEuros
import com.amhsolution.caisse.domain.model.CartLine
import com.amhsolution.caisse.domain.model.DeliveryInfo
import com.amhsolution.caisse.domain.model.OrderType
import com.amhsolution.caisse.domain.model.PaymentRecord
import com.amhsolution.caisse.domain.model.PizzaCustomization
import com.amhsolution.caisse.domain.model.Product
import com.amhsolution.caisse.domain.model.ProductCategory
import com.amhsolution.caisse.domain.model.ProductCustomization
import com.amhsolution.caisse.domain.model.SpecialOfferCustomization
import com.amhsolution.caisse.domain.model.StudentMenuCustomization
import com.amhsolution.caisse.domain.payment.toPaymentArticleLines
import com.amhsolution.caisse.ui.payment.PaymentScreen
import com.amhsolution.caisse.ui.theme.AMHCaisseTheme
import com.amhsolution.caisse.ui.theme.CaisseMuted
import com.amhsolution.caisse.ui.theme.WireframeBorder
import com.amhsolution.caisse.ui.theme.WireframeGreenBackground
import com.amhsolution.caisse.ui.theme.WireframeGreenBorder
import com.amhsolution.caisse.ui.theme.WireframeGreenText
import com.amhsolution.caisse.ui.theme.WireframeNavBackground
import com.amhsolution.caisse.ui.theme.WireframeRedBackground
import com.amhsolution.caisse.ui.theme.WireframeRedBorder
import com.amhsolution.caisse.ui.theme.WireframeRedText
import com.amhsolution.caisse.ui.theme.WireframeYellowBackground

private val QuantityControlWidth = 116.dp
private val QuantityButtonSize = 36.dp
private val QuantityValueWidth = 28.dp
private val QuantityButtonSpacing = 8.dp
private val OrderTypeButtonHeight = 44.dp
private val CategoryButtonHeight = 40.dp
private val ProductCardHeight = 68.dp
private val ProductCardMinWidth = 96.dp

@Composable
fun RegisterScreen(
    onBack: () -> Unit,
    initialCartLines: List<CartLine>? = null,
    initialOrderType: OrderType? = null,
    initialDeliveryInfo: DeliveryInfo? = null,
    catalogProducts: List<Product> = SampleCatalog.products,
    pizzaSupplementPrices: Map<String, Int> = PizzaCatalog.defaultSupplementPrices,
    productSupplementPrices: Map<String, Int> = ProductOptionCatalog.defaultSupplementPrices,
    pizzaSizeOffsets: Map<String, Int> = PizzaCatalog.defaultSizeOffsets,
    onOrderPaid: (OrderType, List<CartLine>, Int, DeliveryInfo?, List<PaymentRecord>) -> Unit = { _, _, _, _, _ -> },
    onQueueOrder: (OrderType, List<CartLine>, Int, DeliveryInfo?) -> Unit = { _, _, _, _ -> },
    onOpenCashDrawer: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf(ProductCategory.KLASSIK) }
    var selectedOrderType by remember { mutableStateOf(initialOrderType ?: OrderType.SUR_PLACE) }
    var cartLines by remember { mutableStateOf(initialCartLines ?: emptyList()) }
    var showPayment by remember { mutableStateOf(false) }
    var deliveryCustomerName by remember { mutableStateOf(initialDeliveryInfo?.customerName ?: "") }
    var deliveryAddress by remember { mutableStateOf(initialDeliveryInfo?.address ?: "") }
    var deliveryPhoneNumber by remember { mutableStateOf(initialDeliveryInfo?.phoneNumber ?: "") }
    var deliveryDriverNumber by remember { mutableStateOf(initialDeliveryInfo?.driverNumber ?: 1) }
    var pizzaToCustomize by remember { mutableStateOf<Product?>(null) }
    var productToCustomize by remember { mutableStateOf<Product?>(null) }
    var kebabaToCustomize by remember { mutableStateOf<Product?>(null) }
    var offerToCustomize by remember { mutableStateOf<Product?>(null) }
    var studentMenuToCustomize by remember { mutableStateOf<Product?>(null) }

    val products = remember(selectedCategory, catalogProducts) {
        catalogProducts.filter { it.category == selectedCategory }
    }
    val cartTotalCentimes = cartLines.sumOf { it.lineTotalCentimes }
    val deliveryInfo = buildDeliveryInfo(
        orderType = selectedOrderType,
        customerName = deliveryCustomerName,
        address = deliveryAddress,
        phoneNumber = deliveryPhoneNumber,
        driverNumber = deliveryDriverNumber
    )
    val deliveryReady = selectedOrderType != OrderType.LIVRAISON || deliveryInfo != null

    pizzaToCustomize?.let { product ->
        PizzaCustomizationDialog(
            product = product,
            supplementPrices = pizzaSupplementPrices,
            sizeOffsets = pizzaSizeOffsets,
            onDismiss = { pizzaToCustomize = null },
            onConfirm = { customization, unitPriceCentimes ->
                cartLines = addCustomizedPizzaToCart(
                    cartLines = cartLines,
                    product = product,
                    customization = customization,
                    unitPriceCentimes = unitPriceCentimes
                )
                pizzaToCustomize = null
            }
        )
    }

    productToCustomize?.let { product ->
        ProductCustomizationDialog(
            product = product,
            supplementPrices = productSupplementPrices,
            onDismiss = { productToCustomize = null },
            onConfirm = { customization, unitPriceCentimes ->
                cartLines = addCustomizedProductToCart(
                    cartLines = cartLines,
                    product = product,
                    customization = customization,
                    unitPriceCentimes = unitPriceCentimes
                )
                productToCustomize = null
            }
        )
    }

    kebabaToCustomize?.let { product ->
        KebabaCustomizationDialog(
            product = product,
            onDismiss = { kebabaToCustomize = null },
            onConfirm = { customization, unitPriceCentimes ->
                cartLines = addCustomizedProductToCart(
                    cartLines = cartLines,
                    product = product,
                    customization = customization,
                    unitPriceCentimes = unitPriceCentimes
                )
                kebabaToCustomize = null
            }
        )
    }

    offerToCustomize?.let { product ->
        SpecialOfferDialog(
            product = product,
            supplementPrices = pizzaSupplementPrices,
            onDismiss = { offerToCustomize = null },
            onConfirm = { customization ->
                cartLines = addSpecialOfferToCart(cartLines, product, customization)
                offerToCustomize = null
            }
        )
    }

    studentMenuToCustomize?.let { product ->
        StudentMenuDialog(
            product = product,
            onDismiss = { studentMenuToCustomize = null },
            onConfirm = { customization ->
                cartLines = addStudentMenuToCart(cartLines, product, customization)
                studentMenuToCustomize = null
            }
        )
    }

    if (showPayment && cartLines.isNotEmpty()) {
        PaymentScreen(
            orderType = selectedOrderType,
            totalCentimes = cartTotalCentimes,
            articles = cartLines.toPaymentArticleLines(),
            onBack = { showPayment = false },
            onComplete = { payments ->
                onOrderPaid(selectedOrderType, cartLines, cartTotalCentimes, deliveryInfo, payments)
                showPayment = false
                cartLines = emptyList()
                deliveryCustomerName = ""
                deliveryAddress = ""
                deliveryPhoneNumber = ""
            },
            modifier = modifier
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
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            RegisterHeader(onBack = onBack)

            Spacer(modifier = Modifier.height(12.dp))

            OrderTypeSelector(
                selectedOrderType = selectedOrderType,
                onOrderTypeSelected = { selectedOrderType = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            RegisterWorkspace(
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it },
                products = products,
                onProductSelected = { product ->
                    when {
                        KebabaOptionCatalog.supportsCustomization(product) ->
                            kebabaToCustomize = product
                        else -> cartLines = addProductToCart(cartLines, product)
                    }
                },
                orderType = selectedOrderType,
                cartLines = cartLines,
                totalCentimes = cartTotalCentimes,
                deliveryCustomerName = deliveryCustomerName,
                onDeliveryCustomerNameChange = { deliveryCustomerName = it },
                deliveryAddress = deliveryAddress,
                onDeliveryAddressChange = { deliveryAddress = it },
                deliveryPhoneNumber = deliveryPhoneNumber,
                onDeliveryPhoneNumberChange = { deliveryPhoneNumber = it },
                deliveryDriverNumber = deliveryDriverNumber,
                onDeliveryDriverNumberChange = { deliveryDriverNumber = it },
                deliveryReady = deliveryReady,
                onIncrease = { lineId ->
                    cartLines = updateQuantity(cartLines, lineId, +1)
                },
                onDecrease = { lineId ->
                    cartLines = updateQuantity(cartLines, lineId, -1)
                },
                onClearCart = { cartLines = emptyList() },
                onCheckoutNow = {
                    if (deliveryReady) showPayment = true
                },
                onOpenCashDrawer = onOpenCashDrawer,
                onQueueOrder = {
                    if (deliveryReady) {
                        onQueueOrder(selectedOrderType, cartLines, cartTotalCentimes, deliveryInfo)
                        cartLines = emptyList()
                        deliveryCustomerName = ""
                        deliveryAddress = ""
                        deliveryPhoneNumber = ""
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

        }
    }
}

@Composable
private fun RegisterHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "02 - Caisse / commande",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Choisir le type de commande puis l'action adaptée",
                style = MaterialTheme.typography.bodyMedium,
                color = CaisseMuted
            )
        }

        TextButton(onClick = onBack) {
            Text("Retour accueil")
        }
    }
}

@Composable
private fun RegisterWorkspace(
    selectedCategory: ProductCategory,
    onCategorySelected: (ProductCategory) -> Unit,
    products: List<Product>,
    onProductSelected: (Product) -> Unit,
    orderType: OrderType,
    cartLines: List<CartLine>,
    totalCentimes: Int,
    deliveryCustomerName: String,
    onDeliveryCustomerNameChange: (String) -> Unit,
    deliveryAddress: String,
    onDeliveryAddressChange: (String) -> Unit,
    deliveryPhoneNumber: String,
    onDeliveryPhoneNumberChange: (String) -> Unit,
    deliveryDriverNumber: Int,
    onDeliveryDriverNumberChange: (Int) -> Unit,
    deliveryReady: Boolean,
    onIncrease: (String) -> Unit,
    onDecrease: (String) -> Unit,
    onClearCart: () -> Unit,
    onCheckoutNow: () -> Unit,
    onOpenCashDrawer: () -> Unit,
    onQueueOrder: () -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        when {
            maxWidth < 720.dp -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CategoryPanel(
                        selectedCategory = selectedCategory,
                        onCategorySelected = onCategorySelected,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(164.dp)
                    )
                    ProductPanel(
                        products = products,
                        onProductSelected = onProductSelected,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                    CartPanel(
                        orderType = orderType,
                        cartLines = cartLines,
                        totalCentimes = totalCentimes,
                        deliveryCustomerName = deliveryCustomerName,
                        onDeliveryCustomerNameChange = onDeliveryCustomerNameChange,
                        deliveryAddress = deliveryAddress,
                        onDeliveryAddressChange = onDeliveryAddressChange,
                        deliveryPhoneNumber = deliveryPhoneNumber,
                        onDeliveryPhoneNumberChange = onDeliveryPhoneNumberChange,
                        deliveryDriverNumber = deliveryDriverNumber,
                        onDeliveryDriverNumberChange = onDeliveryDriverNumberChange,
                        deliveryReady = deliveryReady,
                        onIncrease = onIncrease,
                        onDecrease = onDecrease,
                        onClearCart = onClearCart,
                        onCheckoutNow = onCheckoutNow,
                        onOpenCashDrawer = onOpenCashDrawer,
                        onQueueOrder = onQueueOrder,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 360.dp, max = 520.dp)
                    )
                }
            }

            maxWidth < 950.dp -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CategoryPanel(
                        selectedCategory = selectedCategory,
                        onCategorySelected = onCategorySelected,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(112.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ProductPanel(
                            products = products,
                            onProductSelected = onProductSelected,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                        CartPanel(
                            orderType = orderType,
                            cartLines = cartLines,
                            totalCentimes = totalCentimes,
                            deliveryCustomerName = deliveryCustomerName,
                            onDeliveryCustomerNameChange = onDeliveryCustomerNameChange,
                            deliveryAddress = deliveryAddress,
                            onDeliveryAddressChange = onDeliveryAddressChange,
                            deliveryPhoneNumber = deliveryPhoneNumber,
                            onDeliveryPhoneNumberChange = onDeliveryPhoneNumberChange,
                            deliveryDriverNumber = deliveryDriverNumber,
                            onDeliveryDriverNumberChange = onDeliveryDriverNumberChange,
                            deliveryReady = deliveryReady,
                            onIncrease = onIncrease,
                            onDecrease = onDecrease,
                            onClearCart = onClearCart,
                            onCheckoutNow = onCheckoutNow,
                            onOpenCashDrawer = onOpenCashDrawer,
                            onQueueOrder = onQueueOrder,
                            modifier = Modifier
                                .widthIn(min = 300.dp, max = 320.dp)
                                .fillMaxHeight()
                        )
                    }
                }
            }

            else -> {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CategoryPanel(
                        selectedCategory = selectedCategory,
                        onCategorySelected = onCategorySelected,
                        modifier = Modifier
                            .width(160.dp)
                            .fillMaxHeight()
                    )
                    ProductPanel(
                        products = products,
                        onProductSelected = onProductSelected,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                    CartPanel(
                        orderType = orderType,
                        cartLines = cartLines,
                        totalCentimes = totalCentimes,
                        deliveryCustomerName = deliveryCustomerName,
                        onDeliveryCustomerNameChange = onDeliveryCustomerNameChange,
                        deliveryAddress = deliveryAddress,
                        onDeliveryAddressChange = onDeliveryAddressChange,
                        deliveryPhoneNumber = deliveryPhoneNumber,
                        onDeliveryPhoneNumberChange = onDeliveryPhoneNumberChange,
                        deliveryDriverNumber = deliveryDriverNumber,
                        onDeliveryDriverNumberChange = onDeliveryDriverNumberChange,
                        deliveryReady = deliveryReady,
                        onIncrease = onIncrease,
                        onDecrease = onDecrease,
                        onClearCart = onClearCart,
                        onCheckoutNow = onCheckoutNow,
                        onOpenCashDrawer = onOpenCashDrawer,
                        onQueueOrder = onQueueOrder,
                        modifier = Modifier
                            .width(300.dp)
                            .fillMaxHeight()
                    )
                }
            }
        }
    }
}

@Composable
private fun OrderTypeSelector(
    selectedOrderType: OrderType,
    onOrderTypeSelected: (OrderType) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val compact = maxWidth < 720.dp
        val choices: @Composable RowScope.() -> Unit = {
            OrderType.entries.forEach { orderType ->
                OrderTypeButton(
                    orderType = orderType,
                    selected = orderType == selectedOrderType,
                    onClick = { onOrderTypeSelected(orderType) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (compact) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Type de commande",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    content = choices
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Type de commande",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(end = 4.dp)
                )
                choices()
            }
        }
    }
}

@Composable
private fun OrderTypeButton(
    orderType: OrderType,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(OrderTypeButtonHeight),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) WireframeGreenBackground else Color.White
        ),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) WireframeGreenBorder else WireframeBorder
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = orderType.label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) WireframeGreenText else MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}

@Composable
private fun CategoryPanel(
    selectedCategory: ProductCategory,
    onCategorySelected: (ProductCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    PanelContainer(
        title = "Catégories",
        modifier = modifier
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = ProductCardMinWidth),
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(ProductCategory.entries.toList(), key = { it.name }) { category ->
                val selected = category == selectedCategory
                CategoryButton(
                    label = category.label,
                    selected = selected,
                    onClick = { onCategorySelected(category) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun CategoryButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(CategoryButtonHeight),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) WireframeGreenBackground else WireframeNavBackground
        ),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) WireframeGreenBorder else WireframeBorder
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) WireframeGreenText else MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}

@Composable
private fun ProductPanel(
    products: List<Product>,
    onProductSelected: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    PanelContainer(
        title = "Produits de la carte",
        modifier = modifier
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = ProductCardMinWidth),
            contentPadding = PaddingValues(2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(products, key = { it.id }) { product ->
                ProductCard(
                    product = product,
                    onClick = { onProductSelected(product) }
                )
            }
        }
    }
}

@Composable
private fun ProductCard(
    product: Product,
    onClick: () -> Unit
) {
    val hasConfiguredPrice = product.priceCentimes > 0
    Card(
        onClick = onClick,
        enabled = hasConfiguredPrice,
        modifier = Modifier
            .fillMaxWidth()
            .height(ProductCardHeight),
        shape = RoundedCornerShape(9.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, WireframeBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = product.name,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 15.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (hasConfiguredPrice) {
                    formatEuros(product.priceCentimes)
                } else {
                    "Prix à définir"
                },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (hasConfiguredPrice) WireframeGreenText else CaisseMuted
            )
        }
    }
}

@Composable
private fun CartPanel(
    orderType: OrderType,
    cartLines: List<CartLine>,
    totalCentimes: Int,
    deliveryCustomerName: String,
    onDeliveryCustomerNameChange: (String) -> Unit,
    deliveryAddress: String,
    onDeliveryAddressChange: (String) -> Unit,
    deliveryPhoneNumber: String,
    onDeliveryPhoneNumberChange: (String) -> Unit,
    deliveryDriverNumber: Int,
    onDeliveryDriverNumberChange: (Int) -> Unit,
    deliveryReady: Boolean,
    onIncrease: (String) -> Unit,
    onDecrease: (String) -> Unit,
    onClearCart: () -> Unit,
    onCheckoutNow: () -> Unit,
    onOpenCashDrawer: () -> Unit,
    onQueueOrder: () -> Unit,
    modifier: Modifier = Modifier
) {
    var deliveryInfoExpanded by remember(orderType) {
        mutableStateOf(orderType == OrderType.LIVRAISON && !deliveryReady)
    }

    PanelContainer(
        title = "Panier",
        headerExtra = {
            TicketBadge(orderType = orderType)
        },
        modifier = modifier
    ) {
        if (orderType == OrderType.LIVRAISON && deliveryInfoExpanded) {
            DeliveryInfoPanel(
                customerName = deliveryCustomerName,
                onCustomerNameChange = onDeliveryCustomerNameChange,
                address = deliveryAddress,
                onAddressChange = onDeliveryAddressChange,
                phoneNumber = deliveryPhoneNumber,
                onPhoneNumberChange = onDeliveryPhoneNumberChange,
                driverNumber = deliveryDriverNumber,
                onDriverNumberChange = onDeliveryDriverNumberChange,
                isComplete = deliveryReady,
                onDone = { deliveryInfoExpanded = false },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                CartTableHeader()

                if (cartLines.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Panier vide",
                            style = MaterialTheme.typography.bodyLarge,
                            color = CaisseMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(cartLines, key = { it.lineId }) { line ->
                            CartLineRow(
                                line = line,
                                onIncrease = { onIncrease(line.lineId) },
                                onDecrease = { onDecrease(line.lineId) }
                            )
                        }
                    }
                }

                if (orderType == OrderType.LIVRAISON) {
                    Spacer(modifier = Modifier.height(10.dp))
                    DeliveryInfoSummary(
                        isComplete = deliveryReady,
                        onClick = { deliveryInfoExpanded = true }
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = WireframeBorder.copy(alpha = 0.25f)
                )

                Text(
                    text = "Total : ${formatEuros(totalCentimes)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ActionButton(
                        label = "Encaisser maintenant",
                        containerColor = WireframeGreenBackground,
                        contentColor = WireframeGreenText,
                        borderColor = WireframeGreenBorder,
                        enabled = cartLines.isNotEmpty() && deliveryReady,
                        onClick = onCheckoutNow,
                        modifier = Modifier.weight(1.2f)
                    )
                    ActionButton(
                        label = "Ouvrir tiroir",
                        containerColor = WireframeNavBackground,
                        contentColor = MaterialTheme.colorScheme.onBackground,
                        borderColor = WireframeBorder,
                        enabled = true,
                        onClick = onOpenCashDrawer,
                        modifier = Modifier.weight(0.9f)
                    )
                    ActionButton(
                        label = when (orderType) {
                            OrderType.SUR_PLACE -> "Envoyer en attente"
                            OrderType.LIVRAISON -> "Attente livraison"
                            OrderType.A_EMPORTER -> "Envoyer en attente"
                        },
                        containerColor = WireframeYellowBackground,
                        contentColor = MaterialTheme.colorScheme.onBackground,
                        borderColor = WireframeBorder,
                        enabled = cartLines.isNotEmpty() && deliveryReady,
                        onClick = onQueueOrder,
                        modifier = Modifier.weight(1.2f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                ActionButton(
                    label = "Annuler panier",
                    containerColor = WireframeRedBackground,
                    contentColor = WireframeRedText,
                    borderColor = WireframeRedBorder,
                    enabled = cartLines.isNotEmpty(),
                    onClick = onClearCart,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun DeliveryInfoPanel(
    customerName: String,
    onCustomerNameChange: (String) -> Unit,
    address: String,
    onAddressChange: (String) -> Unit,
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    driverNumber: Int,
    onDriverNumberChange: (Int) -> Unit,
    isComplete: Boolean,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    Column(
        modifier = modifier
            .imePadding()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Infos livraison",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = {
                focusManager.clearFocus()
                onDone()
            }) {
                Text(
                    text = "OK",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = WireframeGreenText
                )
            }
        }

        Text(
            text = if (isComplete) {
                "Informations complètes"
            } else {
                "Nom, adresse et téléphone requis"
            },
            style = MaterialTheme.typography.labelMedium,
            color = if (isComplete) WireframeGreenText else WireframeRedText
        )

        OutlinedTextField(
            value = customerName,
            onValueChange = onCustomerNameChange,
            label = { Text("Nom client") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = address,
            onValueChange = onAddressChange,
            label = { Text("Adresse") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = onPhoneNumberChange,
            label = { Text("Téléphone client") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "Livreur",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            modifier = Modifier
                .fillMaxWidth()
                .height(74.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            userScrollEnabled = false
        ) {
            items((1..10).toList(), key = { it }) { number ->
                val selected = number == driverNumber
                DriverButton(
                    number = number,
                    selected = selected,
                    onClick = { onDriverNumberChange(number) }
                )
            }
        }
    }
}

@Composable
private fun DeliveryInfoSummary(
    isComplete: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = WireframeNavBackground),
        border = BorderStroke(
            width = 1.dp,
            color = if (isComplete) WireframeGreenBorder else WireframeRedBorder
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Infos livraison",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (isComplete) "Complètes · Modifier" else "À compléter",
                style = MaterialTheme.typography.labelMedium,
                color = if (isComplete) WireframeGreenText else WireframeRedText
            )
        }
    }
}

@Composable
private fun DriverButton(
    number: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.height(34.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) WireframeGreenBackground else WireframeNavBackground
        ),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) WireframeGreenBorder else WireframeBorder
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number.toString(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = if (selected) WireframeGreenText else MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
private fun TicketBadge(orderType: OrderType) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = WireframeGreenBackground,
        border = BorderStroke(1.dp, WireframeGreenBorder)
    ) {
        Text(
            text = "Ticket : ${orderType.ticketLabel}",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = WireframeGreenText
        )
    }
}

@Composable
private fun CartTableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Produit",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "Qt.",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(QuantityControlWidth),
            textAlign = TextAlign.Center
        )
        Text(
            text = "Prix",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(64.dp),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun CartLineRow(
    line: CartLine,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = line.product.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            line.pizzaCustomization?.let { customization ->
                Text(
                    text = customization.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = CaisseMuted,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
            line.productCustomization?.summary?.takeIf { it.isNotBlank() }?.let { summary ->
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = CaisseMuted,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
            line.specialOfferCustomization?.let { customization ->
                Text(
                    text = customization.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = CaisseMuted,
                    maxLines = 6,
                    overflow = TextOverflow.Ellipsis
                )
            }
            line.studentMenuCustomization?.let { customization ->
                Text(
                    text = customization.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = CaisseMuted,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Row(
            modifier = Modifier.width(QuantityControlWidth),
            horizontalArrangement = Arrangement.spacedBy(
                QuantityButtonSpacing,
                Alignment.CenterHorizontally
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            QuantityButton(label = "−", onClick = onDecrease)
            Text(
                text = line.quantity.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(QuantityValueWidth),
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            QuantityButton(label = "+", onClick = onIncrease)
        }

        Text(
            text = formatEuros(line.lineTotalCentimes),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(64.dp),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun QuantityButton(
    label: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .width(QuantityButtonSize)
            .height(QuantityButtonSize),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = WireframeNavBackground),
        border = BorderStroke(1.dp, WireframeBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 20.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun ActionButton(
    label: String,
    containerColor: Color,
    contentColor: Color,
    borderColor: Color,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            disabledContainerColor = containerColor.copy(alpha = 0.5f)
        ),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun PanelContainer(
    title: String,
    modifier: Modifier = Modifier,
    headerExtra: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, WireframeBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                headerExtra?.invoke()
            }
            content()
        }
    }
}

private fun addProductToCart(cartLines: List<CartLine>, product: Product): List<CartLine> {
    val existing = cartLines.find { it.product.id == product.id }
    return if (existing != null) {
        cartLines.map { line ->
            if (line.product.id == product.id) {
                line.copy(quantity = line.quantity + 1)
            } else {
                line
            }
        }
    } else {
        cartLines + CartLine(product = product, quantity = 1)
    }
}

private fun addCustomizedPizzaToCart(
    cartLines: List<CartLine>,
    product: Product,
    customization: PizzaCustomization,
    unitPriceCentimes: Int
): List<CartLine> {
    val lineId = "${product.id}-${customization.stableKey}"
    val existing = cartLines.find { it.lineId == lineId }
    return if (existing != null) {
        cartLines.map { line ->
            if (line.lineId == lineId) line.copy(quantity = line.quantity + 1) else line
        }
    } else {
        cartLines + CartLine(
            product = product,
            quantity = 1,
            lineId = lineId,
            unitPriceCentimes = unitPriceCentimes,
            pizzaCustomization = customization
        )
    }
}

private fun addCustomizedProductToCart(
    cartLines: List<CartLine>,
    product: Product,
    customization: ProductCustomization,
    unitPriceCentimes: Int
): List<CartLine> {
    val lineId = "${product.id}-${customization.stableKey}"
    val existing = cartLines.find { it.lineId == lineId }
    return if (existing != null) {
        cartLines.map { line ->
            if (line.lineId == lineId) line.copy(quantity = line.quantity + 1) else line
        }
    } else {
        cartLines + CartLine(
            product = product,
            quantity = 1,
            lineId = lineId,
            unitPriceCentimes = unitPriceCentimes,
            productCustomization = customization
        )
    }
}

private fun addSpecialOfferToCart(
    cartLines: List<CartLine>,
    product: Product,
    customization: SpecialOfferCustomization
): List<CartLine> {
    val unitPriceCentimes = product.priceCentimes + customization.supplementsTotalCentimes
    val lineId = "${product.id}-${customization.stableKey}"
    val existing = cartLines.find { it.lineId == lineId }
    return if (existing != null) {
        cartLines.map { line ->
            if (line.lineId == lineId) line.copy(quantity = line.quantity + 1) else line
        }
    } else {
        cartLines + CartLine(
            product = product,
            quantity = 1,
            lineId = lineId,
            unitPriceCentimes = unitPriceCentimes,
            specialOfferCustomization = customization
        )
    }
}

private fun addStudentMenuToCart(
    cartLines: List<CartLine>,
    product: Product,
    customization: StudentMenuCustomization
): List<CartLine> {
    val lineId = "${product.id}-${customization.stableKey}"
    val existing = cartLines.find { it.lineId == lineId }
    return if (existing != null) {
        cartLines.map { line ->
            if (line.lineId == lineId) line.copy(quantity = line.quantity + 1) else line
        }
    } else {
        cartLines + CartLine(
            product = product,
            quantity = 1,
            lineId = lineId,
            unitPriceCentimes = customization.priceCentimes,
            studentMenuCustomization = customization
        )
    }
}

private fun updateQuantity(
    cartLines: List<CartLine>,
    lineId: String,
    delta: Int
): List<CartLine> {
    return cartLines.mapNotNull { line ->
        if (line.lineId != lineId) {
            line
        } else {
            val newQty = line.quantity + delta
            if (newQty <= 0) null else line.copy(quantity = newQty)
        }
    }
}

private fun buildDeliveryInfo(
    orderType: OrderType,
    customerName: String,
    address: String,
    phoneNumber: String,
    driverNumber: Int
): DeliveryInfo? {
    if (orderType != OrderType.LIVRAISON) return null

    val cleanedName = customerName.trim()
    val cleanedAddress = address.trim()
    val cleanedPhone = phoneNumber.trim()
    if (cleanedName.isEmpty() || cleanedAddress.isEmpty() || cleanedPhone.isEmpty()) return null

    return DeliveryInfo(
        customerName = cleanedName,
        address = cleanedAddress,
        phoneNumber = cleanedPhone,
        driverNumber = driverNumber
    )
}

@Preview(showBackground = true, widthDp = 1280, heightDp = 800)
@Composable
private fun RegisterScreenPreview() {
    AMHCaisseTheme {
        RegisterScreen(onBack = {})
    }
}
