package com.amhsolution.caisse.data.printing

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.amhsolution.caisse.domain.printing.ClosureReportPrintPayload
import com.amhsolution.caisse.domain.printing.TicketPrintPayload
import com.amhsolution.caisse.domain.printing.toEscPosBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.OutputStream
import java.lang.reflect.InvocationTargetException
import java.util.UUID

data class BluetoothPrinterTarget(
    val deviceName: String? = null,
    val deviceAddress: String? = null
)

sealed class PrintResult {
    object Success : PrintResult()
    data class Failure(val message: String, val cause: Throwable? = null) : PrintResult()
}

sealed class PrinterDiscoveryResult {
    data class Success(val printers: List<BluetoothPrinterTarget>) : PrinterDiscoveryResult()
    data class Failure(val message: String) : PrinterDiscoveryResult()
}

class BluetoothReceiptPrinter(
    context: Context,
    private val targetProvider: () -> BluetoothPrinterTarget = { BluetoothPrinterTarget() }
) {
    private val appContext = context.applicationContext

    suspend fun printCustomerTicket(payload: TicketPrintPayload): PrintResult =
        printBytes(payload.toEscPosBytes())

    suspend fun printKitchenTicket(payload: TicketPrintPayload): PrintResult =
        printBytes(payload.toEscPosBytes())

    suspend fun printClosureReport(payload: ClosureReportPrintPayload): PrintResult =
        printBytes(payload.toEscPosBytes())

    suspend fun openCashDrawer(): PrintResult =
        printBytes(com.amhsolution.caisse.domain.printing.EscPosCommandBuilder.cashDrawerPulse())

    @SuppressLint("MissingPermission")
    suspend fun discoverPairedPrinters(): PrinterDiscoveryResult = withContext(Dispatchers.IO) {
        if (!hasBluetoothPermissions()) {
            return@withContext PrinterDiscoveryResult.Failure(
                "Permission Bluetooth manquante."
            )
        }
        val adapter = bluetoothAdapter()
            ?: return@withContext PrinterDiscoveryResult.Failure(
                "Bluetooth indisponible sur cette tablette."
            )
        if (!adapter.isEnabled) {
            return@withContext PrinterDiscoveryResult.Failure(
                "Activez le Bluetooth pour afficher les imprimantes associées."
            )
        }

        PrinterDiscoveryResult.Success(
            adapter.bondedDevices.orEmpty()
                .map { device ->
                    BluetoothPrinterTarget(
                        deviceName = device.name ?: "Appareil Bluetooth",
                        deviceAddress = device.address
                    )
                }
                .sortedBy { it.deviceName.orEmpty().lowercase() }
        )
    }

    @SuppressLint("MissingPermission")
    suspend fun printBytes(bytes: ByteArray): PrintResult = withContext(Dispatchers.IO) {
        if (!hasBluetoothPermissions()) {
            return@withContext PrintResult.Failure(
                "Permissions Bluetooth manquantes. Autorisez l'application puis relancez l'impression."
            )
        }

        val adapter = bluetoothAdapter()
            ?: return@withContext PrintResult.Failure("Bluetooth indisponible sur cette tablette.")

        if (!adapter.isEnabled) {
            return@withContext PrintResult.Failure("Bluetooth desactive sur la tablette.")
        }

        val configuredTarget = targetProvider()
        val device = selectPrinter(adapter, configuredTarget)
            ?: return@withContext PrintResult.Failure(
                if (configuredTarget.isConfigured) {
                    "L'imprimante ${configuredTarget.displayName} est introuvable. Vérifiez le réglage et le Bluetooth."
                } else {
                    "Aucune imprimante Bluetooth associée. Associez l'imprimante dans Android avant d'imprimer."
                }
            )

        if (device.bondState != BluetoothDevice.BOND_BONDED) {
            return@withContext PrintResult.Failure(
                "L'imprimante ${device.name ?: device.address} n'est pas appariee. " +
                    "Ouvrez les parametres Bluetooth et validez l'association (PIN 1234)."
            )
        }

        runCatching {
            stopDiscoveryIfPossible(adapter)
            connectAndWrite(device, bytes)
        }.fold(
            onSuccess = { PrintResult.Success },
            onFailure = { error ->
                val detail = error.message?.takeIf { it.isNotBlank() }
                PrintResult.Failure(
                    buildString {
                        append("Connexion impossible avec ${device.name ?: device.address}.")
                        append(" Verifiez qu'elle est allumee, a moins de 1 m, et non utilisee par un autre appareil.")
                        if (detail != null) append("\nDetail : $detail")
                    },
                    error
                )
            }
        )
    }

    @SuppressLint("MissingPermission")
    private fun connectAndWrite(device: BluetoothDevice, bytes: ByteArray) {
        var lastError: IOException? = null

        for (strategy in connectionStrategies(device)) {
            var socket: BluetoothSocket? = null
            try {
                socket = strategy()
                socket.connect()
                Thread.sleep(PostConnectDelayMs)
                socket.outputStream.use { output -> writeInChunks(output, bytes) }
                return
            } catch (error: IOException) {
                lastError = error
            } catch (error: InvocationTargetException) {
                lastError = (error.cause as? IOException)
                    ?: IOException(error.cause?.message ?: "Connexion RFCOMM refusee.")
            } finally {
                runCatching { socket?.close() }
            }
        }

        throw lastError ?: IOException("Aucune methode de connexion Bluetooth n'a fonctionne.")
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

    @SuppressLint("MissingPermission")
    private fun connectionStrategies(device: BluetoothDevice): List<() -> BluetoothSocket> {
        val strategies = mutableListOf<() -> BluetoothSocket>()

        device.uuids?.mapNotNull { it?.uuid }?.distinct()?.forEach { uuid ->
            strategies += { device.createInsecureRfcommSocketToServiceRecord(uuid) }
            strategies += { device.createRfcommSocketToServiceRecord(uuid) }
        }
        strategies += { device.createInsecureRfcommSocketToServiceRecord(SerialPortProfileUuid) }
        strategies += { device.createRfcommSocketToServiceRecord(SerialPortProfileUuid) }

        for (channel in 1..5) {
            strategies += { createFallbackSocket(device, channel) }
            strategies += { createInsecureFallbackSocket(device, channel) }
        }
        return strategies
    }

    @SuppressLint("MissingPermission")
    private fun selectPrinter(
        adapter: BluetoothAdapter,
        target: BluetoothPrinterTarget
    ): BluetoothDevice? {
        val bondedDevices = adapter.bondedDevices.orEmpty()
        target.deviceAddress?.let { address ->
            return bondedDevices.firstOrNull { it.address.equals(address, ignoreCase = true) }
        }
        target.deviceName?.let { name ->
            return bondedDevices.firstOrNull { it.name.equals(name, ignoreCase = true) }
        }

        val printerKeywords = listOf(
            "bluetooth printer",
            "printer",
            "print",
            "pos",
            "receipt",
            "thermal",
            "80",
            "dmn"
        )
        return bondedDevices.firstOrNull { device ->
            val name = device.name.orEmpty().lowercase()
            printerKeywords.any { keyword -> keyword in name }
        }
    }

    private fun bluetoothAdapter(): BluetoothAdapter? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            appContext.getSystemService(BluetoothManager::class.java)?.adapter
        } else {
            @Suppress("DEPRECATION")
            BluetoothAdapter.getDefaultAdapter()
        }

    private fun hasBluetoothPermissions(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    private fun stopDiscoveryIfPossible(adapter: BluetoothAdapter) {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) return
        runCatching { adapter.cancelDiscovery() }
    }

    private companion object {
        const val ChunkSize = 1024
        const val ChunkDelayMs = 40L
        const val PostConnectDelayMs = 200L

        val SerialPortProfileUuid: UUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

        @SuppressLint("MissingPermission")
        private fun createFallbackSocket(device: BluetoothDevice, channel: Int): BluetoothSocket {
            val method = device.javaClass.getMethod("createRfcommSocket", Int::class.javaPrimitiveType)
            @Suppress("DEPRECATION")
            return method.invoke(device, channel) as BluetoothSocket
        }

        @SuppressLint("MissingPermission")
        private fun createInsecureFallbackSocket(
            device: BluetoothDevice,
            channel: Int
        ): BluetoothSocket {
            val method = device.javaClass.getMethod(
                "createInsecureRfcommSocket",
                Int::class.javaPrimitiveType
            )
            @Suppress("DEPRECATION")
            return method.invoke(device, channel) as BluetoothSocket
        }
    }
}

val BluetoothPrinterTarget.isConfigured: Boolean
    get() = !deviceAddress.isNullOrBlank() || !deviceName.isNullOrBlank()

val BluetoothPrinterTarget.displayName: String
    get() = deviceName?.takeIf { it.isNotBlank() }
        ?: deviceAddress?.takeIf { it.isNotBlank() }
        ?: "Sélection automatique"
