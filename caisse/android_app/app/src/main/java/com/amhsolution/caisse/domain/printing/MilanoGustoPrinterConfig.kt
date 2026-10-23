package com.amhsolution.caisse.domain.printing

/**
 * Profil de l'imprimante caisse Milano Gusto (POS-80, ESC/POS, 80 mm).
 * Autotest : EPSON ESC/POS, Bluetooth « Bluetooth Printer », firmware DMN-C.
 */
object MilanoGustoPrinterConfig {
    const val PROFILE_ID = "POS_80_DMNC_ESC_POS"

    /** Nom Bluetooth affiché sur l'autotest. */
    const val BLUETOOTH_DEVICE_NAME = "Bluetooth Printer"

    /** Adresse MAC de l'imprimante caisse (autotest). */
    const val BLUETOOTH_DEVICE_ADDRESS = "66:32:5E:C7:4B:85"

    /** 80 mm, police Font A 12×24 → 576 / 12 ≈ 48 colonnes. */
    const val LINE_COLUMNS_80MM = 48

    fun customerReceiptOptions(openCashDrawer: Boolean = false): TicketPrinterOptions =
        TicketPrinterOptions(
            profile = PROFILE_ID,
            paperWidth = "80mm",
            lineColumns = LINE_COLUMNS_80MM,
            cutPaper = true,
            openCashDrawer = openCashDrawer,
            codePage = PrinterCodePage.PC850
        )

    fun kitchenTicketOptions(): TicketPrinterOptions =
        customerReceiptOptions(openCashDrawer = false)

    fun closureReportOptions(): TicketPrinterOptions =
        customerReceiptOptions(openCashDrawer = false)
}

enum class PrinterCodePage(val escPosCode: Byte) {
    /** Page 0 — compatible autotest usine, accents limités. */
    PC437(0),

    /** Page 2 — recommandé pour le français (é, è, à, ç…). */
    PC850(2)
}
