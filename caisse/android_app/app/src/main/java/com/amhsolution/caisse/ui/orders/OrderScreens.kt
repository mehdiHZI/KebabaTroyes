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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.amhsolution.caisse.domain.catalog.SampleOrderHistory
import com.amhsolution.caisse.domain.formatEuros
import com.amhsolution.caisse.domain.history.formatHistoryDateTime
import com.amhsolution.caisse.domain.orders.groupedByDriver
import com.amhsolution.caisse.domain.model.DeliveryInfo
import com.amhsolution.caisse.domain.model.OrderTicket
import com.amhsolution.caisse.domain.model.OrderType
import com.amhsolution.caisse.ui.theme.AMHCaisseTheme
import com.amhsolution.caisse.ui.theme.CaisseBlue
import com.amhsolution.caisse.ui.theme.CaisseBlueDark
import com.amhsolution.caisse.ui.theme.CaisseMuted
import com.amhsolution.caisse.ui.theme.WireframeBorder
import com.amhsolution.caisse.ui.theme.WireframeGreenText
import com.amhsolution.caisse.ui.theme.WireframeNavBackground
import com.amhsolution.caisse.ui.theme.WireframeRedText
import com.amhsolution.caisse.ui.theme.WireframeYellowBackground

@Composable
fun PendingOrdersScreen(
    title: String,
    subtitle: String,
    orders: List<OrderTicket>,
    emptyMessage: String,
    onBack: () -> Unit,
    onPayOrder: (OrderTicket) -> Unit,
    onPrintOrder: (OrderTicket) -> Unit,
    onCancelOrder: (OrderTicket) -> Unit,
    groupByDriver: Boolean = false,
    modifier: Modifier = Modifier
) {
    var selectedDriverNumber by remember { mutableStateOf<Int?>(null) }
    val driverNumbers = remember(orders, groupByDriver) {
        if (groupByDriver) {
            orders.mapNotNull { it.deliveryInfo?.driverNumber }.distinct().sorted()
        } else {
            emptyList()
        }
    }
    val effectiveDriverNumber = selectedDriverNumber?.takeIf { it in driverNumbers }
    val visibleOrders = remember(orders, effectiveDriverNumber, groupByDriver) {
        if (!groupByDriver || effectiveDriverNumber == null) {
            orders
        } else {
            orders.filter { it.deliveryInfo?.driverNumber == effectiveDriverNumber }
        }
    }

    OrderListScaffold(
        title = title,
        subtitle = subtitle,
        onBack = onBack,
        modifier = modifier
    ) {
        if (orders.isEmpty()) {
            EmptyState(message = emptyMessage)
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                if (groupByDriver) {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            DriverFilterChip(
                                label = "Tous les livreurs",
                                selected = effectiveDriverNumber == null,
                                onClick = { selectedDriverNumber = null }
                            )
                        }
                        items(driverNumbers, key = { it }) { driverNumber ->
                            DriverFilterChip(
                                label = "Livreur $driverNumber",
                                selected = effectiveDriverNumber == driverNumber,
                                onClick = { selectedDriverNumber = driverNumber }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (groupByDriver) {
                        visibleOrders.groupedByDriver().forEach { group ->
                            item(key = "driver-header-${group.driverNumber}") {
                                DriverSectionHeader(
                                    driverNumber = group.driverNumber,
                                    orderCount = group.orders.size
                                )
                            }
                            items(group.orders, key = { it.id }) { order ->
                                OrderTicketCard(
                                    order = order,
                                    onPayOrder = { onPayOrder(order) },
                                    onPrintOrder = { onPrintOrder(order) },
                                    onCancelOrder = { onCancelOrder(order) }
                                )
                            }
                        }
                    } else {
                        items(visibleOrders, key = { it.id }) { order ->
                            OrderTicketCard(
                                order = order,
                                onPayOrder = { onPayOrder(order) },
                                onPrintOrder = { onPrintOrder(order) },
                                onCancelOrder = { onCancelOrder(order) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderListScaffold(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
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
                        text = title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = subtitle,
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

            content()
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, WireframeBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
                color = CaisseMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun OrderTicketCard(
    order: OrderTicket,
    onPayOrder: () -> Unit,
    onPrintOrder: () -> Unit,
    onCancelOrder: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, WireframeBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OrderNumberBlock(order = order)

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = order.orderType.label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    StatusBadge(label = order.statusLabel)
                }

                Text(
                    text = order.itemsLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = CaisseMuted,
                    maxLines = 2
                )

                if (order.orderType == OrderType.LIVRAISON) {
                    order.deliveryInfo?.let { info ->
                        DeliveryInfoLine(info = info)
                    } ?: Text(
                        text = "Informations livraison à compléter",
                        style = MaterialTheme.typography.bodySmall,
                        color = WireframeRedText,
                        fontWeight = FontWeight.SemiBold
                    )
                }

            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = formatEuros(order.totalCentimes),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = WireframeGreenText
                )
                OrderActionButton(
                    label = "Imprimer",
                    onClick = onPrintOrder
                )
                OrderActionButton(
                    label = "Annuler",
                    onClick = onCancelOrder,
                    containerColor = Color.White,
                    borderColor = WireframeRedText,
                    contentColor = WireframeRedText
                )
                OrderActionButton(
                    label = "Encaisser",
                    onClick = onPayOrder
                )
            }
        }
    }
}

@Composable
private fun DriverFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) CaisseBlue else Color.White
        ),
        border = BorderStroke(1.dp, if (selected) CaisseBlueDark else WireframeBorder)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun DriverSectionHeader(driverNumber: Int?, orderCount: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = WireframeNavBackground,
        border = BorderStroke(1.dp, WireframeBorder)
    ) {
        Text(
            text = if (driverNumber == null) {
                "Livreur non attribué · $orderCount commande(s)"
            } else {
                "Livreur $driverNumber · $orderCount commande(s)"
            },
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun DeliveryInfoLine(info: DeliveryInfo) {
    val phonePart = info.phoneNumber.takeIf { it.isNotBlank() }
        ?.let { " - Tel. $it" }
        ?: ""
    Text(
        text = "${info.customerName} - ${info.address}$phonePart - ${info.driverLabel}",
        style = MaterialTheme.typography.bodySmall,
        color = CaisseBlueDark,
        fontWeight = FontWeight.SemiBold,
        maxLines = 2
    )
}

@Composable
private fun OrderNumberBlock(order: OrderTicket) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = WireframeNavBackground,
        border = BorderStroke(1.dp, WireframeBorder)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "#${order.ticketNumber}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = formatHistoryDateTime(order.createdAtEpochMillis),
                style = MaterialTheme.typography.labelMedium,
                color = CaisseMuted
            )
        }
    }
}

@Composable
private fun StatusBadge(label: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = WireframeYellowBackground,
        border = BorderStroke(1.dp, WireframeBorder)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun OrderActionButton(
    label: String,
    onClick: () -> Unit,
    containerColor: Color = CaisseBlue,
    borderColor: Color = CaisseBlueDark,
    contentColor: Color = Color.White
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
    }
}

@Preview(showBackground = true, widthDp = 1280, heightDp = 800)
@Composable
private fun PendingOrdersScreenPreview() {
    AMHCaisseTheme {
        PendingOrdersScreen(
            title = "A encaisser sur place / a emporter",
            subtitle = "Commandes gardees ouvertes",
            orders = SampleOrderHistory.orders.take(2),
            emptyMessage = "Aucune commande en attente.",
            onBack = {},
            onPayOrder = {},
            onPrintOrder = {},
            onCancelOrder = {}
        )
    }
}
