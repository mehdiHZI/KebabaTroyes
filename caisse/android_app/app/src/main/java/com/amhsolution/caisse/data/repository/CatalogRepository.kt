package com.amhsolution.caisse.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.amhsolution.caisse.data.local.toPriceOverrides
import com.amhsolution.caisse.data.local.toPriceOverridesJson
import com.amhsolution.caisse.domain.catalog.SampleCatalog
import com.amhsolution.caisse.domain.catalog.PizzaCatalog
import com.amhsolution.caisse.domain.catalog.ProductOptionCatalog
import com.amhsolution.caisse.domain.model.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

private val Context.catalogDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "catalog_preferences"
)

private val PRICE_OVERRIDES_KEY = stringPreferencesKey("price_overrides_json")
private val PIZZA_SUPPLEMENT_PRICE_OVERRIDES_KEY =
    stringPreferencesKey("pizza_supplement_price_overrides_json")
private val PRODUCT_SUPPLEMENT_PRICE_OVERRIDES_KEY =
    stringPreferencesKey("product_supplement_price_overrides_json")
private val PIZZA_SIZE_OFFSET_OVERRIDES_KEY =
    stringPreferencesKey("pizza_size_offset_overrides_json")

class CatalogRepository(
    context: Context
) {
    private val dataStore = context.catalogDataStore

    val products: Flow<List<Product>> = dataStore.data
        .map { preferences ->
            val overrides = preferences[PRICE_OVERRIDES_KEY]?.toPriceOverrides().orEmpty()
            SampleCatalog.products.map { product ->
                product.copy(priceCentimes = overrides[product.id] ?: product.priceCentimes)
            }
        }
        .distinctUntilChanged()
        .flowOn(Dispatchers.Default)

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

    suspend fun updatePrice(productId: String, priceCentimes: Int) {
        require(priceCentimes > 0) { "Le prix doit etre superieur a zero." }

        dataStore.edit { preferences ->
            val current = preferences[PRICE_OVERRIDES_KEY]?.toPriceOverrides()?.toMutableMap()
                ?: mutableMapOf()
            current[productId] = priceCentimes
            preferences[PRICE_OVERRIDES_KEY] = current.toPriceOverridesJson()
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
}
