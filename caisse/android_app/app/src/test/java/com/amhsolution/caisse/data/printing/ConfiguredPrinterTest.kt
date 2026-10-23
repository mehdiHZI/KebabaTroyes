package com.amhsolution.caisse.data.printing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ConfiguredPrinterTest {
    @Test
    fun `wifi printer is configured when host is present`() {
        val printer = ConfiguredPrinter(
            linkType = PrinterLinkType.WIFI,
            host = " 192.168.1.50 ",
            port = 9100
        )

        assertTrue(printer.isConfigured)
        assertEquals("192.168.1.50", printer.normalizedHost)
        assertEquals("Wi-Fi · 192.168.1.50", printer.displayName)
    }

    @Test
    fun `wifi printer without host is not configured`() {
        val printer = ConfiguredPrinter(linkType = PrinterLinkType.WIFI, host = "  ")

        assertFalse(printer.isConfigured)
    }

    @Test
    fun `invalid port falls back to 9100`() {
        val printer = ConfiguredPrinter(
            linkType = PrinterLinkType.WIFI,
            host = "10.0.0.8",
            port = 0
        )

        assertEquals(9100, printer.effectivePort)
    }
}
