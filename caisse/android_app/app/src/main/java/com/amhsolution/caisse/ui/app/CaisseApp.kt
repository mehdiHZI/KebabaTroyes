package com.amhsolution.caisse.ui.app

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amhsolution.caisse.data.appContainer
import com.amhsolution.caisse.data.export.ClosurePdfExporter
import com.amhsolution.caisse.data.printing.BluetoothPrinterTarget
import com.amhsolution.caisse.data.printing.ConfiguredPrinter
import com.amhsolution.caisse.data.printing.PrintResult
import com.amhsolution.caisse.data.printing.PrinterDiscoveryResult
import com.amhsolution.caisse.data.printing.PrinterLinkType
import com.amhsolution.caisse.data.printing.PrinterRoutingSettings
import com.amhsolution.caisse.data.printing.TicketOutputTarget
import com.amhsolution.caisse.data.repository.CashClosureRepository
import com.amhsolution.caisse.domain.closure.CashClosure
import com.amhsolution.caisse.domain.closure.CashClosureType
import com.amhsolution.caisse.domain.model.CartLine
import com.amhsolution.caisse.domain.model.DeliveryInfo
import com.amhsolution.caisse.domain.model.HistoryRecord
import com.amhsolution.caisse.domain.model.OrderLineSnapshot
import com.amhsolution.caisse.domain.model.OrderTicket
import com.amhsolution.caisse.domain.model.OrderType
import com.amhsolution.caisse.domain.model.PaymentMethod
import com.amhsolution.caisse.domain.model.PaymentRecord
import com.amhsolution.caisse.domain.model.kitchenAdditionLines
import com.amhsolution.caisse.domain.model.toCartLinesFromCatalog
import com.amhsolution.caisse.domain.model.toOrderLineSnapshots
import com.amhsolution.caisse.domain.payment.toPaymentArticleLinesFromSnapshots
import com.amhsolution.caisse.domain.printing.toClosureReportPrintPayload
import com.amhsolution.caisse.domain.printing.toCustomerTicketPrintPayload
import com.amhsolution.caisse.domain.printing.toKitchenTicketPrintPayload
import com.amhsolution.caisse.domain.printing.toProvisionalCustomerTicketPrintPayload
import com.amhsolution.caisse.domain.printing.TicketPrinterOptions
import com.amhsolution.caisse.ui.closure.ClosureArchiveScreen
import com.amhsolution.caisse.ui.statistics.StatisticsScreen
import com.amhsolution.caisse.ui.home.HomeDestination
import com.amhsolution.caisse.ui.home.HomeScreen
import com.amhsolution.caisse.ui.orders.HistoryScreen
import com.amhsolution.caisse.ui.orders.HistoryTicketDetailScreen
import com.amhsolution.caisse.ui.orders.PendingOrdersScreen
import com.amhsolution.caisse.ui.payment.PaymentScreen
import com.amhsolution.caisse.ui.printers.PrinterSettingsScreen
import com.amhsolution.caisse.ui.prices.PriceManagementScreen
import com.amhsolution.caisse.ui.register.RegisterScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

private const val ClosureAccessPassword = "1234"

private fun requiresBluetooth(vararg endpoints: ConfiguredPrinter): Boolean =
    endpoints.any { it.linkType == PrinterLinkType.BLUETOOTH }

private val BluetoothRuntimePermissions: Array<String> =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN
        )
    } else {
        emptyArray()
    }

