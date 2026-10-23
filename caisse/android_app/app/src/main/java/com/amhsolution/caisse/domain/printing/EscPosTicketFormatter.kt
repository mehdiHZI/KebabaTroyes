package com.amhsolution.caisse.domain.printing

import com.amhsolution.caisse.domain.closure.CashClosureType
import com.amhsolution.caisse.domain.formatEurosAmount
import com.amhsolution.caisse.domain.model.OrderType
import com.amhsolution.caisse.domain.model.PaymentMethod
import java.io.ByteArrayOutputStream
import java.text.Normalizer
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val ticketDateFormatter =
    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.FRANCE)

fun TicketPrintPayload.toPrintableText(zoneId: ZoneId = ZoneId.systemDefault()): String {
    val configuredColumns = printer.lineColumns.coerceAtLeast(MinimumTicketColumns)
    // Les tickets client/cuisine passent par buildReadableTicket (GS ! 0x10 = double largeur) :
    // chaque caractère occupe 2 colonnes physiques → 48 / 2 = 24 colonnes utiles.
    val columns = (configuredColumns / ReadableTicketWidthFactor)
        .coerceAtLeast(MinimumTicketColumns)
        .let { usable ->
            if (documentType == TicketPrintDocumentType.KITCHEN_TICKET) {
                usable.coerceAtMost(KitchenTicketColumns)
            } else {
                usable
            }
        }
    val printedAt = Instant.ofEpochMilli(paidAtEpochMillis).atZone(zoneId)
    return buildString {
        appendCentered(restaurantName.uppercase(Locale.FRANCE), columns)
        appendDivider(columns)
        when (documentType) {
            // Titre + n° sur 2 lignes : en double largeur (24 col.), « TICKET CAISSE #n »
            // déborde et se coupe sur l'imprimante ; le centrage ligne par ligne reste lisible.
            TicketPrintDocumentType.CUSTOMER_RECEIPT -> {
                appendCentered("TICKET CAISSE", columns)
                appendCentered(ticketLabel, columns)
            }
            TicketPrintDocumentType.CUSTOMER_PROVISIONAL_RECEIPT -> {
                appendCentered("TICKET PROVISOIRE", columns)
                appendCentered(ticketLabel, columns)
            }
            TicketPrintDocumentType.KITCHEN_TICKET -> {
                appendCentered(kitchenTitle ?: "BON CUISINE", columns)
                appendCentered(ticketLabel, columns)
            }
            TicketPrintDocumentType.CLOSURE_REPORT -> appendCentered("RAPPORT DE CLOTURE", columns)
        }
        appendLine("Date : ${printedAt.format(ticketDateFormatter)}")
        appendLine("Type : ${orderType.label}")
        if (documentType == TicketPrintDocumentType.KITCHEN_TICKET) {
            appendKitchenDeliveryReference(deliveryInfo, columns)
        } else {
            appendDeliveryBlock(deliveryInfo, columns)
        }
        appendDivider(columns)
        lines.forEachIndexed { index, line ->
            if (index > 0) {
                if (documentType == TicketPrintDocumentType.KITCHEN_TICKET) {
                    appendDivider(columns)
                } else {
                    appendLine()
                }
            }
            if (documentType == TicketPrintDocumentType.KITCHEN_TICKET) {
                appendKitchenLine(line, columns)
            } else {
                appendCustomerLine(line, columns)
            }
        }
        appendDivider(columns)
        if (documentType == TicketPrintDocumentType.KITCHEN_TICKET) {
            appendCentered("A PREPARER", columns)
        } else {
            if (documentType == TicketPrintDocumentType.CUSTOMER_PROVISIONAL_RECEIPT) {
                appendAmountLine("A REGLER", totalCentimes, columns)
                appendLine()
                appendWrappedLine("Ticket provisoire - commande non encaissee", columns)
                appendWrappedLine("TVA et numero TVA imprimes au paiement final.", columns)
            } else {
                fiscalInfo?.let { fiscal ->
                    appendAmountLine("Total HT", fiscal.totalExcludingVatCentimes, columns)
                    appendAmountLine("TVA ${fiscal.vatRateLabel}", fiscal.vatCentimes, columns)
                    appendAmountLine("TOTAL TTC", totalCentimes, columns)
                    appendWrappedLine("No TVA : ${fiscal.vatNumber}", columns)
                } ?: appendAmountLine("TOTAL TTC", totalCentimes, columns)
                payments.filterNot { it.method == PaymentMethod.ANNULE }.forEach { payment ->
                    appendAmountLine(payment.method.label, payment.amountCentimes, columns)
                }
                appendLine()
                appendCentered("Merci et a bientot", columns)
            }
        }
    }
}

