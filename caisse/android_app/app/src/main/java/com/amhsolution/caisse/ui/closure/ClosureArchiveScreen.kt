package com.amhsolution.caisse.ui.closure

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.amhsolution.caisse.domain.closure.CashClosure
import com.amhsolution.caisse.domain.closure.CashClosureType
import com.amhsolution.caisse.domain.formatEuros
import com.amhsolution.caisse.domain.model.OrderType
import com.amhsolution.caisse.domain.model.PaymentMethod
import com.amhsolution.caisse.ui.theme.AMHCaisseTheme
import com.amhsolution.caisse.ui.theme.CaisseMuted
import com.amhsolution.caisse.ui.theme.WireframeBorder
import com.amhsolution.caisse.ui.theme.WireframeGreenText
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val AnnualPeriodOption = "__ANNUAL__"

@Composable
fun ClosureArchiveScreen(
    closures: List<CashClosure>,
    exportPathForClosure: (CashClosure) -> String,
    onPrintClosure: (CashClosure) -> Unit,
    onExportClosure: (CashClosure) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sortedClosures = remember(closures) {
        closures.sortedByDescending { it.businessDay }
    }
    val years = remember(sortedClosures) {
        sortedClosures.map { periodYear(it) }.distinct()
    }
    var selectedYear by remember(years) { mutableStateOf(years.firstOrNull()) }
    val monthOptions = remember(sortedClosures, selectedYear) {
        val selectedYearClosures = sortedClosures.filter {
            selectedYear == null || periodYear(it) == selectedYear
        }
        val hasYearClosure = selectedYearClosures.any { it.type == CashClosureType.YEARLY_Z }
        val months = selectedYearClosures
            .mapNotNull { periodMonthOrNull(it) }
            .distinct()
        if (hasYearClosure) listOf(AnnualPeriodOption) + months else months
    }
    var selectedMonth by remember(selectedYear, monthOptions) {
        mutableStateOf(monthOptions.firstOrNull())
    }
    val closureItems = remember(sortedClosures, selectedYear, selectedMonth) {
        sortedClosures
            .filter { closure ->
                selectedYear == null || periodYear(closure) == selectedYear
            }
            .filter { closure ->
                when (selectedMonth) {
                    AnnualPeriodOption -> closure.type == CashClosureType.YEARLY_Z
                    null -> false
                    else -> closure.type != CashClosureType.YEARLY_Z &&
                        periodMonthOrNull(closure) == selectedMonth
                }
            }
            .sortedWith(
                compareBy<CashClosure> { closureTypeSortOrder(it.type) }
                    .thenByDescending { it.businessDay }
            )
    }
    var selectedClosureId by remember(selectedYear, selectedMonth, closureItems) {
        mutableStateOf(closureItems.firstOrNull()?.id)
    }
    val selectedClosure = closureItems.firstOrNull { it.id == selectedClosureId }
        ?: closureItems.firstOrNull()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Cloture",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tickets Z journaliers, mensuels et annuels",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CaisseMuted
                    )
                }
                TextButton(onClick = onBack) {
                    Text("Retour accueil")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = WireframeBorder.copy(alpha = 0.25f))
            Spacer(modifier = Modifier.height(16.dp))

            if (sortedClosures.isEmpty()) {
                EmptyClosureState()
            } else {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    if (maxWidth < 850.dp) {
                        CompactClosureLayout(
                            years = years,
                            months = monthOptions,
                            closures = closureItems,
                            selectedYear = selectedYear,
                            selectedMonth = selectedMonth,
                            selectedClosure = selectedClosure,
                            exportPathForClosure = exportPathForClosure,
                            onPrintClosure = onPrintClosure,
                            onExportClosure = onExportClosure,
                            onYearSelected = { selectedYear = it },
                            onMonthSelected = { selectedMonth = it },
                            onClosureSelected = { selectedClosureId = it.id }
                        )
                    } else {
                        WideClosureLayout(
                            years = years,
                            months = monthOptions,
                            closures = closureItems,
                            selectedYear = selectedYear,
                            selectedMonth = selectedMonth,
                            selectedClosure = selectedClosure,
                            exportPathForClosure = exportPathForClosure,
                            onPrintClosure = onPrintClosure,
                            onExportClosure = onExportClosure,
                            onYearSelected = { selectedYear = it },
                            onMonthSelected = { selectedMonth = it },
                            onClosureSelected = { selectedClosureId = it.id }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyClosureState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, WireframeBorder)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Aucune cloture enregistree",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "L'application creera automatiquement les clotures journalieres, mensuelles et annuelles quand les periodes seront terminees.",
                style = MaterialTheme.typography.bodyMedium,
                color = CaisseMuted
            )
        }
    }
}

