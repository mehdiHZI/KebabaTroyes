package com.amhsolution.caisse.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.amhsolution.caisse.data.local.toCatalogProducts
import com.amhsolution.caisse.data.local.toCatalogProductsJson
import com.amhsolution.caisse.data.local.toPriceOverrides
import com.amhsolution.caisse.data.local.toPriceOverridesJson
import com.amhsolution.caisse.data.sync.CaisseApiClient
import com.amhsolution.caisse.data.sync.CatalogFetchResult
import com.amhsolution.caisse.data.sync.CatalogUpsertResult
import com.amhsolution.caisse.data.sync.ServerConfigRepository
import com.amhsolution.caisse.domain.catalog.PizzaCatalog
import com.amhsolution.caisse.domain.catalog.ProductOptionCatalog
import com.amhsolution.caisse.domain.catalog.SampleCatalog
import com.amhsolution.caisse.domain.model.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private val Context.catalogDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "catalog_preferences"
)

private val CATALOG_PRODUCTS_KEY = stringPreferencesKey("catalog_products_json")
private val CATALOG_VERSION_KEY = intPreferencesKey("catalog_version")
private val PRICE_OVERRIDES_KEY = stringPreferencesKey("price_overrides_json")
private val PIZZA_SUPPLEMENT_PRICE_OVERRIDES_KEY =
    stringPreferencesKey("pizza_supplement_price_overrides_json")
private val PRODUCT_SUPPLEMENT_PRICE_OVERRIDES_KEY =
    stringPreferencesKey("product_supplement_price_overrides_json")
private val PIZZA_SIZE_OFFSET_OVERRIDES_KEY =
    stringPreferencesKey("pizza_size_offset_overrides_json")