fun ClosureReportPrintPayload.toPrintableText(zoneId: ZoneId = ZoneId.systemDefault()): String {
    val columns = printer.lineColumns.coerceAtLeast(32)
    val closedAt = Instant.ofEpochMilli(closure.closedAtEpochMillis).atZone(zoneId)
    return buildString {
        appendCentered(restaurantName.uppercase(Locale.FRANCE), columns)
        appendCentered(cashRegisterName, columns)
        appendDivider(columns)
        appendCentered(closure.type.printShortLabel(), columns)
        if (closure.type == CashClosureType.DAILY_Z) {
            appendCentered(closure.statusLabel, columns)
        }
        appendAmountLine("Impression", 0, columns, rightText = closedAt.format(ticketDateFormatter))
        appendAmountLine("Periode", 0, columns, rightText = closure.businessDay)
        appendAmountLine("CAISSE", 0, columns, rightText = cashierName)
        appendDivider(columns)

        appendLine("1 - GENERAL")
        appendAmountLine("Nombre de tickets", 0, columns, rightText = closure.ticketCount.toString())
        appendAmountLine("Plage tickets", 0, columns, rightText = closure.ticketRangeLabel)
        appendAmountLine("Total des tickets", closure.totalRevenueCentimes, columns)
        appendAmountLine("Total encaisse", closure.totalRevenueCentimes, columns)
        appendAmountLine("Nombre d'articles", 0, columns, rightText = closure.itemCount.toString())
        appendAmountLine(
            "Commandes annulees",
            0,
            columns,
            rightText = closure.cancelledOrderCount.toString()
        )
        appendLine()

        appendLine("2 - ENCAISSEMENTS")
        listOf(
            PaymentMethod.CARTE_BANCAIRE,
            PaymentMethod.ESPECES,
            PaymentMethod.TITRE_RESTAURANT,
            PaymentMethod.AUTRE
        ).forEach { method ->
            val label = "${method.closurePrintCode()} ${method.label} [${closure.paymentCounts[method] ?: 0}]"
            appendAmountLine(label, closure.paymentTotalsCentimes[method] ?: 0, columns)
        }
        appendLine()

        appendLine("3 - CHIFFRE D'AFFAIRE")
        appendAmountLine("Total TTC", closure.totalRevenueCentimes, columns)
        appendAmountLine("TVA", 0, columns, rightText = "Non detaillee V1")
        appendLine()

        appendLine("4 - TYPES DE COMMANDE")
        OrderType.entries.forEach { orderType ->
            appendAmountLine(orderType.label, closure.orderTypeTotalsCentimes[orderType] ?: 0, columns)
        }
        appendLine()

        appendLine("5 - INFORMATIONS DE CLOTURE")
        appendAmountLine(
            "Date de cloture",
            0,
            columns,
            rightText = closedAt.format(ticketDateFormatter)
        )
    }
}

fun TicketPrintPayload.toEscPosBytes(zoneId: ZoneId = ZoneId.systemDefault()): ByteArray =
    EscPosCommandBuilder.buildReadableTicket(toPrintableText(zoneId), printer)

fun ClosureReportPrintPayload.toEscPosBytes(zoneId: ZoneId = ZoneId.systemDefault()): ByteArray =
    EscPosCommandBuilder.build(toPrintableText(zoneId), printer)

