package com.amhsolution.caisse.data.printing

import com.amhsolution.caisse.domain.printing.ClosureReportPrintPayload
import com.amhsolution.caisse.domain.printing.TicketPrintPayload
import com.amhsolution.caisse.domain.printing.toEscPosBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket

/**
 * Impression ESC/POS via Wi-Fi / Ethernet (socket TCP, port raw 9100 par défaut).
 */
class NetworkReceiptPrinter(
    private val endpointProvider: () -> ConfiguredPrinter
) {
    suspend fun printCustomerTicket(payload: TicketPrintPayload): PrintResult =
        printBytes(payload.toEscPosBytes())

    suspend fun printKitchenTicket(payload: TicketPrintPayload): PrintResult =
        printBytes(payload.toEscPosBytes())

    suspend fun printClosureReport(payload: ClosureReportPrintPayload): PrintResult =
        printBytes(payload.toEscPosBytes())

    suspend fun openCashDrawer(): PrintResult =
        printBytes(com.amhsolution.caisse.domain.printing.EscPosCommandBuilder.cashDrawerPulse())

    suspend fun printBytes(bytes: ByteArray): PrintResult = withContext(Dispatchers.IO) {
        val endpoint = endpointProvider()
        val host = endpoint.normalizedHost
            ?: return@withContext PrintResult.Failure(
                "Adresse IP Wi-Fi manquante. Renseignez l'IP de l'imprimante dans Imprimantes."
            )
        val port = endpoint.effectivePort

        runCatching {
            Socket().use { socket ->
                socket.tcpNoDelay = true
                socket.connect(InetSocketAddress(host, port), ConnectTimeoutMs)
                socket.soTimeout = WriteTimeoutMs
                socket.getOutputStream().use { output ->
                    writeInChunks(output, bytes)
                }
            }
        }.fold(
            onSuccess = { PrintResult.Success },
            onFailure = { error ->
                val detail = error.message?.takeIf { it.isNotBlank() }
                PrintResult.Failure(
                    buildString {
                        append("Connexion Wi-Fi impossible vers $host:$port.")
                        append(" Verifiez que l'imprimante est allumee, sur le meme reseau Wi-Fi,")
                        append(" et que le port raw (souvent 9100) est ouvert.")
                        if (detail != null) append("\nDetail : $detail")
                    },
                    error
                )
            }
        )
    }

    private fun writeInChunks(output: OutputStream, bytes: ByteArray) {
        var offset = 0
        while (offset < bytes.size) {
            val length = minOf(ChunkSize, bytes.size - offset)
            output.write(bytes, offset, length)
            output.flush()
            offset += length
            if (offset < bytes.size) Thread.sleep(ChunkDelayMs)
        }
    }

    private companion object {
        const val ChunkSize = 1024
        const val ChunkDelayMs = 20L
        const val ConnectTimeoutMs = 5_000
        const val WriteTimeoutMs = 10_000
    }
}