@Composable
fun CaisseApp() {
    val context = LocalContext.current
    val container = context.appContainer()
    val historyRepository = container.historyRepository
    val pendingOrderRepository = container.pendingOrderRepository
    val cashClosureRepository = remember(container.database) {
        CashClosureRepository(container.database, File(context.filesDir, "cloture"))
    }
    val catalogRepository = container.catalogRepository
    val printerSettingsRepository = container.printerSettingsRepository
    val scope = rememberCoroutineScope()
    val historyRecords by container.historyRecords.collectAsStateWithLifecycle()
    val cashClosures by remember(cashClosureRepository) {
        cashClosureRepository.observeClosures()
    }.collectAsStateWithLifecycle(initialValue = emptyList<CashClosure>())
    val catalogProducts by container.catalogProducts.collectAsStateWithLifecycle()
    val pizzaSupplementPrices by container.pizzaSupplementPrices.collectAsStateWithLifecycle()
    val pizzaSizeOffsets by container.pizzaSizeOffsets.collectAsStateWithLifecycle()
    val productSupplementPrices by container.productSupplementPrices.collectAsStateWithLifecycle()
    val syncStatus by container.syncStatus.collectAsStateWithLifecycle()
    val ticketSyncRepository = container.ticketSyncRepository
    val remoteOrdersRepository = container.remoteOrdersRepository

    var currentDestination by remember { mutableStateOf<HomeDestination?>(null) }
    var pendingOrders by remember { mutableStateOf<List<OrderTicket>>(emptyList()) }
    var pendingOrdersLoaded by remember { mutableStateOf(false) }
    var pendingPaymentOrder by remember { mutableStateOf<OrderTicket?>(null) }
    var editingOrder by remember { mutableStateOf<OrderTicket?>(null) }
    var selectedHistoryId by remember { mutableStateOf<String?>(null) }
    var localCashClosures by remember { mutableStateOf<List<CashClosure>>(emptyList()) }
    var closureErrorMessage by remember { mutableStateOf<String?>(null) }
    var showClosurePasswordDialog by remember { mutableStateOf(false) }
    var printMessage by remember { mutableStateOf<String?>(null) }
    var printChoiceRequest by remember { mutableStateOf<PrintChoiceRequest?>(null) }
    var printerSettings by remember {
        mutableStateOf(printerSettingsRepository.load())
    }
    var pairedPrinters by remember { mutableStateOf<List<BluetoothPrinterTarget>>(emptyList()) }
    var printerDiscoveryMessage by remember { mutableStateOf<String?>(null) }
    var pendingBluetoothAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    val bluetoothPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val action = pendingBluetoothAction
        pendingBluetoothAction = null
        val allGranted = BluetoothRuntimePermissions.all { permission ->
            permissions[permission] == true
        }
        if (allGranted) {
            action?.invoke()
        } else {
            printMessage = "Permissions Bluetooth refusées. L'impression ne peut pas démarrer."
        }
    }

    val displayedCashClosures = remember(cashClosures, localCashClosures) {
        (cashClosures + localCashClosures)
            .distinctBy { it.id }
            .sortedByDescending { it.closedAtEpochMillis }
    }
    val latestHistoryRecords by rememberUpdatedState(historyRecords)
    val latestPendingOrders by rememberUpdatedState(pendingOrders)

    LaunchedEffect(pendingOrderRepository) {
        pendingOrders = pendingOrderRepository.loadPendingOrders()
        pendingOrdersLoaded = true
    }

    LaunchedEffect(pendingOrders, pendingOrdersLoaded) {
        if (pendingOrdersLoaded) {
            pendingOrderRepository.savePendingOrders(pendingOrders)
        }
    }

    // Pull Uber / Deliveroo / canaux externes depuis le VPS
    LaunchedEffect(remoteOrdersRepository, pendingOrdersLoaded) {
        if (!pendingOrdersLoaded) return@LaunchedEffect
        while (true) {
            try {
                pendingOrders = remoteOrdersRepository.pullAndMerge(latestPendingOrders)
            } catch (_: Exception) {
                // réseau HS — file locale inchangée
            }
            delay(15_000)
        }
    }

    suspend fun runAutomaticClosures() {
        runCatching {
            cashClosureRepository.createAutomaticClosures(latestHistoryRecords)
        }.onSuccess { createdClosures ->
            if (createdClosures.isNotEmpty()) {
                localCashClosures = (localCashClosures + createdClosures)
                    .distinctBy { it.id }
                    .sortedByDescending { it.closedAtEpochMillis }
            }
        }
    }

    LaunchedEffect(historyRecords) {
        runAutomaticClosures()
    }

    LaunchedEffect(cashClosureRepository) {
        while (true) {
            runAutomaticClosures()
            delay(60_000L)
        }
    }

    LaunchedEffect(displayedCashClosures) {
        cashClosureRepository.ensureArchiveFiles(displayedCashClosures)
    }

    if (showClosurePasswordDialog) {
        ClosurePasswordDialog(
            onDismiss = { showClosurePasswordDialog = false },
            onValidated = {
                showClosurePasswordDialog = false
                currentDestination = HomeDestination.ClosureArchive
            }
        )
    }

    printMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { printMessage = null },
            title = { Text("Impression") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { printMessage = null }) {
                    Text("OK")
                }
            }
        )
    }

    fun runWithBluetoothPermission(action: () -> Unit) {
        val allGranted = BluetoothRuntimePermissions.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
        if (allGranted) {
            action()
        } else {
            pendingBluetoothAction = action
            bluetoothPermissionLauncher.launch(BluetoothRuntimePermissions)
        }
    }

    fun runWithPrintPermission(
        vararg endpoints: ConfiguredPrinter,
        action: () -> Unit
    ) {
        if (requiresBluetooth(*endpoints)) {
            runWithBluetoothPermission(action)
        } else {
            action()
        }
    }

    fun refreshPairedPrinters() {
        runWithBluetoothPermission {
            scope.launch {
                when (val result = container.customerTicketPrinter.discoverPairedPrinters()) {
                    is PrinterDiscoveryResult.Success -> {
                        pairedPrinters = result.printers
                        printerDiscoveryMessage = if (result.printers.isEmpty()) {
                            "Associez d'abord les imprimantes dans les réglages Bluetooth Android."
                        } else {
                            "${result.printers.size} appareil(s) Bluetooth associé(s)."
                        }
                    }
                    is PrinterDiscoveryResult.Failure -> {
                        pairedPrinters = emptyList()
                        printerDiscoveryMessage = result.message
                    }
                }
            }
        }
    }

    suspend fun savePaidOrder(
        ticketNumber: Int,
        orderType: OrderType,
        cartLines: List<CartLine>,
        totalCentimes: Int,
        payments: List<PaymentRecord>,
        deliveryInfo: DeliveryInfo?
    ): HistoryRecord {
        val record = historyRepository.insertPaidOrder(
            ticketNumber = ticketNumber,
            orderType = orderType,
            lines = cartLines.toOrderLineSnapshots(),
            totalCentimes = totalCentimes,
            payments = payments,
            deliveryInfo = deliveryInfo
        )
        try {
            ticketSyncRepository.enqueuePaidTicket(record)
        } catch (_: Exception) {
            // Encaissement local OK ; outbox retentera
        }
        return record
    }

    fun printPaidTicket(record: HistoryRecord, target: TicketOutputTarget, automatic: Boolean = false) {
        val settings = printerSettingsRepository.load()
        val shouldOpenCashDrawer = automatic && record.payments.any { payment ->
            payment.method == PaymentMethod.ESPECES && payment.amountCentimes > 0
        }
        val endpoints = buildList {
            if (target.includesCustomer || (shouldOpenCashDrawer && !target.includesCustomer)) {
                add(settings.customerPrinter)
            }
            if (target.includesKitchen) add(settings.kitchenPrinter)
        }
        runWithPrintPermission(*endpoints.toTypedArray()) {
            scope.launch {
                val results = mutableListOf<PrintResult>()
                if (target.includesCustomer) {
                    results += container.customerTicketPrinter.printCustomerTicket(
                        record.toCustomerTicketPrintPayload(
                            printer = TicketPrinterOptions(
                                openCashDrawer = shouldOpenCashDrawer
                            )
                        )
                    )
                }
                if (target.includesKitchen) {
                    results += container.kitchenTicketPrinter.printKitchenTicket(
                        record.toKitchenTicketPrintPayload()
                    )
                }
                if (shouldOpenCashDrawer && !target.includesCustomer) {
                    results += container.customerTicketPrinter.openCashDrawer()
                }
                printMessage = results.toPrintMessage(
                    if (automatic) {
                        target.automaticPaidMessage()
                    } else {
                        "Impression demandee."
                    }
                )
            }
        }
    }

    fun openCashDrawerManually() {
        val settings = printerSettingsRepository.load()
        runWithPrintPermission(settings.customerPrinter) {
            scope.launch {
                val result = container.customerTicketPrinter.openCashDrawer()
                printMessage = result.toUserMessage("Tiroir caisse ouvert.")
            }
        }
    }

    fun printPendingOrder(order: OrderTicket, target: TicketOutputTarget, automatic: Boolean = false) {
        val settings = printerSettingsRepository.load()
        val endpoints = buildList {
            if (target.includesCustomer) add(settings.customerPrinter)
            if (target.includesKitchen) add(settings.kitchenPrinter)
        }
        runWithPrintPermission(*endpoints.toTypedArray()) {
            scope.launch {
                val results = mutableListOf<PrintResult>()
                if (target.includesCustomer) {
                    results += container.customerTicketPrinter.printCustomerTicket(
                        order.toProvisionalCustomerTicketPrintPayload()
                    )
                }
                if (target.includesKitchen) {
                    results += container.kitchenTicketPrinter.printKitchenTicket(
                        order.toKitchenTicketPrintPayload()
                    )
                }
                printMessage = results.toPrintMessage(
                    if (automatic) {
                        target.automaticPendingMessage()
                    } else {
                        "Impression demandee."
                    }
                )
            }
        }
    }

    /**
     * Mise à jour d'une attente : ticket provisoire caisse si demandé, et bon cuisine
     * uniquement pour les lignes ajoutées (ou hausses de quantité).
     */
    fun printPendingOrderUpdate(
        order: OrderTicket,
        customerTarget: TicketOutputTarget?,
        kitchenAdditions: List<OrderLineSnapshot>,
        automatic: Boolean = true
    ) {
        val settings = printerSettingsRepository.load()
        val printCustomer = customerTarget?.includesCustomer == true
        val printKitchen = kitchenAdditions.isNotEmpty()
        if (!printCustomer && !printKitchen) return

        val endpoints = buildList {
            if (printCustomer) add(settings.customerPrinter)
            if (printKitchen) add(settings.kitchenPrinter)
        }
        runWithPrintPermission(*endpoints.toTypedArray()) {
            scope.launch {
                val results = mutableListOf<PrintResult>()
                if (printCustomer) {
                    results += container.customerTicketPrinter.printCustomerTicket(
                        order.toProvisionalCustomerTicketPrintPayload()
                    )
                }
                if (printKitchen) {
                    val kitchenOrder = order.copy(
                        lines = kitchenAdditions,
                        totalCentimes = kitchenAdditions.sumOf { it.totalCentimes }
                    )
                    results += container.kitchenTicketPrinter.printKitchenTicket(
                        kitchenOrder.toKitchenTicketPrintPayload(addition = true)
                    )
                }
                printMessage = results.toPrintMessage(
                    when {
                        !automatic -> "Impression demandee."
                        printCustomer && printKitchen ->
                            "Ticket provisoire et ajout cuisine envoyés."
                        printKitchen ->
                            "Ajout cuisine envoyé à l'imprimante cuisine."
                        else ->
                            "Ticket provisoire envoyé à l'imprimante caisse."
                    }
                )
            }
        }
    }

    printChoiceRequest?.let { request ->
        PrintTicketChoiceDialog(
            onDismiss = { printChoiceRequest = null },
            onSelected = { target ->
                printChoiceRequest = null
                when (request) {
                    is PrintChoiceRequest.PendingOrder -> printPendingOrder(request.order, target)
                    is PrintChoiceRequest.PaidTicket -> printPaidTicket(request.record, target)
                }
            }
        )
    }

    fun queueOrder(
        orderType: OrderType,
        cartLines: List<CartLine>,
        totalCentimes: Int,
        deliveryInfo: DeliveryInfo?,
        existingOrder: OrderTicket? = null
    ) {
        if (cartLines.isEmpty()) return

        scope.launch {
            val ticket = OrderTicket(
                id = existingOrder?.id ?: "pending-${UUID.randomUUID()}",
                ticketNumber = existingOrder?.ticketNumber
                    ?: historyRepository.allocateTicketNumber(),
                orderType = orderType,
                lines = cartLines.toOrderLineSnapshots(),
                totalCentimes = totalCentimes,
                createdAtEpochMillis = existingOrder?.createdAtEpochMillis
                    ?: System.currentTimeMillis(),
                statusLabel = when (orderType) {
                    OrderType.SUR_PLACE -> "A regler"
                    OrderType.A_EMPORTER -> "A regler"
                    OrderType.LIVRAISON -> "Livraison"
                },
                deliveryInfo = deliveryInfo
            )
            pendingOrders = if (existingOrder == null) {
                pendingOrders + ticket
            } else {
                pendingOrders.map { order ->
                    if (order.id == existingOrder.id) ticket else order
                }
            }
            val configuredTarget = printerSettings.automaticOutput
            if (existingOrder == null) {
                pendingOutputTarget(
                    configuredTarget = configuredTarget,
                    updatesPendingOrder = false
                )?.let { target ->
                    printPendingOrder(ticket, target, automatic = true)
                }
            } else {
                val customerTarget = pendingOutputTarget(
                    configuredTarget = configuredTarget,
                    updatesPendingOrder = true
                )
                val kitchenAdditions = if (configuredTarget.includesKitchen) {
                    kitchenAdditionLines(existingOrder.lines, ticket.lines)
                } else {
                    emptyList()
                }
                printPendingOrderUpdate(
                    order = ticket,
                    customerTarget = customerTarget,
                    kitchenAdditions = kitchenAdditions,
                    automatic = true
                )
            }
        }
    }

    fun cancelPendingOrder(order: OrderTicket) {
        scope.launch {
            historyRepository.insertCancelledOrder(order)
            pendingOrders = pendingOrders.filterNot { it.id == order.id }
            try {
                remoteOrdersRepository.ackCancelled(order.id)
            } catch (_: Exception) {
            }
        }
    }

    fun acknowledgeRemoteOrder(order: OrderTicket) {
        scope.launch {
            try {
                remoteOrdersRepository.ackAccepted(order.id)
            } catch (_: Exception) {
            }
        }
    }

    pendingPaymentOrder?.let { order ->
        PaymentScreen(
            orderType = order.orderType,
            totalCentimes = order.totalCentimes,
            articles = order.lines.toPaymentArticleLinesFromSnapshots(),
            onBack = { pendingPaymentOrder = null },
            onComplete = { payments ->
                scope.launch {
                    val record = savePaidOrder(
                        ticketNumber = order.ticketNumber,
                        orderType = order.orderType,
                        cartLines = order.toCartLinesFromCatalog(catalogProducts),
                        totalCentimes = order.totalCentimes,
                        payments = payments,
                        deliveryInfo = order.deliveryInfo
                    )
                    pendingOrders = pendingOrders.filterNot { it.id == order.id }
                    editingOrder = null
                    pendingPaymentOrder = null
                    printPaidTicket(
                        record = record,
                        target = paidOutputTarget(
                            configuredTarget = printerSettings.automaticOutput,
                            settlesPendingOrder = true
                        ),
                        automatic = true
                    )
                }
            }
        )
        return
    }

    selectedHistoryId?.let { historyId ->
        val record = historyRecords.firstOrNull { it.id == historyId }
        if (record != null) {
            val isTicketClosed = displayedCashClosures.any { closure ->
                closure.locksTickets && closure.businessDay == record.businessDay
            }
            HistoryTicketDetailScreen(
                record = record,
                onBack = { selectedHistoryId = null },
                onPrintTicket = {
                    printChoiceRequest = PrintChoiceRequest.PaidTicket(record)
                },
                onSavePayments = { payments ->
                    scope.launch {
                        if (!isTicketClosed) {
                            historyRepository.updatePayments(historyId, payments)
                        }
                        selectedHistoryId = null
                    }
                },
                isLocked = isTicketClosed
            )
            return
        } else {
            selectedHistoryId = null
        }
    }

    when (currentDestination) {
        null -> HomeScreen(
            syncStatus = syncStatus,
            onActionSelected = { destination ->
                when (destination) {
                    HomeDestination.ClosureArchive -> showClosurePasswordDialog = true
                    else -> currentDestination = destination
                }
            }
        )

        HomeDestination.Register -> RegisterScreen(
            onBack = {
                editingOrder = null
                currentDestination = null
            },
            initialCartLines = editingOrder?.toCartLinesFromCatalog(catalogProducts),
            initialOrderType = editingOrder?.orderType,
            initialDeliveryInfo = editingOrder?.deliveryInfo,
            catalogProducts = catalogProducts,
            pizzaSupplementPrices = pizzaSupplementPrices,
            productSupplementPrices = productSupplementPrices,
            pizzaSizeOffsets = pizzaSizeOffsets,
            onOrderPaid = { orderType, cartLines, totalCentimes, deliveryInfo, payments ->
                scope.launch {
                    val settledPendingOrder = editingOrder
                    val ticketNumber = settledPendingOrder?.ticketNumber
                        ?: historyRepository.allocateTicketNumber()
                    val record = savePaidOrder(
                        ticketNumber = ticketNumber,
                        orderType = orderType,
                        cartLines = cartLines,
                        totalCentimes = totalCentimes,
                        payments = payments,
                        deliveryInfo = deliveryInfo
                    )
                    settledPendingOrder?.let { order ->
                        pendingOrders = pendingOrders.filterNot { it.id == order.id }
                        editingOrder = null
                    }
                    printPaidTicket(
                        record = record,
                        target = paidOutputTarget(
                            configuredTarget = printerSettings.automaticOutput,
                            settlesPendingOrder = settledPendingOrder != null
                        ),
                        automatic = true
                    )
                }
            },
            onQueueOrder = { orderType, cartLines, totalCentimes, deliveryInfo ->
                val existingOrder = editingOrder
                queueOrder(
                    orderType = orderType,
                    cartLines = cartLines,
                    totalCentimes = totalCentimes,
                    deliveryInfo = deliveryInfo,
                    existingOrder = existingOrder
                )
                editingOrder = null
            },
            onOpenCashDrawer = {
                openCashDrawerManually()
            }
        )

        HomeDestination.OnSiteToCollect -> PendingOrdersScreen(
            title = "À encaisser sur place / à emporter",
            subtitle = "Commandes sur place et à emporter en attente",
            orders = pendingOrders.filter {
                it.orderType == OrderType.SUR_PLACE || it.orderType == OrderType.A_EMPORTER
            },
            emptyMessage = "Aucune commande sur place en attente.",
            onBack = { currentDestination = null },
            onPayOrder = { order ->
                editingOrder = order
                currentDestination = HomeDestination.Register
            },
            onPrintOrder = { order ->
                printChoiceRequest = PrintChoiceRequest.PendingOrder(order)
            },
            onCancelOrder = { order ->
                cancelPendingOrder(order)
            }
        )

        HomeDestination.DeliveryToCollect -> PendingOrdersScreen(
            title = "À encaisser livraison",
            subtitle = "Livraison maison + Uber Eats / Deliveroo",
            orders = pendingOrders.filter { it.orderType == OrderType.LIVRAISON },
            emptyMessage = "Aucune livraison en attente.",
            groupByDriver = true,
            onBack = { currentDestination = null },
            onPayOrder = { order ->
                acknowledgeRemoteOrder(order)
                editingOrder = order
                currentDestination = HomeDestination.Register
            },
            onPrintOrder = { order ->
                acknowledgeRemoteOrder(order)
                printChoiceRequest = PrintChoiceRequest.PendingOrder(order)
            },
            onCancelOrder = { order ->
                cancelPendingOrder(order)
            }
        )

        HomeDestination.History -> HistoryScreen(
            records = historyRecords,
            onBack = { currentDestination = null },
            onOpenTicket = { record -> selectedHistoryId = record.id }
        )

        HomeDestination.Statistics -> StatisticsScreen(
            records = historyRecords,
            closures = displayedCashClosures,
            closureErrorMessage = closureErrorMessage,
            onBack = { currentDestination = null },
            onCloseBusinessDay = { businessDay ->
                scope.launch {
                    runCatching {
                        cashClosureRepository.createManualDailyClosure(
                            records = historyRecords,
                            businessDay = businessDay
                        )
                    }.onSuccess { closure ->
                        closureErrorMessage = null
                        localCashClosures = localCashClosures
                            .filterNot {
                                it.businessDay == closure.businessDay && it.type == closure.type
                            } + closure
                    }.onFailure { error ->
                        closureErrorMessage = error.message ?: "La cloture n'a pas pu etre enregistree."
                    }
                }
            }
        )

        HomeDestination.PriceManagement -> PriceManagementScreen(
            products = catalogProducts,
            pizzaSupplementPrices = pizzaSupplementPrices,
            productSupplementPrices = productSupplementPrices,
            pizzaSizeOffsets = pizzaSizeOffsets,
            onUpdatePrice = { productId, priceCentimes ->
                scope.launch {
                    catalogRepository.updatePrice(productId, priceCentimes)
                }
            },
            onUpdatePizzaSupplementPrice = { ingredientId, priceCentimes ->
                scope.launch {
                    catalogRepository.updatePizzaSupplementPrice(ingredientId, priceCentimes)
                }
            },
            onUpdateProductSupplementPrice = { priceKey, priceCentimes ->
                scope.launch {
                    catalogRepository.updateProductSupplementPrice(priceKey, priceCentimes)
                }
            },
            onUpdatePizzaSizeOffset = { sizeKey, offsetCentimes ->
                scope.launch {
                    catalogRepository.updatePizzaSizeOffset(sizeKey, offsetCentimes)
                }
            },
            onBack = { currentDestination = null }
        )

        HomeDestination.ClosureArchive -> ClosureArchiveScreen(
            closures = displayedCashClosures,
            exportPathForClosure = { closure ->
                cashClosureRepository.pdfRelativePath(closure)
            },
            onPrintClosure = { closure ->
                val settings = printerSettingsRepository.load()
                runWithPrintPermission(settings.customerPrinter) {
                    scope.launch {
                        val result = container.closurePrinter.printClosureReport(
                            closure.toClosureReportPrintPayload()
                        )
                        printMessage = result.toUserMessage("Ticket Z envoye a l'imprimante.")
                    }
                }
            },
            onExportClosure = { closure ->
                scope.launch {
                    runCatching {
                        cashClosureRepository.ensureArchiveFiles(listOf(closure))
                        val pdfFile = File(
                            context.filesDir,
                            cashClosureRepository.pdfRelativePath(closure)
                        )
                        ClosurePdfExporter.export(
                            payload = closure.toClosureReportPrintPayload(),
                            file = pdfFile
                        )
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            pdfFile
                        )
                        val exportIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/pdf"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            putExtra(Intent.EXTRA_SUBJECT, "Ticket Z ${closure.businessDay}")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            if (context !is Activity) {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                        }
                        context.startActivity(Intent.createChooser(exportIntent, "Exporter le ticket Z en PDF"))
                    }.onSuccess {
                        printMessage = "Export PDF du ticket Z prêt."
                    }.onFailure { error ->
                        printMessage = error.message ?: "Le PDF du ticket Z n'a pas pu être exporté."
                    }
                }
            },
            onBack = { currentDestination = null }
        )

        HomeDestination.PrinterSettings -> PrinterSettingsScreen(
            settings = printerSettings,
            pairedPrinters = pairedPrinters,
            discoveryMessage = printerDiscoveryMessage,
            onRefresh = ::refreshPairedPrinters,
            onSave = { settings: PrinterRoutingSettings ->
                printerSettingsRepository.save(settings)
                printerSettings = settings
            },
            onBack = { currentDestination = null }
        )

    }
}