@Composable
private fun WideClosureLayout(
    years: List<String>,
    months: List<String>,
    closures: List<CashClosure>,
    selectedYear: String?,
    selectedMonth: String?,
    selectedClosure: CashClosure?,
    exportPathForClosure: (CashClosure) -> String,
    onPrintClosure: (CashClosure) -> Unit,
    onExportClosure: (CashClosure) -> Unit,
    onYearSelected: (String) -> Unit,
    onMonthSelected: (String) -> Unit,
    onClosureSelected: (CashClosure) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ArchiveColumn(
            title = "Annees",
            items = years,
            selectedItem = selectedYear,
            itemLabel = { it },
            onItemSelected = onYearSelected,
            modifier = Modifier.weight(0.8f)
        )
        ArchiveColumn(
            title = "Mois",
            items = months,
            selectedItem = selectedMonth,
            itemLabel = { monthOptionLabel(it) },
            onItemSelected = onMonthSelected,
            modifier = Modifier.weight(0.9f)
        )
        ArchiveColumn(
            title = "Clotures",
            items = closures,
            selectedItem = selectedClosure,
            itemLabel = { closureListLabel(it) },
            onItemSelected = onClosureSelected,
            modifier = Modifier.weight(1f)
        )
        ClosureDetailCard(
            closure = selectedClosure,
            exportPathForClosure = exportPathForClosure,
            onPrintClosure = onPrintClosure,
            onExportClosure = onExportClosure,
            modifier = Modifier.weight(2f)
        )
    }
}

@Composable
private fun CompactClosureLayout(
    years: List<String>,
    months: List<String>,
    closures: List<CashClosure>,
    selectedYear: String?,
    selectedMonth: String?,
    selectedClosure: CashClosure?,
    exportPathForClosure: (CashClosure) -> String,
    onPrintClosure: (CashClosure) -> Unit,
    onExportClosure: (CashClosure) -> Unit,
    onYearSelected: (String) -> Unit,
    onMonthSelected: (String) -> Unit,
    onClosureSelected: (CashClosure) -> Unit
) {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ArchiveRow(
            title = "Annees",
            items = years,
            selectedItem = selectedYear,
            itemLabel = { it },
            onItemSelected = onYearSelected
        )
        ArchiveRow(
            title = "Mois",
            items = months,
            selectedItem = selectedMonth,
            itemLabel = { monthOptionLabel(it) },
            onItemSelected = onMonthSelected
        )
        ArchiveRow(
            title = "Clotures",
            items = closures,
            selectedItem = selectedClosure,
            itemLabel = { closureListLabel(it) },
            onItemSelected = onClosureSelected
        )
        ClosureDetailCard(
            closure = selectedClosure,
            exportPathForClosure = exportPathForClosure,
            onPrintClosure = onPrintClosure,
            onExportClosure = onExportClosure
        )
    }
}

@Composable
private fun <T> ArchiveColumn(
    title: String,
    items: List<T>,
    selectedItem: T?,
    itemLabel: (T) -> String,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, WireframeBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            items.forEach { item ->
                ArchiveButton(
                    label = itemLabel(item),
                    selected = item == selectedItem,
                    onClick = { onItemSelected(item) }
                )
            }
        }
    }
}