object EscPosCommandBuilder {
    private val initialize = byteArrayOf(0x1B, 0x40)
    /** Double largeur (x2) : plus lisible ; la mise en page doit alors utiliser 24 colonnes. */
    private val readableTicketTextSize = byteArrayOf(0x1D, 0x21, 0x10)
    private val normalTextSize = byteArrayOf(0x1D, 0x21, 0x00)
    /** Coupe partielle compatible POS-80 / DMN-C. */
    private val cutPaper = byteArrayOf(0x1D, 0x56, 0x42, 0x00)
    private val openCashDrawer = byteArrayOf(0x1B, 0x70, 0x00, 0x19, 0xFA.toByte())
    private const val FeedBeforeCut = "\n\n\n"

    fun buildReadableTicket(text: String, printer: TicketPrinterOptions): ByteArray =
        build(text, printer, readableTicketTextSize)

    fun build(text: String, printer: TicketPrinterOptions): ByteArray =
        build(text, printer, normalTextSize)

    private fun build(
        text: String,
        printer: TicketPrinterOptions,
        textSizeCommand: ByteArray
    ): ByteArray =
        ByteArrayOutputStream().use { output ->
            output.write(initialize)
            output.write(codePageSelect(printer.codePage))
            output.write(textSizeCommand)
            output.write(text.toPrinterBytes(printer.codePage))
            output.write(normalTextSize)
            output.write(FeedBeforeCut.toPrinterBytes(printer.codePage))
            if (printer.openCashDrawer) {
                output.write(openCashDrawer)
            }
            if (printer.cutPaper) {
                output.write(cutPaper)
            }
            output.toByteArray()
        }

    private fun codePageSelect(codePage: PrinterCodePage): ByteArray =
        byteArrayOf(0x1B, 0x74, codePage.escPosCode)

    fun cashDrawerPulse(): ByteArray =
        ByteArrayOutputStream().use { output ->
            output.write(initialize)
            output.write(openCashDrawer)
            output.toByteArray()
        }
}

private fun StringBuilder.appendCentered(text: String, columns: Int) {
    val value = text.trim()
    if (value.isEmpty()) {
        appendLine()
        return
    }
    if (value.length <= columns) {
        val leftPadding = ((columns - value.length) / 2).coerceAtLeast(0)
        appendLine(" ".repeat(leftPadding) + value)
        return
    }
    // Ne pas tronquer : découper en plusieurs lignes centrées.
    var remaining = value
    while (remaining.isNotEmpty()) {
        val chunk = remaining.take(columns)
        remaining = remaining.drop(chunk.length)
        val leftPadding = ((columns - chunk.length) / 2).coerceAtLeast(0)
        appendLine(" ".repeat(leftPadding) + chunk)
    }
}

private fun StringBuilder.appendDivider(columns: Int) {
    appendLine("-".repeat(columns))
}

private fun StringBuilder.appendDeliveryBlock(
    deliveryInfo: com.amhsolution.caisse.domain.model.DeliveryInfo?,
    columns: Int
) {
    deliveryInfo ?: return
    appendWrappedLine("Client : ${deliveryInfo.customerName}", columns)
    if (deliveryInfo.phoneNumber.isNotBlank()) {
        appendWrappedLine("Tel : ${deliveryInfo.phoneNumber}", columns)
    }
    appendWrappedLine("Adresse : ${deliveryInfo.address}", columns)
    appendLine("Livreur : ${deliveryInfo.driverLabel}")
}

private fun StringBuilder.appendKitchenDeliveryReference(
    deliveryInfo: com.amhsolution.caisse.domain.model.DeliveryInfo?,
    columns: Int
) {
    deliveryInfo ?: return
    appendWrappedLine("Client : ${deliveryInfo.customerName}", columns)
    appendWrappedLine("Livreur : ${deliveryInfo.driverLabel}", columns)
}

