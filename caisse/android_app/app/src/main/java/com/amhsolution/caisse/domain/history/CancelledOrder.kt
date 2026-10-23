package com.amhsolution.caisse.domain.history

import com.amhsolution.caisse.domain.model.HistoryRecord
import com.amhsolution.caisse.domain.model.OrderTicket
import com.amhsolution.caisse.domain.model.PaymentMethod
import com.amhsolution.caisse.domain.model.PaymentRecord
import java.time.ZoneId

/**
 * Transforme une commande en attente annulee en ticket d'audit conserve dans l'historique.
 * Le total d'origine reste consultable, mais [HistoryRecord.effectiveTotalCentimes] vaut zero.
 */
fun OrderTicket.toCancelledHistoryRecord(
    cancelledAtEpochMillis: Long = System.currentTimeMillis(),
    zoneId: ZoneId = ZoneId.systemDefault()
): HistoryRecord =
    HistoryRecord(
        id = "cancelled-$id",
        ticketNumber = ticketNumber,
        businessDay = businessDayFromEpochMillis(createdAtEpochMillis, zoneId),
        paidAtEpochMillis = cancelledAtEpochMillis,
        orderType = orderType,
        lines = lines,
        totalCentimes = totalCentimes,
        payments = listOf(PaymentRecord(PaymentMethod.ANNULE, 0)),
        deliveryInfo = deliveryInfo
    )
