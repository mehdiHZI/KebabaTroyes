package com.amhsolution.caisse.data.sync

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

sealed class CaisseApiResult {
    data object Success : CaisseApiResult()
    data class Failure(val message: String, val retryable: Boolean) : CaisseApiResult()
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
}
