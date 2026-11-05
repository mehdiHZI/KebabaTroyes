package com.amhsolution.caisse.ui.payment

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amhsolution.caisse.domain.formatEuros
import com.amhsolution.caisse.domain.model.OrderType
import com.amhsolution.caisse.domain.model.PaymentMethod
import com.amhsolution.caisse.domain.model.PaymentRecord
import com.amhsolution.caisse.domain.payment.PaymentArticleLine
import com.amhsolution.caisse.domain.payment.appendKeypadDigit
import com.amhsolution.caisse.domain.payment.buildPaymentRecord
import com.amhsolution.caisse.domain.payment.cashChangeCentimes
import com.amhsolution.caisse.domain.payment.clearKeypadInput
import com.amhsolution.caisse.domain.payment.formatPaymentsSummary
import com.amhsolution.caisse.domain.payment.remainingCentimes
import com.amhsolution.caisse.domain.payment.totalPaidCentimes
import com.amhsolution.caisse.ui.components.KebabaPanel
import com.amhsolution.caisse.ui.components.KebabaPrimaryButton
import com.amhsolution.caisse.ui.components.KebabaScreenHeader
import com.amhsolution.caisse.ui.components.KebabaSecondaryButton
import com.amhsolution.caisse.ui.components.KebabaSelectableChip
import com.amhsolution.caisse.ui.theme.AMHCaisseTheme
import com.amhsolution.caisse.ui.theme.CaisseMuted
import com.amhsolution.caisse.ui.theme.KebabaBordeaux
import com.amhsolution.caisse.ui.theme.KebabaCard
import com.amhsolution.caisse.ui.theme.KebabaCreamDeep
import com.amhsolution.caisse.ui.theme.KebabaDivider
import com.amhsolution.caisse.ui.theme.KebabaInk
import com.amhsolution.caisse.ui.theme.KebabaPendingBg
import com.amhsolution.caisse.ui.theme.KebabaSoftSelected

@Composable
fun PaymentScreen(
    orderType: OrderType,
    totalCentimes: Int,
    articles: List<PaymentArticleLine>,
    onBack: () -> Unit,
    onComplete: (List<PaymentRecord>) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedMethod by remember { mutableStateOf(PaymentMethod.ESPECES) }
    var payments by remember { mutableStateOf<List<PaymentRecord>>(emptyList()) }
    var inputCentimes by remember(totalCentimes) { mutableIntStateOf(totalCentimes) }

    val paidCentimes = totalPaidCentimes(payments)
    val remaining = remainingCentimes(totalCentimes, payments)
    val changeCentimes = if (selectedMethod == PaymentMethod.ESPECES) {
        cashChangeCentimes(inputCentimes, remaining)
    } else {
        null
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            KebabaScreenHeader(
                title = "Encaissement",
                subtitle = "Paiement mixte · pavé numérique pour les montants",
                onBack = onBack,
                backLabel = "Retour caisse"
            )

            Spacer(modifier = Modifier.height(12.dp))

            PaymentStatusBar(
                orderType = orderType,
                totalCentimes = totalCentimes,
                paidCentimes = paidCentimes,
                remainingCentimes = remaining
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ArticlesPanel(
                    articles = articles,
                    modifier = Modifier
                        .width(260.dp)
                        .fillMaxHeight()
                )

                PaymentMethodPanel(
                    selectedMethod = selectedMethod,
                    onMethodSelected = {
                        selectedMethod = it
                        inputCentimes = remaining
                    },
                    modifier = Modifier
                        .width(180.dp)
                        .fillMaxHeight()
                )

                AmountPanel(
                    inputCentimes = inputCentimes,
                    selectedMethod = selectedMethod,
                    changeCentimes = changeCentimes,
                    remainingCentimes = remaining,
                    canAddPayment = inputCentimes > 0 && remaining > 0,
                    canValidate = remaining == 0 && payments.isNotEmpty(),
                    onAddPayment = {
                        val record = buildPaymentRecord(selectedMethod, inputCentimes, remaining)
                        if (record != null) {
                            val updatedPayments = payments + record
                            payments = updatedPayments
                            val newRemaining = remainingCentimes(totalCentimes, updatedPayments)
                            inputCentimes = newRemaining
                        }
                    },
                    onValidate = { onComplete(payments) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )

                KeypadPanel(
                    payments = payments,
                    onDigit = { digit ->
                        inputCentimes = appendKeypadDigit(inputCentimes, digit)
                    },
                    onClear = { inputCentimes = clearKeypadInput() },
                    onComma = { },
                    modifier = Modifier
                        .width(260.dp)
                        .fillMaxHeight()
                )
            }
        }
    }
}

@Composable
private fun PaymentStatusBar(
    orderType: OrderType,
    totalCentimes: Int,
    paidCentimes: Int,
    remainingCentimes: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = KebabaSoftSelected),
        border = BorderStroke(1.dp, KebabaBordeaux.copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusItem("Ticket", orderType.label)
            StatusItem("Total", formatEuros(totalCentimes))
            StatusItem("Déjà payé", formatEuros(paidCentimes))
            StatusItem("Reste", formatEuros(remainingCentimes), bold = true)
        }
    }
}

