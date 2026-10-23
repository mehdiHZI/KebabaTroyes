package com.amhsolution.caisse.ui.orders

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.amhsolution.caisse.domain.catalog.SampleHistoryRecords
import com.amhsolution.caisse.domain.formatEuros
import com.amhsolution.caisse.domain.formatEurosAmount
import com.amhsolution.caisse.domain.history.HistoryDateFilter
import com.amhsolution.caisse.domain.history.HistorySort
import com.amhsolution.caisse.domain.history.displayLabel
import com.amhsolution.caisse.domain.history.matchesBusinessDay
import com.amhsolution.caisse.domain.history.sortedForHistory
import com.amhsolution.caisse.domain.model.HistoryRecord
import com.amhsolution.caisse.domain.model.OrderType
import com.amhsolution.caisse.domain.model.PaymentMethod
import com.amhsolution.caisse.domain.model.PaymentRecord
import com.amhsolution.caisse.domain.payment.paymentMethodShortLabel
import com.amhsolution.caisse.ui.theme.AMHCaisseTheme
import com.amhsolution.caisse.ui.theme.CaisseBlue
import com.amhsolution.caisse.ui.theme.CaisseBlueDark
import com.amhsolution.caisse.ui.theme.CaisseMuted
import com.amhsolution.caisse.ui.theme.WireframeBorder
import com.amhsolution.caisse.ui.theme.WireframeGreenText
import com.amhsolution.caisse.ui.theme.WireframeNavBackground

@Composable
fun HistoryScreen(
    records: List<HistoryRecord>,
    onBack: () -> Unit,
    onOpenTicket: (HistoryRecord) -> Unit,
    modifier: Modifier = Modifier
) {
    var dateFilter by remember { mutableStateOf(HistoryDateFilter.today()) }
    var showCalendar by remember { mutableStateOf(false) }
    var selectedOrderType by remember { mutableStateOf<OrderType?>(null) }
    var selectedPaymentMethod by remember { mutableStateOf<PaymentMethod?>(null) }
    var ticketSearch by remember { mutableStateOf("") }
    var historySort by remember { mutableStateOf(HistorySort.DATE) }
    var sortAscending by remember { mutableStateOf(false) }

    val filteredRecords = remember(
        records,
        dateFilter,
        selectedOrderType,
        selectedPaymentMethod,
        ticketSearch,
        historySort,
        sortAscending
    ) {
        records.filter { record ->
            val matchesDay = dateFilter.matchesBusinessDay(record.businessDay)
            val matchesType = selectedOrderType == null || record.orderType == selectedOrderType
            val matchesPayment = selectedPaymentMethod == null ||
                record.payments.any { it.method == selectedPaymentMethod }
            val matchesTicket = ticketSearch.isBlank() ||
                record.ticketNumber.toString().contains(ticketSearch.trim())
            matchesDay && matchesType && matchesPayment && matchesTicket
        }.sortedForHistory(historySort, sortAscending)
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
                .padding(horizontal = 24.dp, vertical = 18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "06 - Historique",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Consultation des tickets et paiements",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CaisseMuted
                    )
                }

                TextButton(onClick = onBack) {
                    Text("Retour accueil")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = WireframeBorder.copy(alpha = 0.25f))
            Spacer(modifier = Modifier.height(14.dp))

            HistoryFilterBar(
                dateFilterLabel = dateFilter.displayLabel(),
                onOpenCalendar = { showCalendar = true },
                onResetToday = { dateFilter = HistoryDateFilter.today() },
                selectedOrderType = selectedOrderType,
                onOrderTypeChange = { selectedOrderType = it },
                selectedPaymentMethod = selectedPaymentMethod,
                onPaymentMethodChange = { selectedPaymentMethod = it },
                ticketSearch = ticketSearch,
                onTicketSearchChange = { ticketSearch = it },
                historySort = historySort,
                onHistorySortChange = { historySort = it },
                sortAscending = sortAscending,
                onToggleSortDirection = { sortAscending = !sortAscending }
            )

            Spacer(modifier = Modifier.height(14.dp))

            HistoryTableHeader()

            if (filteredRecords.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Aucun ticket pour ces filtres.",
                        style = MaterialTheme.typography.titleMedium,
                        color = CaisseMuted
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    items(filteredRecords, key = { it.id }) { record ->
                        HistoryTableRow(
                            record = record,
                            onOpenTicket = { onOpenTicket(record) }
                        )
                        HorizontalDivider(color = WireframeBorder.copy(alpha = 0.2f))
                    }
                }
            }

        }
    }
}