private fun PrintResult.toUserMessage(successMessage: String): String =
    when (this) {
        PrintResult.Success -> successMessage
        is PrintResult.Failure -> message
    }

private fun List<PrintResult>.toPrintMessage(successMessage: String): String =
    firstOrNull { it is PrintResult.Failure }?.let { (it as PrintResult.Failure).message }
        ?: successMessage

internal fun paidOutputTarget(
    configuredTarget: TicketOutputTarget,
    settlesPendingOrder: Boolean
): TicketOutputTarget =
    if (settlesPendingOrder) TicketOutputTarget.CUSTOMER else configuredTarget

internal fun pendingOutputTarget(
    configuredTarget: TicketOutputTarget,
    updatesPendingOrder: Boolean
): TicketOutputTarget? {
    if (!updatesPendingOrder) return configuredTarget
    return when (configuredTarget) {
        TicketOutputTarget.CUSTOMER,
        TicketOutputTarget.BOTH -> TicketOutputTarget.CUSTOMER
        TicketOutputTarget.KITCHEN -> null
    }
}

private fun TicketOutputTarget.automaticPaidMessage(): String =
    when (this) {
        TicketOutputTarget.CUSTOMER -> "Ticket final envoyé à l'imprimante caisse."
        TicketOutputTarget.KITCHEN -> "Bon cuisine envoyé à l'imprimante cuisine."
        TicketOutputTarget.BOTH -> "Ticket final et bon cuisine envoyés."
    }

