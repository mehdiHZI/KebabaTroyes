package com.amhsolution.caisse.data.printing

import com.amhsolution.caisse.domain.printing.MilanoGustoPrinterConfig

enum class PrinterLinkType(val label: String) {
    BLUETOOTH("Bluetooth"),
    WIFI("Wi-Fi")
}

/**
 * Cible d'impression configurée (Bluetooth appairé ou imprimante réseau TCP/9100).
 */
data class ConfiguredPrinter(
    val linkType: PrinterLinkType = PrinterLinkType.BLUETOOTH,
    val bluetooth: BluetoothPrinterTarget = BluetoothPrinterTarget(),
    val host: String? = null,
    val port: Int = DefaultNetworkPort
) {
    val isConfigured: Boolean
        get() = when (linkType) {
            PrinterLinkType.BLUETOOTH -> bluetooth.isConfigured
            PrinterLinkType.WIFI -> !normalizedHost.isNullOrBlank()
        }

    val displayName: String
        get() = when (linkType) {
            PrinterLinkType.BLUETOOTH -> bluetooth.displayName
            PrinterLinkType.WIFI -> {
                val hostPart = normalizedHost ?: "IP non définie"
                if (port == DefaultNetworkPort) "Wi-Fi · $hostPart" else "Wi-Fi · $hostPart:$port"
            }
        }

    val normalizedHost: String?
        get() = host?.trim()?.takeIf { it.isNotEmpty() }

    val effectivePort: Int
        get() = port.takeIf { it in 1..65535 } ?: DefaultNetworkPort

    companion object {
        const val DefaultNetworkPort = 9100

        val MilanoGustoDefaultBluetooth = ConfiguredPrinter(
            linkType = PrinterLinkType.BLUETOOTH,
            bluetooth = BluetoothPrinterTarget(
                deviceName = MilanoGustoPrinterConfig.BLUETOOTH_DEVICE_NAME,
                deviceAddress = MilanoGustoPrinterConfig.BLUETOOTH_DEVICE_ADDRESS
            )
        )
    }
}
