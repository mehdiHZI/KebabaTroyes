package com.amhsolution.caisse.data.local

import com.amhsolution.caisse.domain.closure.CashClosure
import com.amhsolution.caisse.domain.closure.CashClosureType
import com.amhsolution.caisse.domain.model.OrderType
import com.amhsolution.caisse.domain.model.PaymentMethod
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val closureDateTimeFormatter =
    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss", Locale.FRANCE)

fun CashClosure.toArchiveJsonString(zoneId: ZoneId = ZoneId.systemDefault()): String {
    val closedAt = Instant.ofEpochMilli(closedAtEpochMillis).atZone(zoneId)

    return JSONObject().apply {
        put("documentType", type.archiveDocumentType())
        put("version", 1)
        put("restaurant", JSONObject().apply {
            put("name", "Kebaba")
            put("cashRegister", "Caisse principale")
        })
        put("closure", JSONObject().apply {
            put("id", id)
            put("type", type.name)
            put("label", type.label)
            put("periodKey", businessDay)
            put("businessDay", if (type == CashClosureType.DAILY_Z) businessDay else JSONObject.NULL)
            put("printedAt", closedAt.format(closureDateTimeFormatter))
            put("closedAtEpochMillis", closedAtEpochMillis)
            put("cashier", "Kebaba Caisse")
            put("isFinal", isFinal)
            put("statusLabel", statusLabel)
        })
        put("general", JSONObject().apply {
            put("ticketCount", ticketCount)
            put("firstTicketNumber", firstTicketNumber ?: JSONObject.NULL)
            put("lastTicketNumber", lastTicketNumber ?: JSONObject.NULL)
            put("ticketRange", ticketRangeLabel)
            put("totalTicketsCentimes", totalRevenueCentimes)
            put("totalCollectedCentimes", totalRevenueCentimes)
            put("itemCount", itemCount)
            put("cancelledOrderCount", cancelledOrderCount)
        })
        put("payments", paymentMethodsJson())
        put("orderTypes", orderTypesJson())
        put("revenue", JSONObject().apply {
            put("totalTtcCentimes", totalRevenueCentimes)
            put("totalHtCentimes", JSONObject.NULL)
            put("vatLines", JSONArray())
            put("note", "TVA non detaillee dans la V1.")
        })
    }.toString(2)
}

private fun CashClosureType.archiveDocumentType(): String =
    when (this) {
        CashClosureType.DAILY_Z -> "CASH_CLOSURE_DAILY_Z"
        CashClosureType.MONTHLY_Z -> "CASH_CLOSURE_MONTHLY_Z"
        CashClosureType.YEARLY_Z -> "CASH_CLOSURE_YEARLY_Z"
    }

private fun CashClosure.paymentMethodsJson(): JSONArray =
    JSONArray().apply {
        listOf(
            PaymentMethod.CARTE_BANCAIRE,
            PaymentMethod.ESPECES,
            PaymentMethod.TITRE_RESTAURANT,
            PaymentMethod.AUTRE
        ).forEach { method ->
            put(JSONObject().apply {
                put("code", method.closureCode())
                put("label", method.label)
                put("count", paymentCounts[method] ?: 0)
                put("amountCentimes", paymentTotalsCentimes[method] ?: 0)
            })
        }
    }

private fun CashClosure.orderTypesJson(): JSONArray =
    JSONArray().apply {
        OrderType.entries.forEach { type ->
            put(JSONObject().apply {
                put("code", type.name)
                put("label", type.label)
                put("amountCentimes", orderTypeTotalsCentimes[type] ?: 0)
            })
        }
    }

private fun PaymentMethod.closureCode(): String =
    when (this) {
        PaymentMethod.CARTE_BANCAIRE -> "CB"
        PaymentMethod.ESPECES -> "ESP"
        PaymentMethod.TITRE_RESTAURANT -> "TR"
        PaymentMethod.AUTRE -> "AUTRE"
        PaymentMethod.ANNULE -> "ANNULE"
    }
