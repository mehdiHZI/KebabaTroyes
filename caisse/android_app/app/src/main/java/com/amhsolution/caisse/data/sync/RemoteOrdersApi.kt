package com.amhsolution.caisse.data.sync

import com.amhsolution.caisse.domain.model.DeliveryInfo
import com.amhsolution.caisse.domain.model.OrderLineSnapshot
import com.amhsolution.caisse.domain.model.OrderTicket
import com.amhsolution.caisse.domain.model.OrderType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.util.concurrent.TimeUnit

data class RemoteChannelOrder(
    val orderId: String,
    val channel: String,
    val channelLabel: String,
    val status: String,
    val paidOnline: Boolean,
    val totalCentimes: Int,
    val customerName: String,
    val phone: String,
    val address: String,
    val notes: String,
    val lines: List<OrderLineSnapshot>,
    val createdAtEpochMillis: Long
) {
    fun toOrderTicket(ticketNumber: Int): OrderTicket {
        val status = buildString {
            append(channelLabel)
            if (paidOnline) append(" · payé en ligne")
            if (notes.isNotBlank()) append(" · ").append(notes.take(40))
        }
        return OrderTicket(
            id = orderId,
            ticketNumber = ticketNumber,
            orderType = OrderType.LIVRAISON,
            lines = lines,
            totalCentimes = totalCentimes,
            createdAtEpochMillis = createdAtEpochMillis,
            statusLabel = status,
            deliveryInfo = DeliveryInfo(
                customerName = customerName.ifBlank { channelLabel },
                address = address.ifBlank { "Livraison $channelLabel" },
                driverNumber = when (channel) {
                    "uber_eats" -> 801
                    "deliveroo" -> 802
                    else -> 800
                },
                phoneNumber = phone
            )
        )
    }
}

class RemoteOrdersApi(
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
) {
    private val jsonMedia = "application/json; charset=utf-8".toMediaType()

    fun fetchOpenOrders(config: ServerConfig): Result<List<RemoteChannelOrder>> {
        if (!config.isConfigured) {
            return Result.failure(IllegalStateException("API non configurée"))
        }
        val request = Request.Builder()
            .url("${config.baseUrl.trimEnd('/')}/v1/orders?status=open")
            .header("Authorization", "Bearer ${config.apiToken}")
            .header("Accept", "application/json")
            .get()
            .build()

        return try {
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return Result.failure(IllegalStateException("HTTP ${response.code}"))
                }
                val body = response.body?.string().orEmpty()
                val root = JSONObject(body)
                val array = root.optJSONArray("orders") ?: JSONArray()
                val list = buildList {
                    for (i in 0 until array.length()) {
                        parseOrder(array.getJSONObject(i))?.let { add(it) }
                    }
                }
                Result.success(list)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun ackOrder(
        config: ServerConfig,
        orderId: String,
        status: String = "accepted"
    ): CaisseApiResult {
        if (!config.isConfigured) {
            return CaisseApiResult.Failure("API non configurée", retryable = false)
        }
        val payload = JSONObject()
            .put("status", status)
            .put("registerId", config.registerId)
            .toString()
        val request = Request.Builder()
            .url("${config.baseUrl.trimEnd('/')}/v1/orders/${orderId}/ack")
            .header("Authorization", "Bearer ${config.apiToken}")
            .post(payload.toRequestBody(jsonMedia))
            .build()
        return try {
            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) CaisseApiResult.Success
                else CaisseApiResult.Failure("HTTP ${response.code}", retryable = response.code >= 500)
            }
        } catch (e: Exception) {
            CaisseApiResult.Failure(e.message?.take(120) ?: "Erreur réseau", retryable = true)
        }
    }

    fun cancelOrder(config: ServerConfig, orderId: String): CaisseApiResult {
        if (!config.isConfigured) {
            return CaisseApiResult.Failure("API non configurée", retryable = false)
        }
        val request = Request.Builder()
            .url("${config.baseUrl.trimEnd('/')}/v1/orders/${orderId}/cancel")
            .header("Authorization", "Bearer ${config.apiToken}")
            .post("{}".toRequestBody(jsonMedia))
            .build()
        return try {
            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) CaisseApiResult.Success
                else CaisseApiResult.Failure("HTTP ${response.code}", retryable = response.code >= 500)
            }
        } catch (e: Exception) {
            CaisseApiResult.Failure(e.message?.take(120) ?: "Erreur réseau", retryable = true)
        }
    }

    private fun parseOrder(json: JSONObject): RemoteChannelOrder? {
        val orderId = json.optString("orderId").ifBlank { return null }
        val linesJson = json.optJSONArray("lines") ?: JSONArray()
        val lines = buildList {
            for (i in 0 until linesJson.length()) {
                val line = linesJson.getJSONObject(i)
                val qty = line.optInt("quantity", 1).coerceAtLeast(1)
                val total = line.optInt("totalCentimes", 0)
                add(
                    OrderLineSnapshot(
                        productName = line.optString("name", "Article"),
                        quantity = qty,
                        totalCentimes = total,
                        productId = line.optString("productId").takeIf { it.isNotBlank() },
                        lineId = line.optString("lineId").takeIf { it.isNotBlank() },
                        unitPriceCentimes = line.optInt("unitPriceCentimes", if (qty > 0) total / qty else total)
                    )
                )
            }
        }
        if (lines.isEmpty()) return null
        val createdAt = json.optString("createdAt").ifBlank { json.optString("receivedAt") }
        val epoch = runCatching { Instant.parse(createdAt).toEpochMilli() }
            .getOrElse { System.currentTimeMillis() }

        return RemoteChannelOrder(
            orderId = orderId,
            channel = json.optString("channel", "manual"),
            channelLabel = json.optString("channelLabel", "Externe"),
            status = json.optString("status", "open"),
            paidOnline = json.optBoolean("paidOnline", true),
            totalCentimes = json.optInt("totalCentimes", lines.sumOf { it.totalCentimes }),
            customerName = json.optString("customerName"),
            phone = json.optString("phone"),
            address = json.optString("address"),
            notes = json.optString("notes"),
            lines = lines,
            createdAtEpochMillis = epoch
        )
    }
}
