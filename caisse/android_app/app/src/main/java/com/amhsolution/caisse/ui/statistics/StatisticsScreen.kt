package com.amhsolution.caisse.ui.statistics

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.amhsolution.caisse.domain.catalog.SampleHistoryRecords
import com.amhsolution.caisse.domain.closure.CashClosure
import com.amhsolution.caisse.domain.closure.CashClosureType
import com.amhsolution.caisse.domain.formatEuros
import com.amhsolution.caisse.domain.history.HistoryDateFilter
import com.amhsolution.caisse.domain.history.displayLabel
import com.amhsolution.caisse.domain.model.HistoryRecord
import com.amhsolution.caisse.domain.model.OrderType
import com.amhsolution.caisse.domain.model.PaymentMethod
import com.amhsolution.caisse.domain.statistics.computeStatistics
import com.amhsolution.caisse.ui.common.DateFilterSection
import com.amhsolution.caisse.ui.orders.HistoryCalendarDialog
import com.amhsolution.caisse.ui.components.KebabaScreenHeader
import com.amhsolution.caisse.ui.theme.AMHCaisseTheme
import com.amhsolution.caisse.ui.theme.CaisseMuted
import com.amhsolution.caisse.ui.theme.WireframeBorder
import com.amhsolution.caisse.ui.theme.WireframeGreenBackground
import com.amhsolution.caisse.ui.theme.WireframeGreenBorder
import com.amhsolution.caisse.ui.theme.WireframeGreenText

@Composable
fun StatisticsScreen(
    records: List<HistoryRecord>,
    closures: List<CashClosure> = emptyList(),
    closureErrorMessage: String? = null,
    onBack: () -> Unit,
    onCreateRapportX: (String) -> Unit = {},
    onCreateFinalZ: (String) -> Unit = {},
    /** @deprecated Utiliser onCreateRapportX */
    onCloseBusinessDay: (String) -> Unit = onCreateRapportX,
    modifier: Modifier = Modifier
) {
    var dateFilter by remember { mutableStateOf(HistoryDateFilter.today()) }
    var showCalendar by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf<Pair<String, String>?>(null) }

    val stats = remember(records, dateFilter) {
        records.computeStatistics(dateFilter = dateFilter)
    }
    val selectedBusinessDay = (dateFilter as? HistoryDateFilter.Single)?.day?.toString()
    val selectedDayRecords = remember(records, selectedBusinessDay) {
        if (selectedBusinessDay == null) emptyList()
        else records.filter { it.businessDay == selectedBusinessDay }
    }
    val selectedDayZ = remember(closures, selectedBusinessDay) {
        closures.firstOrNull {
            it.businessDay == selectedBusinessDay &&
                it.type == CashClosureType.DAILY_Z &&
                it.isFinal
        }
    }
    val selectedDayX = remember(closures, selectedBusinessDay) {
        closures.firstOrNull {
            it.businessDay == selectedBusinessDay && it.type == CashClosureType.DAILY_X
        }
    }

    pendingAction?.let { (day, kind) ->
        AlertDialog(
            onDismissRequest = { pendingAction = null },
            title = {
                Text(if (kind == "Z") "Cloture Z definitive" else "Rapport X provisoire")
            },
            text = {
                Text(
                    if (kind == "Z") {
                        "Figer la journee $day ? Apres Z, aucune reecriture de ticket : corrections uniquement par avoir."
                    } else {
                        "Generer un rapport X pour $day ? Le rapport est provisoire et ne verrouille pas la journee."
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingAction = null
                        if (kind == "Z") onCreateFinalZ(day) else onCreateRapportX(day)
                    }
                ) {
                    Text(if (kind == "Z") "Cloturer Z" else "Rapport X")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingAction = null }) {
                    Text("Annuler")
                }
            }
        )
    }

    if (showCalendar) {
        HistoryCalendarDialog(
            onDismiss = { showCalendar = false },
            onConfirm = { filter ->
                dateFilter = filter
                showCalendar = false
            }
        )
    }
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 14.dp)
        ) {
            KebabaScreenHeader(
                title = "Statistiques",
                subtitle = dateFilter.displayLabel(),
                onBack = onBack,
                backLabel = "Retour accueil"
            )

            Spacer(modifier = Modifier.height(10.dp))

            DateFilterSection(
                dateFilterLabel = dateFilter.displayLabel(),
                onOpenCalendar = { showCalendar = true },
                onResetToday = { dateFilter = HistoryDateFilter.today() }
            )

            Spacer(modifier = Modifier.height(12.dp))

            StatRow(label = "Commandes totales", value = stats.totalOrders.toString())
            StatRow(label = "Commandes encaissées", value = stats.activeOrders.toString())
            StatRow(label = "Commandes annulées", value = stats.cancelledOrders.toString())
            StatRow(
                label = "Chiffre d'affaires total",
                value = formatEuros(stats.totalRevenueCentimes),
                highlight = true
            )
            StatRow(
                label = "Espèces",
                value = formatEuros(stats.cashRevenueCentimes)
            )
            StatRow(
                label = "CB",
                value = formatEuros(stats.cardRevenueCentimes)
            )
            StatRow(
                label = "Ticket restaurant",
                value = formatEuros(stats.ticketRestoRevenueCentimes)
            )
            StatRow(
                label = "Autre",
                value = formatEuros(stats.otherRevenueCentimes)
            )

            Spacer(modifier = Modifier.height(10.dp))

            DailyClosureCard(
                businessDay = selectedBusinessDay,
                records = selectedDayRecords,
                finalZ = selectedDayZ,
                rapportX = selectedDayX,
                errorMessage = closureErrorMessage,
                onRequestRapportX = { day -> pendingAction = day to "X" },
                onRequestFinalZ = { day -> pendingAction = day to "Z" }
            )
        }
    }
}

