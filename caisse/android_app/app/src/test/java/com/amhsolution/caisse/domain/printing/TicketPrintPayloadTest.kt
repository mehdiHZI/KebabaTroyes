package com.amhsolution.caisse.domain.printing

import com.amhsolution.caisse.domain.catalog.PizzaBase
import com.amhsolution.caisse.domain.catalog.PizzaSize
import com.amhsolution.caisse.domain.model.DeliveryInfo
import com.amhsolution.caisse.domain.model.HistoryRecord
import com.amhsolution.caisse.domain.model.PizzaCustomization
import com.amhsolution.caisse.domain.model.OrderLineSnapshot
import com.amhsolution.caisse.domain.model.OrderTicket
import com.amhsolution.caisse.domain.model.OrderType
import com.amhsolution.caisse.domain.model.PaymentMethod
import com.amhsolution.caisse.domain.model.PaymentRecord
import com.amhsolution.caisse.domain.model.StudentMenuChoiceKind
import com.amhsolution.caisse.domain.model.StudentMenuCustomization
import com.amhsolution.caisse.domain.model.SupplementSelection
import com.amhsolution.caisse.domain.closure.CashClosure
import com.amhsolution.caisse.domain.closure.CashClosureType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TicketPrintPayloadTest {
    @Test
    fun `prepare un ticket client depuis un historique encaisse`() {
        val record = HistoryRecord(
            id = "history-1",
            ticketNumber = 42,
            businessDay = "2026-06-23",
            paidAtEpochMillis = 1_782_200_000_000,
            orderType = OrderType.A_EMPORTER,
            lines = listOf(
                OrderLineSnapshot(
                    productName = "Margherita",
                    quantity = 2,
                    totalCentimes = 1600
                )
            ),
            totalCentimes = 1600,
            payments = listOf(PaymentRecord(PaymentMethod.CARTE_BANCAIRE, 1600))
        )

        val payload = record.toCustomerTicketPrintPayload(restaurantName = "Kebaba")

        assertEquals(TicketPrintDocumentType.CUSTOMER_RECEIPT, payload.documentType)
        assertEquals("Kebaba", payload.restaurantName)
        assertEquals(42, payload.ticketNumber)
        assertEquals(OrderType.A_EMPORTER, payload.orderType)
        assertEquals(1, payload.lines.size)
        assertEquals("2x Margherita", payload.lines.first().displayLabel)
        assertEquals(800, payload.lines.first().unitTotalCentimes)
        assertEquals(1600, payload.totalCentimes)
        assertEquals("80mm", payload.printer.paperWidth)
        assertEquals(48, payload.printer.lineColumns)
        assertEquals(MilanoGustoPrinterConfig.PROFILE_ID, payload.printer.profile)
        assertEquals(PrinterCodePage.PC850, payload.printer.codePage)
        assertEquals("FR37106121080", payload.fiscalInfo?.vatNumber)
        assertEquals(1000, payload.fiscalInfo?.vatRateBasisPoints)
    }

    @Test
    fun `prepare un bon cuisine depuis une commande en attente`() {
        val order = OrderTicket(
            id = "pending-1",
            ticketNumber = 7,
            orderType = OrderType.LIVRAISON,
            lines = listOf(
                OrderLineSnapshot(
                    productName = "Frutti del mare",
                    quantity = 1,
                    totalCentimes = 1000,
                    productId = "pizza-frutti-del-mare",
                    pizzaCustomization = PizzaCustomization(
                        size = PizzaSize.JUNIOR,
                        base = PizzaBase.TOMATE
                    )
                )
            ),
            totalCentimes = 1050,
            createdAtEpochMillis = 1_782_200_000_000,
            statusLabel = "Livraison"
        )

        val payload = order.toKitchenTicketPrintPayload(printedAtEpochMillis = 1_782_200_060_000)

        assertEquals(TicketPrintDocumentType.KITCHEN_TICKET, payload.documentType)
        assertEquals("#7", payload.ticketLabel)
        assertEquals(0, payload.payments.size)
        assertTrue(payload.toPrintableText().contains("BON CUISINE"))
        assertTrue(payload.toPrintableText().contains("#7"))
        assertEquals(
            listOf("Taille : 29cm", "Base tomate", "Mozzarella", "Fruits de mer"),
            payload.lines.first().kitchenDetails
        )
        assertTrue(payload.toPrintableText().contains("1x Frutti del mare"))
        assertTrue(payload.toPrintableText().contains("  - Mozzarella"))

        val additionText = order.toKitchenTicketPrintPayload(
            printedAtEpochMillis = 1_782_200_060_000,
            addition = true
        ).toPrintableText()
        assertTrue(additionText.contains("AJOUT CUISINE"))
        assertTrue(!additionText.contains("BON CUISINE"))
    }

    @Test
    fun `genere un texte imprimable pour un ticket client`() {
        val record = HistoryRecord(
            id = "history-2",
            ticketNumber = 43,
            businessDay = "2026-06-23",
            paidAtEpochMillis = 1_782_200_000_000,
            orderType = OrderType.SUR_PLACE,
            lines = listOf(OrderLineSnapshot(productName = "Margherita", quantity = 1, totalCentimes = 800)),
            totalCentimes = 800,
            payments = listOf(PaymentRecord(PaymentMethod.ESPECES, 800))
        )

        val text = record.toCustomerTicketPrintPayload().toPrintableText()
        val bytes = record.toCustomerTicketPrintPayload().toEscPosBytes()

        assertTrue(text.contains("TICKET CAISSE"))
        assertTrue(text.contains("#43"))
        assertTrue(text.contains("1x Margherita"))
        assertTrue(text.contains("TOTAL TTC"))
        assertTrue(text.contains("TVA 10,00 %"))
        assertTrue(text.contains("No TVA"))
        assertTrue(bytes.isNotEmpty())
        assertEquals(0x1B.toByte(), bytes[0])
        assertEquals(0x40.toByte(), bytes[1])
        assertEquals(0x1B.toByte(), bytes[2])
        assertEquals(0x74.toByte(), bytes[3])
        assertEquals(PrinterCodePage.PC850.escPosCode, bytes[4])
        assertEquals(0x1D.toByte(), bytes[5])
        assertEquals(0x21.toByte(), bytes[6])
        assertEquals(0x10.toByte(), bytes[7])

        val lines = text.lines()
        assertEquals("      MILANO GUSTO", lines.first())
        assertTrue(lines.any { it == "     TICKET CAISSE" })
        assertTrue(lines.any { it == "          #43" })
        assertTrue(lines.any { it == "   Merci et a bientot" })
    }

    @Test
    fun `le ticket caisse reprend les details cuisine sous chaque article`() {
        val record = HistoryRecord(
            id = "history-details",
            ticketNumber = 53,
            businessDay = "2026-09-16",
            paidAtEpochMillis = 1_782_200_000_000,
            orderType = OrderType.SUR_PLACE,
            lines = listOf(
                OrderLineSnapshot(
                    productName = "Frutti del mare",
                    quantity = 1,
                    totalCentimes = 1050,
                    productId = "pizza-frutti-del-mare",
                    pizzaCustomization = PizzaCustomization(
                        size = PizzaSize.JUNIOR,
                        base = PizzaBase.TOMATE
                    )
                )
            ),
            totalCentimes = 1050,
            payments = listOf(PaymentRecord(PaymentMethod.CARTE_BANCAIRE, 1050))
        )

        val text = record.toCustomerTicketPrintPayload().toPrintableText()

        assertTrue(text.contains("1x Frutti del mare"))
        assertTrue(text.contains("  - Taille : 29cm"))
        assertTrue(text.contains("  - Base tomate"))
        assertTrue(text.contains("  - Mozzarella"))
        assertTrue(text.contains("TOTAL TTC"))
    }

    @Test
    fun `remplace oeuf par un libelle compatible avec l imprimante`() {
        val order = OrderTicket(
            id = "pending-oeuf",
            ticketNumber = 44,
            orderType = OrderType.SUR_PLACE,
            lines = listOf(
                OrderLineSnapshot(
                    productName = "Boursino",
                    quantity = 1,
                    totalCentimes = 1100,
                    productId = "pizza-boursina",
                    pizzaCustomization = PizzaCustomization(
                        size = PizzaSize.JUNIOR,
                        base = PizzaBase.CREME
                    )
                )
            ),
            totalCentimes = 1100,
            createdAtEpochMillis = 1_782_200_000_000,
            statusLabel = "A regler"
        )

        val printedBytes = order.toKitchenTicketPrintPayload()
            .toEscPosBytes()
            .toString(Charsets.ISO_8859_1)

        assertTrue(printedBytes.contains("  - Oeuf"))
        assertTrue(!printedBytes.contains("  - uf"))
    }

    @Test
    fun `garde pommes de terre sur une ligne cuisine compacte`() {
        val order = OrderTicket(
            id = "pending-long-label",
            ticketNumber = 47,
            orderType = OrderType.SUR_PLACE,
            lines = listOf(
                OrderLineSnapshot(
                    productName = "Tonno",
                    quantity = 1,
                    totalCentimes = 1200,
                    productId = "pizza-tonno",
                    pizzaCustomization = PizzaCustomization(
                        size = PizzaSize.JUNIOR,
                        base = PizzaBase.TOMATE,
                        addedSupplements = listOf(
                            SupplementSelection("pommes-de-terre", "Pommes de terre", 100)
                        )
                    )
                )
            ),
            totalCentimes = 1200,
            createdAtEpochMillis = 1_782_200_000_000,
            statusLabel = "A regler"
        )

        val lines = order.toKitchenTicketPrintPayload().toPrintableText().lines()

        assertTrue(lines.contains("  + Pommes de terre"))
        assertTrue(lines.filter { it.isNotEmpty() }.all { it.length <= 24 })
    }

    @Test
    fun `limite les donnees livraison imprimees en cuisine`() {
        val deliveryInfo = DeliveryInfo(
            customerName = "Camille",
            address = "22 rue Georges Tremet",
            driverNumber = 3,
            phoneNumber = "0612893838"
        )
        val order = OrderTicket(
            id = "pending-delivery",
            ticketNumber = 48,
            orderType = OrderType.LIVRAISON,
            lines = listOf(
                OrderLineSnapshot(productName = "Tonno", quantity = 1, totalCentimes = 1100)
            ),
            totalCentimes = 1100,
            createdAtEpochMillis = 1_782_200_000_000,
            statusLabel = "Livraison",
            deliveryInfo = deliveryInfo
        )

        val customerText = order.toProvisionalCustomerTicketPrintPayload().toPrintableText()
        val kitchenPayload = order.toKitchenTicketPrintPayload()
        val kitchenText = kitchenPayload.toPrintableText()

        assertTrue(customerText.contains("Camille"))
        assertTrue(customerText.contains("0612893838"))
        assertTrue(customerText.contains("22 rue Georges Tremet"))
        assertTrue(kitchenText.contains("Client : Camille"))
        assertTrue(kitchenText.contains("Livreur : Livreur 3"))
        assertTrue(!kitchenText.contains("0612893838"))
        assertTrue(!kitchenText.contains("22 rue Georges Tremet"))
        assertEquals("", kitchenPayload.deliveryInfo?.phoneNumber)
        assertEquals("", kitchenPayload.deliveryInfo?.address)
    }

    @Test
    fun `le bon cuisine ne contient ni prix ni paiement ni tva`() {
        val record = HistoryRecord(
            id = "history-kitchen",
            ticketNumber = 45,
            businessDay = "2026-07-17",
            paidAtEpochMillis = 1_782_200_000_000,
            orderType = OrderType.SUR_PLACE,
            lines = listOf(
                OrderLineSnapshot(productName = "Margherita", quantity = 1, totalCentimes = 800)
            ),
            totalCentimes = 800,
            payments = listOf(PaymentRecord(PaymentMethod.ESPECES, 800))
        )

        val customerText = record.toCustomerTicketPrintPayload().toPrintableText()
        val kitchenText = record.toKitchenTicketPrintPayload().toPrintableText()

        assertTrue(customerText.contains("TICKET CAISSE"))
        assertTrue(customerText.contains("TOTAL TTC"))
        assertTrue(kitchenText.contains("BON CUISINE"))
        assertTrue(!kitchenText.contains("TOTAL TTC"))
        assertTrue(!kitchenText.contains("No TVA"))
        assertTrue(!kitchenText.contains("Espèces"))
    }

    @Test
    fun `ajoute l impulsion rj12 uniquement quand elle est demandee`() {
        val record = HistoryRecord(
            id = "history-drawer",
            ticketNumber = 46,
            businessDay = "2026-07-17",
            paidAtEpochMillis = 1_782_200_000_000,
            orderType = OrderType.SUR_PLACE,
            lines = listOf(
                OrderLineSnapshot(productName = "Margherita", quantity = 1, totalCentimes = 800)
            ),
            totalCentimes = 800,
            payments = listOf(PaymentRecord(PaymentMethod.ESPECES, 800))
        )
        val drawerPulse = byteArrayOf(0x1B, 0x70, 0x00, 0x19, 0xFA.toByte())

        val regularBytes = record.toCustomerTicketPrintPayload().toEscPosBytes()
        val cashBytes = record.toCustomerTicketPrintPayload(
            printer = TicketPrinterOptions(openCashDrawer = true)
        ).toEscPosBytes()

        assertTrue(!regularBytes.containsSequence(drawerPulse))
        assertTrue(cashBytes.containsSequence(drawerPulse))
    }

    @Test
    fun `genere un ticket provisoire sans tva depuis une commande en attente`() {
        val order = OrderTicket(
            id = "pending-2",
            ticketNumber = 8,
            orderType = OrderType.SUR_PLACE,
            lines = listOf(OrderLineSnapshot(productName = "Nordico", quantity = 1, totalCentimes = 1050)),
            totalCentimes = 1050,
            createdAtEpochMillis = 1_782_200_000_000,
            statusLabel = "A regler"
        )

        val payload = order.toProvisionalCustomerTicketPrintPayload(
            printedAtEpochMillis = 1_782_200_060_000
        )
        val text = payload.toPrintableText()

        assertEquals(TicketPrintDocumentType.CUSTOMER_PROVISIONAL_RECEIPT, payload.documentType)
        assertEquals(null, payload.fiscalInfo)
        assertTrue(text.contains("TICKET PROVISOIRE"))
        assertTrue(text.contains("#8"))
        assertTrue(text.contains("A REGLER"))
        assertTrue(!text.contains("No TVA"))
        assertTrue(!text.contains("TVA 10,00 %"))
    }

    @Test
    fun `prepare un ticket z imprimable depuis une cloture`() {
        val closure = CashClosure(
            id = "daily-z-2026-06-24",
            type = CashClosureType.DAILY_Z,
            businessDay = "2026-06-24",
            closedAtEpochMillis = 1_782_200_000_000,
            ticketCount = 2,
            firstTicketNumber = 1,
            lastTicketNumber = 2,
            totalRevenueCentimes = 2000,
            itemCount = 2,
            averageBasketCentimes = 1000,
            paymentTotalsCentimes = mapOf(
                PaymentMethod.CARTE_BANCAIRE to 1000,
                PaymentMethod.ESPECES to 1000
            ),
            paymentCounts = mapOf(
                PaymentMethod.CARTE_BANCAIRE to 1,
                PaymentMethod.ESPECES to 1,
                PaymentMethod.ANNULE to 3
            ),
            orderTypeTotalsCentimes = mapOf(OrderType.SUR_PLACE to 2000)
        )

        val payload = closure.toClosureReportPrintPayload()
        val text = payload.toPrintableText()
        val bytes = payload.toEscPosBytes()

        assertEquals(TicketPrintDocumentType.CLOSURE_REPORT, payload.documentType)
        assertTrue(text.contains("TICKET Z JOURNALIER"))
        assertTrue(text.contains("Cloture manuelle (provisoire)"))
        assertTrue(text.contains("1 - GENERAL"))
        assertTrue(text.contains("2 - ENCAISSEMENTS"))
        assertTrue(text.contains("Commandes annulees"))
        assertTrue(text.contains("5 - INFORMATIONS DE CLOTURE"))
        assertTrue(text.contains("Date de cloture"))
        assertTrue(!text.contains("Panier moyen"))
        assertTrue(!text.contains("Fond de caisse"))
        assertTrue(!text.contains("Ecart de caisse"))
        assertEquals(0x1D.toByte(), bytes[5])
        assertEquals(0x21.toByte(), bytes[6])
        assertEquals(0x00.toByte(), bytes[7])
    }

    @Test
    fun `affiche smoothie et gout separes sur le bon cuisine`() {
        val order = OrderTicket(
            id = "pending-smoothie",
            ticketNumber = 51,
            orderType = OrderType.A_EMPORTER,
            lines = listOf(
                OrderLineSnapshot(
                    productName = "Smoothie Fraise-Banane",
                    quantity = 1,
                    totalCentimes = 650,
                    productId = "smoothie-fraise-banane"
                )
            ),
            totalCentimes = 650,
            createdAtEpochMillis = 1_782_200_000_000,
            statusLabel = "A regler"
        )

        val payload = order.toKitchenTicketPrintPayload()
        val text = payload.toPrintableText()

        assertEquals("Smoothie", payload.lines.single().productName)
        assertEquals(listOf("Fraise-Banane"), payload.lines.single().kitchenDetails)
        assertTrue(text.contains("1x Smoothie"))
        assertTrue(text.contains("  - Fraise-Banane"))
    }

    @Test
    fun `affiche la composition du classico focaccia dans le menu etudiant`() {
        val order = OrderTicket(
            id = "pending-student-classico",
            ticketNumber = 52,
            orderType = OrderType.SUR_PLACE,
            lines = listOf(
                OrderLineSnapshot(
                    productName = "Menu étudiant",
                    quantity = 1,
                    totalCentimes = 750,
                    productId = "menu-etudiant",
                    studentMenuCustomization = StudentMenuCustomization(
                        choiceKind = StudentMenuChoiceKind.SANDWICH,
                        itemProductId = "sandwich-classico",
                        itemProductName = "Classico",
                        drinkId = "boisson-pepsi-33cl",
                        drinkLabel = "Pepsi"
                    )
                )
            ),
            totalCentimes = 750,
            createdAtEpochMillis = 1_782_200_000_000,
            statusLabel = "A regler"
        )

        val details = order.toKitchenTicketPrintPayload().lines.single().kitchenDetails
        val text = order.toKitchenTicketPrintPayload().toPrintableText()

        assertTrue(details.contains("MENU ETUDIANT"))
        assertTrue(details.contains("Classico"))
        assertTrue(details.contains("Pain focaccia"))
        assertTrue(details.contains("Mozza"))
        assertTrue(details.contains("Tomates fraîches"))
        assertTrue(details.contains("Pesto"))
        assertTrue(details.contains("Basilic"))
        assertTrue(details.contains("Pepsi"))
        assertTrue(text.contains("  - Pain focaccia"))
    }
}

private fun ByteArray.containsSequence(sequence: ByteArray): Boolean =
    indices.any { start ->
        start + sequence.size <= size && sequence.indices.all { offset ->
            this[start + offset] == sequence[offset]
        }
    }
