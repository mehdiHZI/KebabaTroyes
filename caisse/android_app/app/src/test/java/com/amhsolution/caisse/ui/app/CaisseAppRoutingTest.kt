package com.amhsolution.caisse.ui.app

import com.amhsolution.caisse.data.printing.TicketOutputTarget
import org.junit.Assert.assertEquals
import org.junit.Test

class CaisseAppRoutingTest {
    @Test
    fun `une nouvelle commande conserve le routage automatique configure`() {
        assertEquals(
            TicketOutputTarget.BOTH,
            paidOutputTarget(
                configuredTarget = TicketOutputTarget.BOTH,
                settlesPendingOrder = false
            )
        )
    }

    @Test
    fun `l encaissement d une attente ne reimprime jamais en cuisine`() {
        TicketOutputTarget.entries.forEach { configuredTarget ->
            assertEquals(
                TicketOutputTarget.CUSTOMER,
                paidOutputTarget(
                    configuredTarget = configuredTarget,
                    settlesPendingOrder = true
                )
            )
        }
    }

    @Test
    fun `la creation d une attente conserve le routage automatique configure`() {
        TicketOutputTarget.entries.forEach { configuredTarget ->
            assertEquals(
                configuredTarget,
                pendingOutputTarget(
                    configuredTarget = configuredTarget,
                    updatesPendingOrder = false
                )
            )
        }
    }

    @Test
    fun `la mise a jour d une attente ne reimprime pas le bon cuisine complet`() {
        assertEquals(
            TicketOutputTarget.CUSTOMER,
            pendingOutputTarget(TicketOutputTarget.BOTH, updatesPendingOrder = true)
        )
        assertEquals(
            TicketOutputTarget.CUSTOMER,
            pendingOutputTarget(TicketOutputTarget.CUSTOMER, updatesPendingOrder = true)
        )
        assertEquals(
            null,
            pendingOutputTarget(TicketOutputTarget.KITCHEN, updatesPendingOrder = true)
        )
    }
}
