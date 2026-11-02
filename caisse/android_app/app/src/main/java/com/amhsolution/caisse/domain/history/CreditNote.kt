package com.amhsolution.caisse.domain.history

import com.amhsolution.caisse.domain.model.HistoryRecord
import com.amhsolution.caisse.domain.model.PaymentMethod
import com.amhsolution.caisse.domain.model.PaymentRecord
import java.util.UUID

/**
 * Crée un avoir / contre-opération (nouvelle transaction) liée au ticket d'origine.
 * Le ticket d'origine n'est jamais modifié (inaltérabilité ISCA).
 */
fun HistoryRecord.toCreditNote(
    ticketNumber: Int,
    createdAtEpochMillis: Long = System.currentTimeMillis()
): HistoryRecord {
    require(!isCorrection) { "Impossible d'émettre un avoir sur une contre-operation." }
    require(!isCancelled) { "Ticket deja annule (legacy ANNULE) — pas d'avoir." }
    require(effectiveTotalCentimes > 0) { "Montant avoir invalide." }

    val creditAmount = -effectiveTotalCentimes
    return copy(
        id = UUID.randomUUID().toString(),
        ticketNumber = ticketNumber,
        businessDay = businessDayFromEpochMillis(createdAtEpochMillis),
        paidAtEpochMillis = createdAtEpochMillis,
        totalCentimes = creditAmount,
        payments = listOf(
            PaymentRecord(
                method = PaymentMethod.AVOIR,
                amountCentimes = creditAmount
            )
        ),
        linkedTicketId = id,
        isCorrection = true,
        integrityHash = "",
        previousIntegrityHash = null,
        lines = lines.map { line ->
            line.copy(
                unitPriceCentimes = line.unitPriceCentimes?.let { -kotlin.math.abs(it) },
                totalCentimes = -kotlin.math.abs(line.totalCentimes)
            )
        }
    )
}
