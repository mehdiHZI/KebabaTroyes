package com.amhsolution.caisse.ui.archive

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.amhsolution.caisse.data.repository.FiscalArchiveSummary
import com.amhsolution.caisse.domain.history.currentBusinessDay
import com.amhsolution.caisse.domain.model.HistoryRecord
import com.amhsolution.caisse.ui.components.KebabaScreenHeader
import com.amhsolution.caisse.ui.theme.CaisseMuted
import com.amhsolution.caisse.ui.theme.WireframeBorder
import com.amhsolution.caisse.ui.theme.WireframeGreenText
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.launch

private val dayFormatter = DateTimeFormatter.ISO_LOCAL_DATE
private val displayDateTime = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.FRANCE)

@Composable
fun FiscalArchiveScreen(
    archives: List<FiscalArchiveSummary>,
    historyRecords: List<HistoryRecord>,
    onCreateArchive: suspend (periodFrom: String, periodTo: String) -> Result<FiscalArchiveSummary>,
    onShareArchive: (FiscalArchiveSummary) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val defaults = remember(historyRecords) { defaultPeriod(historyRecords) }
    var periodFrom by remember(defaults) { mutableStateOf(defaults.first) }
    var periodTo by remember(defaults) { mutableStateOf(defaults.second) }
    var busy by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var statusOk by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val sortedArchives = remember(archives) {
        archives.sortedByDescending { it.createdAtEpochMillis }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 18.dp)
        ) {
            KebabaScreenHeader(
                title = "Archive fiscale",
                subtitle = "Export figé pour contrôle (hors FACTICE) — PIN responsable",
                onBack = onBack,
                backLabel = "Retour accueil"
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = WireframeBorder.copy(alpha = 0.25f))
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Période (jours d’activité AAAA-MM-JJ)",
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = periodFrom,
                    onValueChange = {
                        periodFrom = it.trim()
                        statusMessage = null
                    },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    label = { Text("Du") },
                    placeholder = { Text("2026-01-01") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = periodTo,
                    onValueChange = {
                        periodTo = it.trim()
                        statusMessage = null
                    },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    label = { Text("Au") },
                    placeholder = { Text(currentBusinessDay()) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                statusMessage?.let { message ->
                    Text(
                        text = message,
                        color = if (statusOk) WireframeGreenText else MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                } ?: Spacer(modifier = Modifier.weight(1f))
                Button(
                    enabled = !busy,
                    onClick = {
                        scope.launch {
                            busy = true
                            statusMessage = null
                            val fromOk = runCatching { LocalDate.parse(periodFrom, dayFormatter) }.isSuccess
                            val toOk = runCatching { LocalDate.parse(periodTo, dayFormatter) }.isSuccess
                            if (!fromOk || !toOk) {
                                statusOk = false
                                statusMessage = "Dates invalides (format AAAA-MM-JJ)."
                                busy = false
                                return@launch
                            }
                            if (periodFrom > periodTo) {
                                statusOk = false
                                statusMessage = "La date de début doit précéder la fin."
                                busy = false
                                return@launch
                            }
                            val result = onCreateArchive(periodFrom, periodTo)
                            busy = false
                            result.onSuccess { summary ->
                                statusOk = true
                                statusMessage =
                                    "Archive créée : ${summary.ticketCount} ticket(s), empreinte ${summary.archiveHash.take(12)}…"
                            }.onFailure { error ->
                                statusOk = false
                                statusMessage = error.message ?: "Échec de l'archive."
                            }
                        }
                    }
                ) {
                    Text(if (busy) "Génération…" else "Générer l'archive")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Archives déjà générées",
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Fichier JSON ouvert + notice — partageable pour contrôle.",
                color = CaisseMuted,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (sortedArchives.isEmpty()) {
                Text(
                    text = "Aucune archive pour l’instant.",
                    color = CaisseMuted,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(sortedArchives, key = { it.id }) { archive ->
                        ArchiveRow(
                            archive = archive,
                            onShare = { onShareArchive(archive) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ArchiveRow(
    archive: FiscalArchiveSummary,
    onShare: () -> Unit
) {
    val createdLabel = remember(archive.createdAtEpochMillis) {
        Instant.ofEpochMilli(archive.createdAtEpochMillis)
            .atZone(ZoneId.systemDefault())
            .format(displayDateTime)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(1.dp, WireframeBorder.copy(alpha = 0.35f)), RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "${archive.periodFrom} → ${archive.periodTo}",
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "$createdLabel · ${archive.ticketCount} ticket(s) · ${archive.archiveHash.take(16)}…",
                color = CaisseMuted,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        TextButton(onClick = onShare) {
            Text("Partager")
        }
    }
}

private fun defaultPeriod(records: List<HistoryRecord>): Pair<String, String> {
    val productionDays = records
        .asSequence()
        .filterNot { it.isSimulation }
        .map { it.businessDay }
        .filter { it.isNotBlank() }
        .sorted()
        .toList()
    val today = currentBusinessDay()
    if (productionDays.isEmpty()) {
        val yearStart = runCatching {
            LocalDate.parse(today, dayFormatter).withDayOfYear(1).format(dayFormatter)
        }.getOrDefault(today)
        return yearStart to today
    }
    return productionDays.first() to productionDays.last()
}
