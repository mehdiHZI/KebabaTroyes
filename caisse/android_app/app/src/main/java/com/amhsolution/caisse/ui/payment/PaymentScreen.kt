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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.amhsolution.caisse.ui.theme.AMHCaisseTheme
import com.amhsolution.caisse.ui.theme.CaisseBlue
import com.amhsolution.caisse.ui.theme.CaisseBlueDark
import com.amhsolution.caisse.ui.theme.CaisseMuted
import com.amhsolution.caisse.ui.theme.WireframeBorder
import com.amhsolution.caisse.ui.theme.WireframeGreenBackground
import com.amhsolution.caisse.ui.theme.WireframeGreenBorder
import com.amhsolution.caisse.ui.theme.WireframeGreenText
import com.amhsolution.caisse.ui.theme.WireframeNavBackground
import com.amhsolution.caisse.ui.theme.WireframeYellowBackground

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
            PaymentHeader(onBack = onBack)

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
                    onComma = {
                        // La virgule est gérée visuellement via le format EUR ; on garde le bouton du wireframe.
                    },
                    modifier = Modifier
                        .width(260.dp)
                        .fillMaxHeight()
                )
            }
        }
    }
}

@Composable
private fun PaymentHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "03 - Encaissement mixte",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Articles avec prix pour paiements séparés · pavé numérique pour les montants",
                style = MaterialTheme.typography.bodyMedium,
                color = CaisseMuted
            )
        }

        TextButton(onClick = onBack) {
            Text("Retour caisse")
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
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = WireframeGreenBackground),
        border = BorderStroke(1.dp, WireframeGreenBorder),
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
            color = WireframeGreenText
        )
    }
}

@Composable
private fun ArticlesPanel(
    articles: List<PaymentArticleLine>,
    modifier: Modifier = Modifier
) {
    PaymentPanel(title = "Articles", modifier = modifier) {
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
                            color = WireframeGreenText
                        )
                    }
                    HorizontalDivider(color = WireframeBorder.copy(alpha = 0.25f))
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
    PaymentPanel(title = "Moyen de paiement", modifier = modifier) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            PaymentMethod.entries
                .filterNot { it == PaymentMethod.ANNULE }
                .forEach { method ->
                    val selected = method == selectedMethod
                    Card(
                        onClick = { onMethodSelected(method) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selected) WireframeYellowBackground else Color.White
                        ),
                        border = BorderStroke(
                            width = if (selected) 2.dp else 1.dp,
                            color = if (selected) WireframeBorder else WireframeBorder.copy(alpha = 0.5f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = method.label,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
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
    PaymentPanel(title = "Montant encaissé", modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, WireframeBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = formatEuros(inputCentimes),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
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
                    colors = CardDefaults.cardColors(containerColor = WireframeYellowBackground),
                    border = BorderStroke(1.dp, WireframeBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Text(
                        text = "Rendu monnaie : ${formatEuros(changeCentimes)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
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
                PaymentActionButton(
                    label = "Ajouter paiement",
                    containerColor = WireframeGreenBackground,
                    contentColor = WireframeGreenText,
                    borderColor = WireframeGreenBorder,
                    enabled = canAddPayment,
                    onClick = onAddPayment,
                    modifier = Modifier.weight(1f)
                )
                PaymentActionButton(
                    label = "Valider ticket",
                    containerColor = CaisseBlue,
                    contentColor = Color.White,
                    borderColor = CaisseBlueDark,
                    enabled = canValidate,
                    onClick = onValidate,
                    modifier = Modifier.weight(1f)
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
    PaymentPanel(title = "Pavé numérique", modifier = modifier) {
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
                colors = CardDefaults.cardColors(containerColor = WireframeGreenBackground),
                border = BorderStroke(1.dp, WireframeGreenBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = formatPaymentsSummary(payments),
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = WireframeGreenText,
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
        colors = CardDefaults.cardColors(containerColor = WireframeNavBackground),
        border = BorderStroke(1.dp, WireframeBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun PaymentPanel(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, WireframeBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}

@Composable
private fun PaymentActionButton(
    label: String,
    containerColor: Color,
    contentColor: Color,
    borderColor: Color,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            disabledContainerColor = containerColor.copy(alpha = 0.45f)
        ),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                textAlign = TextAlign.Center
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