class CatalogRepository(
    context: Context,
    private val serverConfigRepository: ServerConfigRepository,
    private val apiClient: CaisseApiClient = CaisseApiClient()
) {
    private val dataStore = context.catalogDataStore

    val products: Flow<List<Product>> = dataStore.data
        .map { preferences ->
            val cached = preferences[CATALOG_PRODUCTS_KEY]?.toCatalogProducts().orEmpty()
            val base = cached.ifEmpty { SampleCatalog.products }
            val overrides = preferences[PRICE_OVERRIDES_KEY]?.toPriceOverrides().orEmpty()
            if (overrides.isEmpty()) {
                base
            } else {
                base.map { product ->
                    product.copy(priceCentimes = overrides[product.id] ?: product.priceCentimes)
                }
            }
        }
        .distinctUntilChanged()
        .flowOn(Dispatchers.Default)

    /** Produits visibles en caisse (activés uniquement). */
    val enabledProducts: Flow<List<Product>> =
        products.map { list -> list.filter { it.enabled } }
            .distinctUntilChanged()

    val pizzaSupplementPrices: Flow<Map<String, Int>> = dataStore.data
        .map { preferences ->
            val overrides = preferences[PIZZA_SUPPLEMENT_PRICE_OVERRIDES_KEY]
                ?.toPriceOverrides()
                .orEmpty()
            PizzaCatalog.defaultSupplementPrices + overrides
        }
        .distinctUntilChanged()
        .flowOn(Dispatchers.Default)

    val productSupplementPrices: Flow<Map<String, Int>> = dataStore.data
        .map { preferences ->
            val overrides = preferences[PRODUCT_SUPPLEMENT_PRICE_OVERRIDES_KEY]
                ?.toPriceOverrides()
                .orEmpty()
            ProductOptionCatalog.defaultSupplementPrices + overrides
        }
        .distinctUntilChanged()
        .flowOn(Dispatchers.Default)

    val pizzaSizeOffsets: Flow<Map<String, Int>> = dataStore.data
        .map { preferences ->
            val overrides = preferences[PIZZA_SIZE_OFFSET_OVERRIDES_KEY]
                ?.toPriceOverrides()
                .orEmpty()
            PizzaCatalog.defaultSizeOffsets + overrides
        }
        .distinctUntilChanged()
        .flowOn(Dispatchers.Default)

    /**
     * Tire le catalogue VPS et met à jour le cache local.
     * Migre les anciens overrides de prix vers les produits puis les efface.
     */
    suspend fun pullFromServer(): Result<Int> = withContext(Dispatchers.IO) {
        val config = serverConfigRepository.config.first()
        when (val result = apiClient.fetchCatalog(config)) {
            is CatalogFetchResult.Failure -> Result.failure(IllegalStateException(result.message))
            is CatalogFetchResult.Success -> {
                if (result.products.isEmpty()) {
                    return@withContext Result.failure(
                        IllegalStateException("Catalogue serveur vide")
                    )
                }
                val prefs = dataStore.data.first()
                val overrides = prefs[PRICE_OVERRIDES_KEY]?.toPriceOverrides().orEmpty()
                val merged = if (overrides.isEmpty()) {
                    result.products
                } else {
                    result.products.map { product ->
                        product.copy(
                            priceCentimes = overrides[product.id] ?: product.priceCentimes
                        )
                    }
                }
                dataStore.edit { preferences ->
                    preferences[CATALOG_PRODUCTS_KEY] = merged.toCatalogProductsJson()
                    preferences[CATALOG_VERSION_KEY] = result.version
                    if (overrides.isNotEmpty()) {
                        preferences.remove(PRICE_OVERRIDES_KEY)
                    }
                }
                // Pousse les prix migrés vers le serveur (best-effort)
                if (overrides.isNotEmpty()) {
                    for (product in merged) {
                        if (product.id in overrides) {
                            apiClient.upsertProduct(config, product)
                        }
                    }
                }
                Result.success(result.products.size)
            }
        }
    }

    suspend fun upsertProduct(product: Product): Result<Product> = withContext(Dispatchers.IO) {
        require(product.name.isNotBlank()) { "Le nom est obligatoire." }
        require(product.priceCentimes > 0) { "Le prix doit etre superieur a zero." }
        val config = serverConfigRepository.config.first()
        if (!config.isConfigured) {
            return@withContext Result.failure(
                IllegalStateException("Configurez d'abord l'URL et le token serveur.")
            )
        }
        val withId = if (product.id.isBlank()) {
            product.copy(id = slugify(product.name))
        } else {
            product
        }
        when (val remote = apiClient.upsertProduct(config, withId)) {
            is CatalogUpsertResult.Failure ->
                Result.failure(IllegalStateException(remote.message))
            is CatalogUpsertResult.Success -> {
                replaceCachedProduct(remote.product)
                Result.success(remote.product)
            }
        }
    }

    private fun slugify(name: String): String {
        val ascii = java.text.Normalizer
            .normalize(name.trim(), java.text.Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
            .lowercase()
        return ascii
            .replace(Regex("[^a-z0-9]+"), "-")
            .replace(Regex("^-+|-+$"), "")
            .ifBlank { "produit-${System.currentTimeMillis()}" }
            .take(64)
    }

    suspend fun setEnabled(productId: String, enabled: Boolean): Result<Product> {
        val current = products.first().firstOrNull { it.id == productId }
            ?: return Result.failure(IllegalArgumentException("Article introuvable."))
        return upsertProduct(current.copy(enabled = enabled))
    }

    /** Compat ancien écran Prix : met à jour le prix seul via upsert serveur. */
    suspend fun updatePrice(productId: String, priceCentimes: Int) {
        require(priceCentimes > 0) { "Le prix doit etre superieur a zero." }
        val current = products.first().firstOrNull { it.id == productId }
        if (current != null) {
            upsertProduct(current.copy(priceCentimes = priceCentimes)).getOrThrow()
            return
        }
        dataStore.edit { preferences ->
            val map = preferences[PRICE_OVERRIDES_KEY]?.toPriceOverrides()?.toMutableMap()
                ?: mutableMapOf()
            map[productId] = priceCentimes
            preferences[PRICE_OVERRIDES_KEY] = map.toPriceOverridesJson()
        }
    }

    suspend fun updatePizzaSizeOffset(sizeKey: String, offsetCentimes: Int) {
        require(offsetCentimes >= 0) { "La majoration ne peut pas etre negative." }
        require(sizeKey in PizzaCatalog.defaultSizeOffsets) { "Taille invalide." }

        dataStore.edit { preferences ->
            val current = preferences[PIZZA_SIZE_OFFSET_OVERRIDES_KEY]
                ?.toPriceOverrides()
                ?.toMutableMap()
                ?: mutableMapOf()
            current[sizeKey] = offsetCentimes
            preferences[PIZZA_SIZE_OFFSET_OVERRIDES_KEY] = current.toPriceOverridesJson()
        }
    }

    suspend fun updatePizzaSupplementPrice(ingredientId: String, priceCentimes: Int) {
        require(priceCentimes > 0) { "Le prix doit etre superieur a zero." }
        require(PizzaCatalog.ingredientFor(ingredientId) != null) { "Ingredient inconnu." }

        dataStore.edit { preferences ->
            val current = preferences[PIZZA_SUPPLEMENT_PRICE_OVERRIDES_KEY]
                ?.toPriceOverrides()
                ?.toMutableMap()
                ?: mutableMapOf()
            current[ingredientId] = priceCentimes
            preferences[PIZZA_SUPPLEMENT_PRICE_OVERRIDES_KEY] = current.toPriceOverridesJson()
        }
    }

    suspend fun updateProductSupplementPrice(priceKey: String, priceCentimes: Int) {
        require(priceCentimes > 0) { "Le prix doit etre superieur a zero." }
        require(priceKey in ProductOptionCatalog.defaultSupplementPrices) {
            "Supplement inconnu."
        }

        dataStore.edit { preferences ->
            val current = preferences[PRODUCT_SUPPLEMENT_PRICE_OVERRIDES_KEY]
                ?.toPriceOverrides()
                ?.toMutableMap()
                ?: mutableMapOf()
            current[priceKey] = priceCentimes
            preferences[PRODUCT_SUPPLEMENT_PRICE_OVERRIDES_KEY] = current.toPriceOverridesJson()
        }
    }

    private suspend fun replaceCachedProduct(product: Product) {
        dataStore.edit { preferences ->
            val current = preferences[CATALOG_PRODUCTS_KEY]?.toCatalogProducts()?.toMutableList()
                ?: SampleCatalog.products.toMutableList()
            val index = current.indexOfFirst { it.id == product.id }
            if (index >= 0) {
                current[index] = product
            } else {
                current.add(product)
            }
            preferences[CATALOG_PRODUCTS_KEY] = current.toCatalogProductsJson()
        }
    }
}