@Composable
private fun StatusItem(label: String, value: String, bold: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = CaisseMuted
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.SemiBold,
            color = KebabaInk
        )
    }
}

@Composable
private fun ArticlesPanel(
    articles: List<PaymentArticleLine>,
    modifier: Modifier = Modifier
) {
    KebabaPanel(title = "Articles", modifier = modifier) {
        if (articles.isEmpty()) {
            Text(
                text = "Aucun article",
                style = MaterialTheme.typography.bodyMedium,
                color = CaisseMuted
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(articles) { article ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = article.label,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = KebabaInk,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = formatEuros(article.totalCentimes),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = KebabaBordeaux
                        )
                    }
                    HorizontalDivider(color = KebabaDivider.copy(alpha = 0.55f))
                }
            }
        }
    }
}

@Composable
private fun PaymentMethodPanel(
    selectedMethod: PaymentMethod,
    onMethodSelected: (PaymentMethod) -> Unit,
    modifier: Modifier = Modifier
) {
    KebabaPanel(title = "Moyen de paiement", modifier = modifier) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            PaymentMethod.entries
                .filterNot { it == PaymentMethod.ANNULE || it == PaymentMethod.AVOIR }
                .forEach { method ->
                    KebabaSelectableChip(
                        label = method.label,
                        selected = method == selectedMethod,
                        onClick = { onMethodSelected(method) },
                        modifier = Modifier.fillMaxWidth(),
                        height = 56.dp
                    )
                }
        }
    }
}

@Composable
private fun AmountPanel(
    inputCentimes: Int,
    selectedMethod: PaymentMethod,
    changeCentimes: Int?,
    remainingCentimes: Int,
    canAddPayment: Boolean,
    canValidate: Boolean,
    onAddPayment: () -> Unit,
    onValidate: () -> Unit,
    modifier: Modifier = Modifier
) {
    KebabaPanel(title = "Montant encaissé", modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = KebabaCreamDeep),
                border = BorderStroke(1.dp, KebabaDivider.copy(alpha = 0.7f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = formatEuros(inputCentimes),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = KebabaInk
                    )
                }
            }

            Text(
                text = when (selectedMethod) {
                    PaymentMethod.ESPECES ->
                        "Exemple espèces : commande ${formatEuros(remainingCentimes)} et le client donne un montant plus élevé."
                    PaymentMethod.CARTE_BANCAIRE ->
                        "Confirmer le paiement sur le TPE après saisie du montant."
                    else -> "Saisir le montant reçu pour ce moyen de paiement."
                },
                style = MaterialTheme.typography.bodySmall,
                color = CaisseMuted
            )

            if (selectedMethod == PaymentMethod.ESPECES && changeCentimes != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = KebabaPendingBg),
                    border = BorderStroke(1.dp, KebabaDivider.copy(alpha = 0.5f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Text(
                        text = "Rendu monnaie : ${formatEuros(changeCentimes)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = KebabaInk,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KebabaSecondaryButton(
                    label = "Ajouter paiement",
                    enabled = canAddPayment,
                    onClick = onAddPayment,
                    modifier = Modifier.weight(1f),
                    height = 52.dp
                )
                KebabaPrimaryButton(
                    label = "Valider ticket",
                    enabled = canValidate,
                    onClick = onValidate,
                    modifier = Modifier.weight(1f),
                    height = 52.dp
                )
            }
        }
    }
}

@Composable
private fun KeypadPanel(
    payments: List<PaymentRecord>,
    onDigit: (Int) -> Unit,
    onClear: () -> Unit,
    onComma: () -> Unit,
    modifier: Modifier = Modifier
) {
    KebabaPanel(title = "Pavé numérique", modifier = modifier) {
        Column(modifier = Modifier.fillMaxSize()) {
            val keys = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("C", "0", ",")
            )

            keys.forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    row.forEach { key ->
                        KeypadButton(
                            label = key,
                            onClick = {
                                when (key) {
                                    "C" -> onClear()
                                    "," -> onComma()
                                    else -> onDigit(key.toInt())
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = KebabaSoftSelected),
                border = BorderStroke(1.dp, KebabaBordeaux.copy(alpha = 0.2f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = formatPaymentsSummary(payments),
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = KebabaInk,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun KeypadButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(64.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = KebabaCard),
        border = BorderStroke(1.dp, KebabaDivider.copy(alpha = 0.75f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = KebabaInk
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 1280, heightDp = 800)
@Composable
private fun PaymentScreenPreview() {
    AMHCaisseTheme {
        PaymentScreen(
            orderType = OrderType.A_EMPORTER,
            totalCentimes = 4200,
            articles = listOf(
                PaymentArticleLine("1x Margherita", 1100),
                PaymentArticleLine("1x Kebaba Klassik", 800),
                PaymentArticleLine("2x Pepsi", 300),
                PaymentArticleLine("1x Smoothie Banane", 550)
            ),
            onBack = {},
            onComplete = { _ -> }
        )
    }
}
