package com.amhsolution.caisse.domain.printing

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

private const val ClosurePrintSchemaVersion = 1

data class ClosureReportPrintPayload(
    val schemaVersion: Int,
    val documentType: TicketPrintDocumentType,
    val restaurantName: String,
    val cashRegisterName: String,
    val cashierName: String,
    val closure: CashClosure,
    val printer: TicketPrinterOptions
)

fun CashClosure.toClosureReportPrintPayload(
    restaurantName: String = "Kebaba",
    cashRegisterName: String = "Caisse principale",
    cashierName: String = "Kebaba Caisse",
    printer: TicketPrinterOptions = MilanoGustoPrinterConfig.closureReportOptions()
): ClosureReportPrintPayload =
    ClosureReportPrintPayload(
        schemaVersion = ClosurePrintSchemaVersion,
        documentType = TicketPrintDocumentType.CLOSURE_REPORT,
        restaurantName = restaurantName,
        cashRegisterName = cashRegisterName,
        cashierName = cashierName,
        closure = this,
        printer = printer
    )

fun ClosureReportPrintPayload.toJsonString(zoneId: ZoneId = ZoneId.systemDefault()): String {
    val closedAt = Instant.ofEpochMilli(closure.closedAtEpochMillis).atZone(zoneId)
    return JSONObject()
        .put("schemaVersion", schemaVersion)
        .put("documentType", documentType.name)
        .put(
            "restaurant",
            JSONObject()
                .put("name", restaurantName)
                .put("cashRegister", cashRegisterName)
        )
        .put(
            "closure",
            JSONObject()
                .put("id", closure.id)
                .put("type", closure.type.name)
                .put("label", closure.type.label)
                .put("periodKey", closure.businessDay)
                .put("printedAt", closedAt.format(closureDateTimeFormatter))
                .put("closedAtEpochMillis", closure.closedAtEpochMillis)
                .put("cashier", cashierName)
        )
        .put(
            "general",
            JSONObject()
                .put("ticketCount", closure.ticketCount)
                .put("firstTicketNumber", closure.firstTicketNumber ?: JSONObject.NULL)
                .put("lastTicketNumber", closure.lastTicketNumber ?: JSONObject.NULL)
                .put("ticketRange", closure.ticketRangeLabel)
                .put("totalTicketsCentimes", closure.totalRevenueCentimes)
                .put("totalCollectedCentimes", closure.totalRevenueCentimes)
                .put("itemCount", closure.itemCount)
                .put("cancelledOrderCount", closure.cancelledOrderCount)
        )
        .put("payments", closure.paymentMethodsJsonArray())
        .put("orderTypes", closure.orderTypesJsonArray())
        .put(
            "revenue",
            JSONObject()
                .put("totalTtcCentimes", closure.totalRevenueCentimes)
                .put("totalHtCentimes", JSONObject.NULL)
                .put("vatLines", JSONArray())
                .put("note", "TVA non detaillee dans la V1.")
        )
        .put("printer", printer.toJsonObject())
        .toString(2)
}

internal fun PaymentMethod.closurePrintCode(): String =
    when (this) {
        PaymentMethod.CARTE_BANCAIRE -> "CB"
        PaymentMethod.ESPECES -> "ESP"
        PaymentMethod.TITRE_RESTAURANT -> "TR"
        PaymentMethod.AUTRE -> "AUTRE"
        PaymentMethod.ANNULE -> "ANNULE"
    }

internal fun CashClosureType.printShortLabel(): String =
    when (this) {
        CashClosureType.DAILY_Z -> "TICKET Z JOURNALIER"
        CashClosureType.MONTHLY_Z -> "TICKET Z MENSUEL"
        CashClosureType.YEARLY_Z -> "TICKET Z ANNUEL"
    }

private val closureDateTimeFormatter =
    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss", Locale.FRANCE)

private fun CashClosure.paymentMethodsJsonArray(): JSONArray =
    JSONArray().apply {
        listOf(
            PaymentMethod.CARTE_BANCAIRE,
            PaymentMethod.ESPECES,
            PaymentMethod.TITRE_RESTAURANT,
            PaymentMethod.AUTRE
        ).forEach { method ->
            put(
                JSONObject()
                    .put("code", method.closurePrintCode())
                    .put("label", method.label)
                    .put("count", paymentCounts[method] ?: 0)
                    .put("amountCentimes", paymentTotalsCentimes[method] ?: 0)
            )
        }
    }

private fun CashClosure.orderTypesJsonArray(): JSONArray =
    JSONArray().apply {
        OrderType.entries.forEach { orderType ->
            put(
                JSONObject()
                    .put("code", orderType.name)
                    .put("label", orderType.label)
                    .put("amountCentimes", orderTypeTotalsCentimes[orderType] ?: 0)
            )
        }
    }

private fun TicketPrinterOptions.toJsonObject(): JSONObject =
    JSONObject()
        .put("profile", profile)
        .put("paperWidth", paperWidth)
        .put("lineColumns", lineColumns)
        .put("cutPaper", cutPaper)
        .put("openCashDrawer", openCashDrawer)
        .put("codePage", codePage.name)
