package com.amhsolution.caisse.data

import android.content.Context
import com.amhsolution.caisse.CaisseApplication
import com.amhsolution.caisse.data.auth.ResponsibleAuthRepository
import com.amhsolution.caisse.data.auth.SimulationModeRepository
import com.amhsolution.caisse.data.local.CaisseDatabase
import com.amhsolution.caisse.data.printing.PrinterSettingsRepository
import com.amhsolution.caisse.data.printing.ReceiptPrinter
import com.amhsolution.caisse.data.repository.CashClosureRepository
import com.amhsolution.caisse.data.repository.CatalogRepository
import com.amhsolution.caisse.data.repository.FiscalArchiveRepository
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
import com.amhsolution.caisse.domain.closure.PerpetualTotals
import com.amhsolution.caisse.domain.model.HistoryRecord
import com.amhsolution.caisse.domain.model.Product
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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
    val simulationModeRepository = SimulationModeRepository(appContext)
    val responsibleAuthRepository = ResponsibleAuthRepository(appContext)
    val historyRepository = HistoryRepository(
        database = database,
        isSimulationMode = {
            runBlocking { simulationModeRepository.isEnabledNow() }
        }
    )
    val cashClosureRepository = CashClosureRepository(database, File(appContext.filesDir, "cloture"))
    val fiscalArchiveRepository = FiscalArchiveRepository(
        database = database,
        archiveRoot = File(appContext.filesDir, "archives")
    )
    val pendingOrderRepository = PendingOrderRepository(File(appContext.filesDir, "pending_orders.json"))
    val printerSettingsRepository = PrinterSettingsRepository(appContext)
    val serverConfigRepository = ServerConfigRepository(appContext)
    val catalogRepository = CatalogRepository(appContext, serverConfigRepository)
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

    val simulationEnabled: StateFlow<Boolean> =
        simulationModeRepository.isEnabled
            .stateIn(scope, SharingStarted.Eagerly, false)

    val perpetualTotals: StateFlow<PerpetualTotals> =
        database.perpetualTotalsDao().observe()
            .map { entity ->
                PerpetualTotals(
                    totalRevenueCentimes = entity?.totalRevenueCentimes ?: 0L,
                    ticketCount = entity?.ticketCount ?: 0L,
                    correctionCount = entity?.correctionCount ?: 0L,
                    lastUpdatedEpochMillis = entity?.lastUpdatedEpochMillis ?: 0L,
                    integrityHash = entity?.integrityHash.orEmpty()
                )
            }
            .stateIn(scope, SharingStarted.Eagerly, PerpetualTotals())

    val catalogProducts: StateFlow<List<Product>> =
        catalogRepository.enabledProducts
            .stateIn(scope, SharingStarted.Eagerly, SampleCatalog.products.filter { it.enabled })

    /** Tous les articles (y compris désactivés) pour l'admin catalogue. */
    val allCatalogProducts: StateFlow<List<Product>> =
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
            runCatching { serverConfigRepository.migratePlaintextTokenIfNeeded() }
        }
        scope.launch {
            runCatching { catalogRepository.pullFromServer() }
            while (true) {
                kotlinx.coroutines.delay(60_000)
                runCatching { catalogRepository.pullFromServer() }
            }
        }
        scope.launch {
            runCatching { ticketSyncRepository.probeAuth() }
            while (true) {
                try {
                    ticketSyncRepository.flush()
                } catch (_: Exception) {
                    // retry later
                }
                kotlinx.coroutines.delay(30_000)
                runCatching { ticketSyncRepository.probeAuth() }
            }
        }
    }
}

fun Context.appContainer(): AppContainer =
    (applicationContext as CaisseApplication).container