@Composable
private fun <T> ArchiveRow(
    title: String,
    items: List<T>,
    selectedItem: T?,
    itemLabel: (T) -> String,
    onItemSelected: (T) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items.forEach { item ->
                ArchiveButton(
                    label = itemLabel(item),
                    selected = item == selectedItem,
                    onClick = { onItemSelected(item) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ArchiveButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (selected) WireframeGreenText.copy(alpha = 0.12f) else Color.White
        ),
        border = BorderStroke(1.dp, if (selected) WireframeGreenText else WireframeBorder),
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(
            text = label,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun ClosureDetailCard(
    closure: CashClosure?,
    exportPathForClosure: (CashClosure) -> String,
    onPrintClosure: (CashClosure) -> Unit,
    onExportClosure: (CashClosure) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxHeight(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, WireframeBorder)
    ) {
        if (closure == null) {
            Text(
                text = "Selectionner une cloture.",
                modifier = Modifier.padding(18.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = CaisseMuted
            )
            return@Card
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "${closure.type.label} - ${closurePeriodLabel(closure)}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            if (closure.type == CashClosureType.DAILY_Z) {
                Text(
                    text = closure.statusLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = WireframeGreenText
                )
            }
            Text(
                text = exportPathForClosure(closure),
                style = MaterialTheme.typography.bodySmall,
                color = CaisseMuted
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onPrintClosure(closure) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, WireframeGreenText)
                ) {
                    Text(
                        text = "Imprimer ticket Z",
                        fontWeight = FontWeight.Bold,
                        color = WireframeGreenText
                    )
                }
                OutlinedButton(
                    onClick = { onExportClosure(closure) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, WireframeBorder)
                ) {
                    Text(
                        text = "Exporter PDF",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            HorizontalDivider(color = WireframeBorder.copy(alpha = 0.25f))
            DetailRow("Nombre de tickets", closure.ticketCount.toString())
            DetailRow("Plage tickets", closure.ticketRangeLabel)
            DetailRow("Total tickets", formatEuros(closure.totalRevenueCentimes))
            DetailRow("Total encaisse", formatEuros(closure.totalRevenueCentimes))
            DetailRow("Nombre d'articles", closure.itemCount.toString())
            DetailRow("Commandes annulées", closure.cancelledOrderCount.toString())
            SectionTitle("Encaissements")
            PaymentMethod.entries.filterNot { it == PaymentMethod.ANNULE }.forEach { method ->
                DetailRow(
                    "${method.label} [${closure.paymentCounts[method] ?: 0}]",
                    formatEuros(closure.paymentTotalsCentimes[method] ?: 0)
                )
            }
            SectionTitle("Types de commande")
            OrderType.entries.forEach { type ->
                DetailRow(
                    type.label,
                    formatEuros(closure.orderTypeTotalsCentimes[type] ?: 0)
                )
            }
            SectionTitle("Informations de cloture")
            DetailRow("Date de cloture", formatClosureDateTime(closure.closedAtEpochMillis))
        }
    }
}

@Composable
private fun SectionTitle(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = CaisseMuted)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

private fun periodYear(closure: CashClosure): String =
    closure.businessDay.take(4)

private fun periodMonthOrNull(closure: CashClosure): String? =
    when (closure.type) {
        CashClosureType.DAILY_Z,
        CashClosureType.MONTHLY_Z -> closure.businessDay.substring(5, 7)
        CashClosureType.YEARLY_Z -> null
    }

private fun closureTypeSortOrder(type: CashClosureType): Int =
    when (type) {
        CashClosureType.YEARLY_Z -> 0
        CashClosureType.MONTHLY_Z -> 1
        CashClosureType.DAILY_Z -> 2
    }

private fun monthOptionLabel(option: String): String =
    if (option == AnnualPeriodOption) "Annee complete" else monthLabel(option)

private fun monthLabel(month: String): String =
    when (month) {
        "01" -> "Janvier"
        "02" -> "Fevrier"
        "03" -> "Mars"
        "04" -> "Avril"
        "05" -> "Mai"
        "06" -> "Juin"
        "07" -> "Juillet"
        "08" -> "Aout"
        "09" -> "Septembre"
        "10" -> "Octobre"
        "11" -> "Novembre"
        "12" -> "Decembre"
        else -> month
    }

private fun closureListLabel(closure: CashClosure): String =
    when (closure.type) {
        CashClosureType.DAILY_Z -> dayLabel(closure.businessDay)
        CashClosureType.MONTHLY_Z -> "Mois complet"
        CashClosureType.YEARLY_Z -> "Annee complete"
    }

private fun closurePeriodLabel(closure: CashClosure): String =
    when (closure.type) {
        CashClosureType.DAILY_Z -> dayLabel(closure.businessDay)
        CashClosureType.MONTHLY_Z -> "${monthLabel(closure.businessDay.substring(5, 7))} ${periodYear(closure)}"
        CashClosureType.YEARLY_Z -> periodYear(closure)
    }

private fun dayLabel(businessDay: String): String {
    val day = LocalDate.parse(businessDay)
    return day.format(DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.FRANCE))
}

private fun formatClosureDateTime(epochMillis: Long): String =
    Instant.ofEpochMilli(epochMillis)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.FRANCE))

@Preview(showBackground = true, widthDp = 1280, heightDp = 800)
@Composable
private fun ClosureArchiveScreenPreview() {
    AMHCaisseTheme {
        ClosureArchiveScreen(
            closures = listOf(
                CashClosure(
                    id = "daily-z-2026-06-24",
                    type = CashClosureType.DAILY_Z,
                    businessDay = "2026-06-24",
                    closedAtEpochMillis = System.currentTimeMillis(),
                    ticketCount = 6,
                    firstTicketNumber = 1,
                    lastTicketNumber = 6,
                    totalRevenueCentimes = 6000,
                    itemCount = 8,
                    averageBasketCentimes = 1000,
                    paymentTotalsCentimes = mapOf(
                        PaymentMethod.ESPECES to 3000,
                        PaymentMethod.CARTE_BANCAIRE to 3000
                    ),
                    paymentCounts = mapOf(
                        PaymentMethod.ESPECES to 3,
                        PaymentMethod.CARTE_BANCAIRE to 3
                    ),
                    orderTypeTotalsCentimes = mapOf(OrderType.SUR_PLACE to 6000),
                    isFinal = true
                )
            ),
            exportPathForClosure = { "cloture/2026/06/2026-06-24-ticket-z.pdf" },
            onPrintClosure = {},
            onExportClosure = {},
            onBack = {}
        )
    }
}
