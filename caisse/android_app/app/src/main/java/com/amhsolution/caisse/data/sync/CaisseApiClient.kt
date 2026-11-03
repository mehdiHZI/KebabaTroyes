package com.amhsolution.caisse.data.sync

import com.amhsolution.caisse.data.local.toCatalogProducts
import com.amhsolution.caisse.data.local.toJsonObject
import com.amhsolution.caisse.data.local.toProduct
import com.amhsolution.caisse.domain.model.Product
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

sealed class CaisseApiResult {
    data object Success : CaisseApiResult()
    data class Failure(val message: String, val retryable: Boolean) : CaisseApiResult()
}

sealed class CatalogFetchResult {
    data class Success(val products: List<Product>, val version: Int) : CatalogFetchResult()
    data class Failure(val message: String, val retryable: Boolean) : CatalogFetchResult()
}

sealed class CatalogUpsertResult {
    data class Success(val product: Product) : CatalogUpsertResult()
    data class Failure(val message: String, val retryable: Boolean) : CatalogUpsertResult()
}

class CaisseApiClient(
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
) {
    private val jsonMedia = "application/json; charset=utf-8".toMediaType()

    fun pushTicket(config: ServerConfig, payloadJson: String): CaisseApiResult {
        if (!config.isConfigured) {
            return CaisseApiResult.Failure("API non configurée", retryable = false)
        }
        val url = "${config.baseUrl.trimEnd('/')}/v1/tickets"
        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer ${config.apiToken}")
            .header("Accept", "application/json")
            .post(payloadJson.toRequestBody(jsonMedia))
            .build()

        return try {
            httpClient.newCall(request).execute().use { response ->
                when (response.code) {
                    200, 201 -> CaisseApiResult.Success
                    401, 403 -> CaisseApiResult.Failure(
                        "Auth API refusée (${response.code})",
                        retryable = false
                    )
                    400 -> CaisseApiResult.Failure(
                        "Payload invalide (400)",
                        retryable = false
                    )
                    else -> CaisseApiResult.Failure(
                        "HTTP ${response.code}",
                        retryable = response.code >= 500 || response.code == 429
                    )
                }
            }
        } catch (e: Exception) {
            CaisseApiResult.Failure(
                e.message?.take(120) ?: "Erreur réseau",
                retryable = true
            )
        }
    }

    fun health(config: ServerConfig): CaisseApiResult {
        if (config.baseUrl.isBlank()) {
            return CaisseApiResult.Failure("URL absente", retryable = false)
        }
        val request = Request.Builder()
            .url("${config.baseUrl.trimEnd('/')}/health")
            .get()
            .build()
        return try {
            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) CaisseApiResult.Success
                else CaisseApiResult.Failure("HTTP ${response.code}", retryable = true)
            }
        } catch (e: Exception) {
            CaisseApiResult.Failure(e.message?.take(120) ?: "Hors ligne", retryable = true)
        }
    }

    fun fetchCatalog(config: ServerConfig): CatalogFetchResult {
        if (!config.isConfigured) {
            return CatalogFetchResult.Failure("API non configurée", retryable = false)
        }
        val request = Request.Builder()
            .url("${config.baseUrl.trimEnd('/')}/v1/catalog")
            .header("Authorization", "Bearer ${config.apiToken}")
            .header("Accept", "application/json")
            .get()
            .build()
        return try {
            httpClient.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                when (response.code) {
                    200 -> {
                        val json = JSONObject(body)
                        val array = json.optJSONArray("products") ?: JSONArray()
                        val products = array.toString().toCatalogProducts()
                        CatalogFetchResult.Success(
                            products = products,
                            version = json.optInt("version", 1)
                        )
                    }
                    401, 403 -> CatalogFetchResult.Failure(
                        "Auth API refusée (${response.code})",
                        retryable = false
                    )
                    else -> CatalogFetchResult.Failure(
                        "HTTP ${response.code}",
                        retryable = response.code >= 500 || response.code == 429
                    )
                }
            }
        } catch (e: Exception) {
            CatalogFetchResult.Failure(
                e.message?.take(120) ?: "Erreur réseau",
                retryable = true
            )
        }
    }

    fun upsertProduct(config: ServerConfig, product: Product): CatalogUpsertResult {
        if (!config.isConfigured) {
            return CatalogUpsertResult.Failure("API non configurée", retryable = false)
        }
        val payload = product.toJsonObject().toString()
        val request = Request.Builder()
            .url("${config.baseUrl.trimEnd('/')}/v1/catalog/products/${product.id}")
            .header("Authorization", "Bearer ${config.apiToken}")
            .header("Accept", "application/json")
            .put(payload.toRequestBody(jsonMedia))
            .build()
        return try {
            httpClient.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                when (response.code) {
                    200, 201 -> {
                        val json = JSONObject(body)
                        val saved = json.optJSONObject("product")?.toProduct()
                            ?: product
                        CatalogUpsertResult.Success(saved)
                    }
                    401, 403 -> CatalogUpsertResult.Failure(
                        "Auth API refusée (${response.code})",
                        retryable = false
                    )
                    400 -> CatalogUpsertResult.Failure(
                        runCatching {
                            JSONObject(body).optString("error").ifBlank { "Payload invalide" }
                        }.getOrDefault("Payload invalide"),
                        retryable = false
                    )
                    else -> CatalogUpsertResult.Failure(
                        "HTTP ${response.code}",
                        retryable = response.code >= 500 || response.code == 429
                    )
                }
            }
        } catch (e: Exception) {
            CatalogUpsertResult.Failure(
                e.message?.take(120) ?: "Erreur réseau",
                retryable = true
            )
        }
    }

    fun createProduct(config: ServerConfig, product: Product): CatalogUpsertResult {
        if (!config.isConfigured) {
            return CatalogUpsertResult.Failure("API non configurée", retryable = false)
        }
        val payload = product.toJsonObject().toString()
        val request = Request.Builder()
            .url("${config.baseUrl.trimEnd('/')}/v1/catalog/products")
            .header("Authorization", "Bearer ${config.apiToken}")
            .header("Accept", "application/json")
            .post(payload.toRequestBody(jsonMedia))
            .build()
        return try {
            httpClient.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                when (response.code) {
                    200, 201 -> {
                        val json = JSONObject(body)
                        val saved = json.optJSONObject("product")?.toProduct()
                            ?: return CatalogUpsertResult.Failure("Réponse invalide", false)
                        CatalogUpsertResult.Success(saved)
                    }
                    401, 403 -> CatalogUpsertResult.Failure(
                        "Auth API refusée (${response.code})",
                        retryable = false
                    )
                    400 -> CatalogUpsertResult.Failure(
                        runCatching {
                            JSONObject(body).optString("error").ifBlank { "Payload invalide" }
                        }.getOrDefault("Payload invalide"),
                        retryable = false
                    )
                    else -> CatalogUpsertResult.Failure(
                        "HTTP ${response.code}",
                        retryable = response.code >= 500 || response.code == 429
                    )
                }
            }
        } catch (e: Exception) {
            CatalogUpsertResult.Failure(
                e.message?.take(120) ?: "Erreur réseau",
                retryable = true
            )
        }
    }
}