private fun TicketOutputTarget.automaticPendingMessage(): String =
    when (this) {
        TicketOutputTarget.CUSTOMER -> "Ticket provisoire envoyé à l'imprimante caisse."
        TicketOutputTarget.KITCHEN -> "Bon cuisine envoyé à l'imprimante cuisine."
        TicketOutputTarget.BOTH -> "Ticket provisoire et bon cuisine envoyés."
    }

private sealed class PrintChoiceRequest {
    data class PendingOrder(val order: OrderTicket) : PrintChoiceRequest()
    data class PaidTicket(val record: HistoryRecord) : PrintChoiceRequest()
}

@Composable
private fun PrintTicketChoiceDialog(
    onDismiss: () -> Unit,
    onSelected: (TicketOutputTarget) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Imprimer") },
        text = { Text("Choisissez le ou les tickets à sortir.") },
        confirmButton = {
            TextButton(onClick = { onSelected(TicketOutputTarget.BOTH) }) {
                Text("Les deux")
            }
        },
        dismissButton = {
            TextButton(onClick = { onSelected(TicketOutputTarget.KITCHEN) }) {
                Text("Cuisine")
            }
            TextButton(onClick = { onSelected(TicketOutputTarget.CUSTOMER) }) {
                Text("Caisse")
            }
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

@Composable
private fun ClosurePasswordDialog(
    onDismiss: () -> Unit,
    onValidated: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var hasError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Accès clôture") },
        text = {
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    hasError = false
                },
                label = { Text("Mot de passe") },
                singleLine = true,
                isError = hasError,
                supportingText = {
                    if (hasError) {
                        Text("Mot de passe incorrect.")
                    } else {
                        Text("Accès réservé à la clôture de caisse.")
                    }
                },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (password == ClosureAccessPassword) {
                        onValidated()
                    } else {
                        hasError = true
                    }
                }
            ) {
                Text("Valider")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
