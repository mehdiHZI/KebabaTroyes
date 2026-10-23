package com.amhsolution.caisse.data.printing

import android.content.Context
import com.amhsolution.caisse.domain.printing.ClosureReportPrintPayload
import com.amhsolution.caisse.domain.printing.TicketPrintPayload

/**
 * Route l'impression vers Bluetooth ou Wi-Fi selon la configuration enregistrée.
 */
class ReceiptPrinter(
    context: Context,
    private val endpointProvider: () -> ConfiguredPrinter
) {
    private val bluetoothPrinter = BluetoothReceiptPrinter(context) {
        endpointProvider().bluetooth
    }
    private val networkPrinter = NetworkReceiptPrinter(endpointProvider)

    suspend fun printCustomerTicket(payload: TicketPrintPayload): PrintResult =
        delegate().printCustomerTicket(payload)

    suspend fun printKitchenTicket(payload: TicketPrintPayload): PrintResult =
        delegate().printKitchenTicket(payload)

    suspend fun printClosureReport(payload: ClosureReportPrintPayload): PrintResult =
        delegate().printClosureReport(payload)

    suspend fun openCashDrawer(): PrintResult =
        delegate().openCashDrawer()

    suspend fun printBytes(bytes: ByteArray): PrintResult =
        when (endpointProvider().linkType) {
            PrinterLinkType.BLUETOOTH -> bluetoothPrinter.printBytes(bytes)
            PrinterLinkType.WIFI -> networkPrinter.printBytes(bytes)
        }

    suspend fun discoverPairedPrinters(): PrinterDiscoveryResult =
        bluetoothPrinter.discoverPairedPrinters()

    fun currentEndpoint(): ConfiguredPrinter = endpointProvider()

    private fun delegate(): PrinterBackend =
        when (endpointProvider().linkType) {
            PrinterLinkType.BLUETOOTH -> BluetoothBackend(bluetoothPrinter)
            PrinterLinkType.WIFI -> NetworkBackend(networkPrinter)
        }

    private interface PrinterBackend {
        suspend fun printCustomerTicket(payload: TicketPrintPayload): PrintResult
        suspend fun printKitchenTicket(payload: TicketPrintPayload): PrintResult
        suspend fun printClosureReport(payload: ClosureReportPrintPayload): PrintResult
        suspend fun openCashDrawer(): PrintResult
    }

    private class BluetoothBackend(
        private val printer: BluetoothReceiptPrinter
    ) : PrinterBackend {
        override suspend fun printCustomerTicket(payload: TicketPrintPayload) =
            printer.printCustomerTicket(payload)

        override suspend fun printKitchenTicket(payload: TicketPrintPayload) =
            printer.printKitchenTicket(payload)

        override suspend fun printClosureReport(payload: ClosureReportPrintPayload) =
            printer.printClosureReport(payload)

        override suspend fun openCashDrawer() =
            printer.openCashDrawer()
    }

    private class NetworkBackend(
        private val printer: NetworkReceiptPrinter
    ) : PrinterBackend {
        override suspend fun printCustomerTicket(payload: TicketPrintPayload) =
            printer.printCustomerTicket(payload)

        override suspend fun printKitchenTicket(payload: TicketPrintPayload) =
            printer.printKitchenTicket(payload)

        override suspend fun printClosureReport(payload: ClosureReportPrintPayload) =
            printer.printClosureReport(payload)

        override suspend fun openCashDrawer() =
            printer.openCashDrawer()
    }
}