@Composable
fun StatisticsScreen(
    records: List<HistoryRecord>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    StatisticsScreen(
        records = records,
        closures = emptyList(),
        onBack = onBack,
        modifier = modifier
    )
}

@Composable
private fun DailyClosureCard(
    businessDay: String?,
    records: List<HistoryRecord>,
    finalZ: CashClosure?,
    rapportX: CashClosure?,
    errorMessage: String?,
    onRequestRapportX: (String) -> Unit,
    onRequestFinalZ: (String) -> Unit
) {
    val canRapportX = businessDay != null && records.isNotEmpty()
    val canFinalZ = businessDay != null && records.isNotEmpty() && finalZ == null

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, WireframeBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Cloture caisse journaliere",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Rapport X = provisoire (illimite). Cloture Z = definitive (une seule par jour). " +
                    "Apres Z : corrections uniquement par avoir. Z auto en fin de journee si absente.",
                style = MaterialTheme.typography.bodyMedium,
                color = CaisseMuted
            )
            errorMessage?.let { message ->
                Text(
                    text = "Erreur : $message",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.error
                )
            }

            when {
                businessDay == null -> {
                    Text(
                        text = "Selectionner une seule journee pour cloturer.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CaisseMuted
                    )
                }

                finalZ != null -> {
                    ClosureSummary(finalZ)
                }

                else -> {
                    rapportX?.let { ClosureSummary(it) }
                    if (records.isEmpty()) {
                        Text(
                            text = "Aucun ticket encaisse sur cette journee.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CaisseMuted
                        )
                    } else {
                        DailyClosurePreview(records)
                    }
                }
            }

            if (canRapportX && businessDay != null && finalZ == null) {
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    onClick = { onRequestRapportX(businessDay) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = WireframeGreenBackground),
                    border = BorderStroke(1.dp, WireframeGreenBorder)
                ) {
                    Text(
                        text = if (rapportX == null) "Generer rapport X" else "Actualiser rapport X",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        color = WireframeGreenText
                    )
                }
            }

            if (canFinalZ && businessDay != null) {
                Card(
                    onClick = { onRequestFinalZ(businessDay) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, WireframeBorder)
                ) {
                    Text(
                        text = "Cloture Z definitive",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyClosurePreview(records: List<HistoryRecord>) {
    val activeRecords = records.filterNot { it.isCancelled }
    val cancelledRecords = records.count { it.isCancelled }
    val totalCentimes = activeRecords.sumOf { it.effectiveTotalCentimes }
    val firstTicket = activeRecords.minOfOrNull { it.ticketNumber }
    val lastTicket = activeRecords.maxOfOrNull { it.ticketNumber }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Tickets : ${activeRecords.size}", style = MaterialTheme.typography.bodyMedium)
        if (cancelledRecords > 0) {
            Text(
                text = "Annulés : $cancelledRecords",
                style = MaterialTheme.typography.bodyMedium,
                color = CaisseMuted
            )
        }
        Text(
            text = "Plage : ${formatTicketRange(firstTicket, lastTicket)}",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "Total provisoire : ${formatEuros(totalCentimes)}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Cloture automatique a partir de 23:59.",
            style = MaterialTheme.typography.bodySmall,
            color = CaisseMuted
        )
    }
}

@Composable
private fun ClosureSummary(closure: CashClosure) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = closure.statusLabel,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = WireframeGreenText
        )
        if (closure.isFinal) {
            Text(
                text = "Tickets de cette journee verrouilles.",
                style = MaterialTheme.typography.bodySmall,
                color = CaisseMuted
            )
        } else {
            Text(
                text = "Provisoire : les tickets restent modifiables jusqu'a 23:59.",
                style = MaterialTheme.typography.bodySmall,
                color = CaisseMuted
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text("Tickets : ${closure.ticketCount}", style = MaterialTheme.typography.bodySmall)
                Text("Plage : ${closure.ticketRangeLabel}", style = MaterialTheme.typography.bodySmall)
                Text("Articles : ${closure.itemCount}", style = MaterialTheme.typography.bodySmall)
                Text(
                    text = "Total : ${formatEuros(closure.totalRevenueCentimes)}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                ClosureTotalsRow("Espèces", closure.paymentTotalsCentimes[PaymentMethod.ESPECES] ?: 0)
                ClosureTotalsRow("CB", closure.paymentTotalsCentimes[PaymentMethod.CARTE_BANCAIRE] ?: 0)
                ClosureTotalsRow(
                    "Ticket restaurant",
                    closure.paymentTotalsCentimes[PaymentMethod.TITRE_RESTAURANT] ?: 0
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                ClosureTotalsRow("Sur place", closure.orderTypeTotalsCentimes[OrderType.SUR_PLACE] ?: 0)
                ClosureTotalsRow("A emporter", closure.orderTypeTotalsCentimes[OrderType.A_EMPORTER] ?: 0)
                ClosureTotalsRow("Livraison", closure.orderTypeTotalsCentimes[OrderType.LIVRAISON] ?: 0)
            }
        }
    }
}

@Composable
private fun ClosureTotalsRow(label: String, amountCentimes: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = CaisseMuted)
        Text(formatEuros(amountCentimes), style = MaterialTheme.typography.bodySmall)
    }
}

private fun formatTicketRange(firstTicket: Int?, lastTicket: Int?): String =
    when {
        firstTicket == null || lastTicket == null -> "Aucun ticket"
        firstTicket == lastTicket -> "#$firstTicket"
        else -> "#$firstTicket - #$lastTicket"
    }

@Composable
private fun StatRow(
    label: String,
    value: String,
    highlight: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, WireframeBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (highlight) WireframeGreenText else MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 1280, heightDp = 800)
@Composable
private fun StatisticsScreenPreview() {
    AMHCaisseTheme {
        StatisticsScreen(
            records = SampleHistoryRecords.records,
            closures = emptyList(),
            onBack = {}
        )
    }
}