@Composable
private fun HistoryFilterBar(
    dateFilterLabel: String,
    onOpenCalendar: () -> Unit,
    onResetToday: () -> Unit,
    selectedOrderType: OrderType?,
    onOrderTypeChange: (OrderType?) -> Unit,
    selectedPaymentMethod: PaymentMethod?,
    onPaymentMethodChange: (PaymentMethod?) -> Unit,
    ticketSearch: String,
    onTicketSearchChange: (String) -> Unit,
    historySort: HistorySort,
    onHistorySortChange: (HistorySort) -> Unit,
    sortAscending: Boolean,
    onToggleSortDirection: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = WireframeNavBackground),
        border = BorderStroke(1.dp, WireframeBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Filtres et tri des tickets",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChipButton(
                    label = dateFilterLabel,
                    selected = true,
                    onClick = onOpenCalendar
                )
                TextButton(onClick = onResetToday) {
                    Text("Aujourd'hui")
                }

                HistoryDropdownFilter(
                    label = selectedOrderType?.label ?: "Type ticket : tout",
                    options = listOf(null to "Tout") + OrderType.entries.map { it to it.label },
                    onSelected = { value -> onOrderTypeChange(value) },
                    modifier = Modifier.weight(1f)
                )

                HistoryDropdownFilter(
                    label = selectedPaymentMethod?.let(::paymentMethodShortLabel) ?: "Paiement : tout",
                    options = listOf(null to "Tout") + PaymentMethod.entries.map {
                        it to paymentMethodShortLabel(it)
                    },
                    onSelected = { value -> onPaymentMethodChange(value) },
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = ticketSearch,
                    onValueChange = onTicketSearchChange,
                    modifier = Modifier.weight(1f),
                    label = { Text("Ticket #") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HistoryDropdownFilter(
                    label = "Trier par : ${historySort.label}",
                    options = HistorySort.entries.map { it to it.label },
                    onSelected = { value -> value?.let(onHistorySortChange) },
                    modifier = Modifier.weight(1f)
                )
                FilterChipButton(
                    label = if (sortAscending) "Ordre croissant" else "Ordre décroissant",
                    selected = true,
                    onClick = onToggleSortDirection
                )
                Spacer(modifier = Modifier.weight(2f))
            }
        }
    }
}

