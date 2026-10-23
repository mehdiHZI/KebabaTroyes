package com.amhsolution.caisse.data

import android.content.Context
import com.amhsolution.caisse.CaisseApplication
import com.amhsolution.caisse.data.local.CaisseDatabase
import com.amhsolution.caisse.data.printing.PrinterSettingsRepository
import com.amhsolution.caisse.data.printing.ReceiptPrinter
import com.amhsolution.caisse.data.repository.CashClosureRepository
import com.amhsolution.caisse.data.repository.CatalogRepository
import com.amhsolution.caisse.data.repository.HistoryRepository
import com.amhsolution.caisse.data.repository.PendingOrderRepository
import com.amhsolution.caisse.data.sync.RemoteOrdersRepository
import com.amhsolution.caisse.data.sync.ServerConfigRepository
import com.amhsolution.caisse.data.sync.SyncStatusSnapshot
import com.amhsolution.caisse.data.sync.SyncUiState
import com.amhsolution.caisse.data.sync.TicketSyncRepository
import com.amhsolution.caisse.domain.catalog.PizzaCatalog
import com.amhsolution.caisse.domain.catalog.ProductOptionCatalog
import com.amhsolution.caisse.domain.catalog.SampleCatalog
import com.amhsolution.caisse.domain.closure.CashClosure
import com.amhsolution.caisse.domain.model.HistoryRecord
import com.amhsolution.caisse.domain.model.Product
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

/**
 * Données partagées et préchargées au lancement de l'application.
 * Les [StateFlow] en [SharingStarted.Eagerly] démarrent la lecture Room / DataStore
 * dès [CaisseApplication.onCreate], avant l'affichage de l'interface.
 */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database: CaisseDatabase = CaisseDatabase.getInstance(appContext)
    val historyRepository = HistoryRepository(database)
    val cashClosureRepository = CashClosureRepository(database, File(appContext.filesDir, "cloture"))
    val pendingOrderRepository = PendingOrderRepository(File(appContext.filesDir, "pending_orders.json"))
    val catalogRepository = CatalogRepository(appContext)
    val printerSettingsRepository = PrinterSettingsRepository(appContext)
    val serverConfigRepository = ServerConfigRepository(appContext)
    val ticketSyncRepository = TicketSyncRepository(database, serverConfigRepository)
    val remoteOrdersRepository = RemoteOrdersRepository(serverConfigRepository)
    val customerTicketPrinter = ReceiptPrinter(appContext) {
        printerSettingsRepository.load().customerPrinter
    }
    val kitchenTicketPrinter = ReceiptPrinter(appContext) {
        printerSettingsRepository.load().kitchenPrinter
    }
    val closurePrinter = customerTicketPrinter

    val historyRecords: StateFlow<List<HistoryRecord>> =
        historyRepository.observeHistory()
            .stateIn(scope, SharingStarted.Eagerly, emptyList())

    val cashClosures: StateFlow<List<CashClosure>> =
        cashClosureRepository.observeClosures()
            .stateIn(scope, SharingStarted.Eagerly, emptyList())

    val catalogProducts: StateFlow<List<Product>> =
        catalogRepository.products
            .stateIn(scope, SharingStarted.Eagerly, SampleCatalog.products)

    val pizzaSupplementPrices: StateFlow<Map<String, Int>> =
        catalogRepository.pizzaSupplementPrices
            .stateIn(scope, SharingStarted.Eagerly, PizzaCatalog.defaultSupplementPrices)

    val pizzaSizeOffsets: StateFlow<Map<String, Int>> =
        catalogRepository.pizzaSizeOffsets
            .stateIn(scope, SharingStarted.Eagerly, PizzaCatalog.defaultSizeOffsets)

    val productSupplementPrices: StateFlow<Map<String, Int>> =
        catalogRepository.productSupplementPrices
            .stateIn(scope, SharingStarted.Eagerly, ProductOptionCatalog.defaultSupplementPrices)

    val syncStatus: StateFlow<SyncStatusSnapshot> =
        ticketSyncRepository.status
            .stateIn(
                scope,
                SharingStarted.Eagerly,
                SyncStatusSnapshot(SyncUiState.DISABLED, 0, 0, "API non configurée")
            )

    init {
        scope.launch {
            while (true) {
                try {
                    ticketSyncRepository.flush()
                } catch (_: Exception) {
                    // retry later
                }
                kotlinx.coroutines.delay(30_000)
            }
        }
    }
}

fun Context.appContainer(): AppContainer =
    (applicationContext as CaisseApplication).container