private fun StringBuilder.appendWrappedLine(text: String, columns: Int) {
    val indentation = text.takeWhile { it == ' ' }
    val words = text.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    if (words.isEmpty()) {
        appendLine()
        return
    }

    var currentLine = indentation
    words.forEach { word ->
        val separator = if (currentLine.length > indentation.length) " " else ""
        if (currentLine.length + separator.length + word.length <= columns) {
            currentLine += separator + word
        } else if (currentLine.length == indentation.length) {
            val availableColumns = (columns - indentation.length).coerceAtLeast(1)
            val chunks = word.chunked(availableColumns)
            chunks.dropLast(1).forEach { chunk -> appendLine(indentation + chunk) }
            currentLine = indentation + chunks.last()
        } else {
            appendLine(currentLine.trimEnd())
            val availableColumns = (columns - indentation.length).coerceAtLeast(1)
            val chunks = word.chunked(availableColumns)
            chunks.dropLast(1).forEach { chunk -> appendLine(indentation + chunk) }
            currentLine = indentation + chunks.last()
        }
    }
    appendLine(currentLine.trimEnd())
}

private fun StringBuilder.appendKitchenLine(line: TicketPrintLine, columns: Int) {
    appendWrappedLine("${line.quantity}x ${line.productName}", columns)
    appendLineDetails(line.kitchenDetails, columns)
}

private fun StringBuilder.appendCustomerLine(line: TicketPrintLine, columns: Int) {
    appendAmountLine(
        "${line.quantity}x ${line.productName}",
        line.lineTotalCentimes,
        columns
    )
    appendLineDetails(line.kitchenDetails, columns)
}

private fun StringBuilder.appendLineDetails(details: List<String>, columns: Int) {
    details.forEach { detail ->
        val compactLine = when {
            detail.startsWith("Supp. ") -> "  + ${detail.removePrefix("Supp. ")}"
            detail.startsWith("Sans ") -> "  SANS ${detail.removePrefix("Sans ")}"
            detail.equals("Menu", ignoreCase = true) -> "  MENU"
            detail.equals("MENU ETUDIANT", ignoreCase = true) -> "  MENU ETUDIANT"
            else -> "  - $detail"
        }
        appendWrappedLine(compactLine, columns)
    }
}

private fun StringBuilder.appendAmountLine(
    label: String,
    centimes: Int,
    columns: Int,
    rightText: String = moneyForTicket(centimes)
) {
    val right = rightText.take(columns)
    val maxLabelLength = (columns - right.length - 1).coerceAtLeast(8)
    val left = label.take(maxLabelLength)
    val spacing = (columns - left.length - right.length).coerceAtLeast(1)
    appendLine(left + " ".repeat(spacing) + right)
}

private fun moneyForTicket(centimes: Int): String =
    "${formatEurosAmount(centimes)} EUR"

private fun String.toPrinterBytes(codePage: PrinterCodePage): ByteArray =
    toPrinterSafeText(codePage).toByteArray(printerCharset(codePage))

private fun printerCharset(codePage: PrinterCodePage): java.nio.charset.Charset =
    when (codePage) {
        PrinterCodePage.PC850 -> java.nio.charset.Charset.forName("IBM850")
        PrinterCodePage.PC437 -> Charsets.ISO_8859_1
    }

private fun String.toPrinterSafeText(codePage: PrinterCodePage): String {
    val replaced = replace("€", " EUR")
        .replace("œ", "oe")
        .replace("Œ", "OE")
        .replace("’", "'")
        .replace("`", "'")
        .replace("–", "-")
        .replace("—", "-")
        .replace("…", "...")
    return when (codePage) {
        PrinterCodePage.PC850 -> replaced
        PrinterCodePage.PC437 -> Normalizer.normalize(replaced, Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
    }
}

private const val MinimumTicketColumns = 24
private const val KitchenTicketColumns = 24
/** Facteur de largeur pour GS ! 0x10 (double largeur) utilisé dans buildReadableTicket. */
private const val ReadableTicketWidthFactor = 2