@Composable
private fun <T> HistoryDropdownFilter(
    label: String,
    options: List<Pair<T?, String>>,
    onSelected: (T?) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Card(
            onClick = { expanded = true },
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (enabled) Color.White else WireframeNavBackground
            ),
            border = BorderStroke(1.dp, WireframeBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else CaisseMuted
                )
                Text(
                    text = "v",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (enabled) CaisseBlueDark else CaisseMuted
                )
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { (value, text) ->
                DropdownMenuItem(
                    text = { Text(text) },
                    onClick = {
                        onSelected(value)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun FilterChipButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(999.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) CaisseBlue else Color.White
        ),
        border = BorderStroke(1.dp, if (selected) CaisseBlueDark else WireframeBorder)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun HistoryTableHeader() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
        color = WireframeNavBackground,
        border = BorderStroke(1.dp, WireframeBorder)
    ) {
        HistoryTableRowCells(
            dateTime = "Date / heure",
            ticket = "Ticket",
            orderType = "Type ticket",
            payment = "Paiement",
            total = "Total",
            detail = "Detail",
            isHeader = true,
            onOpenTicket = null
        )
    }
}

@Composable
private fun HistoryTableRow(
    record: HistoryRecord,
    onOpenTicket: () -> Unit
) {
    HistoryTableRowCells(
        dateTime = record.dateTimeLabel,
        ticket = record.ticketLabel,
        orderType = record.orderType.label,
        payment = if (record.isCancelled) "Annulé" else record.paymentBreakdownLabel,
        total = if (record.isCancelled) "Annulé" else formatEurosAmount(record.effectiveTotalCentimes),
        detail = "Ouvrir ticket",
        isHeader = false,
        isCancelled = record.isCancelled,
        onOpenTicket = onOpenTicket
    )
}

@Composable
private fun HistoryTableRowCells(
    dateTime: String,
    ticket: String,
    orderType: String,
    payment: String,
    total: String,
    detail: String,
    isHeader: Boolean,
    isCancelled: Boolean = false,
    onOpenTicket: (() -> Unit)?
) {
    val style = if (isHeader) {
        MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
    } else {
        MaterialTheme.typography.bodyMedium
    }
    val rowColor = if (!isHeader && isCancelled) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = dateTime, modifier = Modifier.weight(1.1f), style = style, color = rowColor)
        Text(text = ticket, modifier = Modifier.weight(0.7f), style = style, fontWeight = FontWeight.Bold, color = rowColor)
        Text(text = orderType, modifier = Modifier.weight(1f), style = style, color = rowColor)
        Text(text = payment, modifier = Modifier.weight(1.1f), style = style, color = rowColor)
        Text(
            text = total,
            modifier = Modifier.weight(0.7f),
            style = style,
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.SemiBold,
            color = when {
                isHeader -> MaterialTheme.colorScheme.onSurface
                isCancelled -> MaterialTheme.colorScheme.error
                else -> WireframeGreenText
            },
            textAlign = TextAlign.End
        )
        Box(modifier = Modifier.weight(0.9f), contentAlignment = Alignment.CenterEnd) {
            if (onOpenTicket != null) {
                TextButton(onClick = onOpenTicket) {
                    Text(
                        text = detail,
                        fontWeight = FontWeight.SemiBold,
                        color = CaisseBlueDark
                    )
                }
            } else {
                Text(
                    text = detail,
                    style = style,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun HistoryTicketDetailScreen(
    record: HistoryRecord,
    onBack: () -> Unit,
    onPrintTicket: (() -> Unit)? = null,
    onSavePayments: (List<PaymentRecord>) -> Unit,
    isLocked: Boolean = false,
    modifier: Modifier = Modifier
) {
    var editedPayments by remember(record.id, record.payments) {
        mutableStateOf(record.payments)
    }
    var paymentCommand by remember(record.id) { mutableStateOf("") }

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
                        text = "Ticket ${record.ticketLabel}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${record.dateTimeLabel} · ${record.orderType.label}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CaisseMuted
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    onPrintTicket?.let { print ->
                        TextButton(onClick = print) {
                            Text("Imprimer ticket")
                        }
                    }
                    TextButton(onClick = onBack) {
                        Text("Retour historique")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, WireframeBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = record.itemsLabel,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Total : ${formatEuros(record.effectiveTotalCentimes)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = WireframeGreenText
                    )
                    Text(
                        text = if (record.isCancelled) {
                            "Paiement : Annulé"
                        } else {
                            "Paiement : ${record.paymentBreakdownLabel}"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    record.deliveryInfo?.let { info ->
                        val phonePart = info.phoneNumber.takeIf { it.isNotBlank() }
                            ?.let { " - Tel. $it" }
                            ?: ""
                        Text(
                            text = "${info.customerName} - ${info.address}$phonePart - ${info.driverLabel}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CaisseMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = if (isLocked) {
                    "Ticket verrouille par une cloture Z"
                } else {
                    "Modifier le moyen de paiement"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (isLocked) {
                Text(
                    text = "La journee de ce ticket est deja cloturee. Le ticket reste consultable, mais le paiement n'est plus modifiable.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CaisseMuted
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            OutlinedTextField(
                value = paymentCommand,
                onValueChange = { paymentCommand = it },
                enabled = !isLocked,
                label = { Text("Commande paiement") },
                supportingText = { Text("Ecrire annuler puis enregistrer pour annuler ce ticket.") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(0.5f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (editedPayments.size == 1) {
                PaymentMethodSelector(
                    selectedMethod = editedPayments.first().method,
                    enabled = !isLocked,
                    onMethodSelected = { method ->
                        val currentPayment = editedPayments.first()
                        editedPayments = listOf(
                            currentPayment.copy(
                                method = method,
                                amountCentimes = if (
                                    currentPayment.method == PaymentMethod.ANNULE &&
                                    currentPayment.amountCentimes == 0
                                ) {
                                    record.totalCentimes
                                } else {
                                    currentPayment.amountCentimes
                                }
                            )
                        )
                    }
                )
            } else {
                editedPayments.forEachIndexed { index, payment ->
                    PaymentLineEditor(
                        payment = payment,
                        enabled = !isLocked,
                        onMethodChange = { method ->
                            editedPayments = editedPayments.toMutableList().also {
                                it[index] = payment.copy(method = method)
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onBack) {
                    Text("Annuler")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Card(
                    onClick = {
                        val command = paymentCommand.trim()
                        if (command.equals("annuler", ignoreCase = true)) {
                            onSavePayments(listOf(PaymentRecord(PaymentMethod.ANNULE, 0)))
                        } else {
                            onSavePayments(editedPayments)
                        }
                    },
                    enabled = !isLocked,
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CaisseBlue),
                    border = BorderStroke(1.dp, CaisseBlueDark)
                ) {
                    Text(
                        text = "Enregistrer",
                        modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun PaymentMethodSelector(
    selectedMethod: PaymentMethod,
    onMethodSelected: (PaymentMethod) -> Unit,
    enabled: Boolean = true
) {
    HistoryDropdownFilter(
        label = paymentMethodShortLabel(selectedMethod),
        options = PaymentMethod.entries
            .filterNot { it == PaymentMethod.ANNULE }
            .map { it to paymentMethodShortLabel(it) },
        onSelected = { method -> method?.let(onMethodSelected) },
        modifier = Modifier.fillMaxWidth(0.5f),
        enabled = enabled
    )
}

@Composable
private fun PaymentLineEditor(
    payment: PaymentRecord,
    onMethodChange: (PaymentMethod) -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = formatEuros(payment.amountCentimes),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        PaymentMethodSelector(
            selectedMethod = payment.method,
            onMethodSelected = onMethodChange,
            enabled = enabled
        )
    }
}

@Preview(showBackground = true, widthDp = 1280, heightDp = 800)
@Composable
private fun HistoryScreenPreview() {
    AMHCaisseTheme {
        HistoryScreen(
            records = SampleHistoryRecords.records,
            onBack = {},
            onOpenTicket = {}
        )
    }
}
