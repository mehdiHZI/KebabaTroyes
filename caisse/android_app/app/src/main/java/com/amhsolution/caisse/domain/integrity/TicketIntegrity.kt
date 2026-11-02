package com.amhsolution.caisse.domain.integrity

import com.amhsolution.caisse.domain.model.HistoryRecord
import com.amhsolution.caisse.domain.model.OrderLineSnapshot
import com.amhsolution.caisse.domain.model.PaymentRecord
import java.security.MessageDigest

/** Empreinte de genèse (premier ticket de la chaîne). */
const val INTEGRITY_GENESIS = "GENESIS"

/**
 * Chaînage SHA-256 des tickets validés (pilier I/S ISCA).
 * Payload canonique stable — aucun Float, montants en centimes.
 */
object TicketIntegrity {

    fun canonicalPayload(
        ticketNumber: Int,
        businessDay: String,
        paidAtEpochMillis: Long,
        orderType: String,
        totalCentimes: Int,
        lines: List<OrderLineSnapshot>,
        payments: List<PaymentRecord>,
        linkedTicketId: String?,
        isCorrection: Boolean,
        isSimulation: Boolean,
        registerId: String
    ): String {
        val linesPart = lines.joinToString(";") { line ->
            listOf(
                line.productId.orEmpty(),
                line.displayLabel.replace('|', '/'),
                line.quantity.toString(),
                (line.unitPriceCentimes ?: -1).toString(),
                line.totalCentimes.toString()
            ).joinToString(",")
        }
        val paymentsPart = payments.joinToString(";") { payment ->
            listOf(
                payment.method.name,
                payment.amountCentimes.toString(),
                (payment.receivedCentimes ?: -1).toString(),
                (payment.changeCentimes ?: -1).toString()
            ).joinToString(",")
        }
        return listOf(
            "v1",
            registerId,
            ticketNumber.toString(),
            businessDay,
            paidAtEpochMillis.toString(),
            orderType,
            totalCentimes.toString(),
            linesPart,
            paymentsPart,
            linkedTicketId.orEmpty(),
            if (isCorrection) "1" else "0",
            if (isSimulation) "1" else "0"
        ).joinToString("|")
    }

    fun hash(previousHash: String?, payload: String): String {
        val prev = previousHash?.takeIf { it.isNotBlank() } ?: INTEGRITY_GENESIS
        return sha256Hex("$prev\n$payload")
    }

    fun hashForRecord(record: HistoryRecord, previousHash: String?): String =
        hash(
            previousHash = previousHash,
            payload = canonicalPayload(
                ticketNumber = record.ticketNumber,
                businessDay = record.businessDay,
                paidAtEpochMillis = record.paidAtEpochMillis,
                orderType = record.orderType.name,
                totalCentimes = record.totalCentimes,
                lines = record.lines,
                payments = record.payments,
                linkedTicketId = record.linkedTicketId,
                isCorrection = record.isCorrection,
                isSimulation = record.isSimulation,
                registerId = record.registerId
            )
        )

    fun verify(record: HistoryRecord): Boolean {
        if (record.integrityHash.isBlank()) return false
        val expected = hashForRecord(record, record.previousIntegrityHash)
        return expected.equals(record.integrityHash, ignoreCase = true)
    }

    fun sha256Hex(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { byte -> "%02x".format(byte) }
    }
}
