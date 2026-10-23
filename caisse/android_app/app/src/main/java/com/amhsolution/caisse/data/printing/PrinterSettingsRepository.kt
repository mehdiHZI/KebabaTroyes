package com.amhsolution.caisse.data.printing

import android.content.Context

enum class TicketOutputTarget(
    val label: String,
    val includesCustomer: Boolean,
    val includesKitchen: Boolean
) {
    CUSTOMER("Caisse", includesCustomer = true, includesKitchen = false),
    KITCHEN("Cuisine", includesCustomer = false, includesKitchen = true),
    BOTH("Les deux", includesCustomer = true, includesKitchen = true)
}

data class PrinterRoutingSettings(
    val customerPrinter: ConfiguredPrinter = ConfiguredPrinter.MilanoGustoDefaultBluetooth,
    val kitchenPrinter: ConfiguredPrinter = ConfiguredPrinter.MilanoGustoDefaultBluetooth,
    val automaticOutput: TicketOutputTarget = TicketOutputTarget.BOTH
)

class PrinterSettingsRepository(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        PreferencesName,
        Context.MODE_PRIVATE
    )

    fun load(): PrinterRoutingSettings =
        PrinterRoutingSettings(
            customerPrinter = loadConfiguredPrinter(
                linkTypeKey = CustomerLinkTypeKey,
                nameKey = CustomerPrinterNameKey,
                addressKey = CustomerPrinterAddressKey,
                hostKey = CustomerHostKey,
                portKey = CustomerPortKey
            ),
            kitchenPrinter = loadConfiguredPrinter(
                linkTypeKey = KitchenLinkTypeKey,
                nameKey = KitchenPrinterNameKey,
                addressKey = KitchenPrinterAddressKey,
                hostKey = KitchenHostKey,
                portKey = KitchenPortKey
            ),
            automaticOutput = preferences.getString(AutomaticOutputKey, null)
                ?.let { stored ->
                    TicketOutputTarget.entries.firstOrNull { it.name == stored }
                }
                ?: TicketOutputTarget.BOTH
        )

    private fun loadConfiguredPrinter(
        linkTypeKey: String,
        nameKey: String,
        addressKey: String,
        hostKey: String,
        portKey: String
    ): ConfiguredPrinter {
        val linkType = preferences.getString(linkTypeKey, null)
            ?.let { stored -> PrinterLinkType.entries.firstOrNull { it.name == stored } }
            ?: PrinterLinkType.BLUETOOTH

        val bluetooth = if (!preferences.contains(nameKey) && !preferences.contains(addressKey)) {
            ConfiguredPrinter.MilanoGustoDefaultBluetooth.bluetooth
        } else {
            BluetoothPrinterTarget(
                deviceName = preferences.getString(nameKey, null),
                deviceAddress = preferences.getString(addressKey, null)
            )
        }

        return ConfiguredPrinter(
            linkType = linkType,
            bluetooth = bluetooth,
            host = preferences.getString(hostKey, null),
            port = preferences.getInt(portKey, ConfiguredPrinter.DefaultNetworkPort)
        )
    }

    fun save(settings: PrinterRoutingSettings) {
        preferences.edit()
            .putString(CustomerLinkTypeKey, settings.customerPrinter.linkType.name)
            .putNullableString(CustomerPrinterNameKey, settings.customerPrinter.bluetooth.deviceName)
            .putNullableString(CustomerPrinterAddressKey, settings.customerPrinter.bluetooth.deviceAddress)
            .putNullableString(CustomerHostKey, settings.customerPrinter.host)
            .putInt(CustomerPortKey, settings.customerPrinter.effectivePort)
            .putString(KitchenLinkTypeKey, settings.kitchenPrinter.linkType.name)
            .putNullableString(KitchenPrinterNameKey, settings.kitchenPrinter.bluetooth.deviceName)
            .putNullableString(KitchenPrinterAddressKey, settings.kitchenPrinter.bluetooth.deviceAddress)
            .putNullableString(KitchenHostKey, settings.kitchenPrinter.host)
            .putInt(KitchenPortKey, settings.kitchenPrinter.effectivePort)
            .putString(AutomaticOutputKey, settings.automaticOutput.name)
            .apply()
    }

    private fun android.content.SharedPreferences.Editor.putNullableString(
        key: String,
        value: String?
    ): android.content.SharedPreferences.Editor =
        if (value.isNullOrBlank()) remove(key) else putString(key, value)

    private companion object {
        const val PreferencesName = "printer_routing"
        const val CustomerLinkTypeKey = "customer_link_type"
        const val CustomerPrinterNameKey = "customer_printer_name"
        const val CustomerPrinterAddressKey = "customer_printer_address"
        const val CustomerHostKey = "customer_host"
        const val CustomerPortKey = "customer_port"
        const val KitchenLinkTypeKey = "kitchen_link_type"
        const val KitchenPrinterNameKey = "kitchen_printer_name"
        const val KitchenPrinterAddressKey = "kitchen_printer_address"
        const val KitchenHostKey = "kitchen_host"
        const val KitchenPortKey = "kitchen_port"
        const val AutomaticOutputKey = "automatic_ticket_output"
    }
}
