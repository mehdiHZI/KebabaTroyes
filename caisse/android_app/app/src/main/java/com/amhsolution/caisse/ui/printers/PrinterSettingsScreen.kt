package com.amhsolution.caisse.ui.printers

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.amhsolution.caisse.data.printing.BluetoothPrinterTarget
import com.amhsolution.caisse.data.printing.ConfiguredPrinter
import com.amhsolution.caisse.data.printing.PrinterLinkType
import com.amhsolution.caisse.data.printing.PrinterRoutingSettings
import com.amhsolution.caisse.data.printing.TicketOutputTarget
import com.amhsolution.caisse.data.printing.displayName
import com.amhsolution.caisse.ui.theme.CaisseBlue
import com.amhsolution.caisse.ui.theme.CaisseMuted
import com.amhsolution.caisse.ui.theme.WireframeBorder
import com.amhsolution.caisse.ui.theme.WireframeNavBackground

@Composable
fun PrinterSettingsScreen(
    settings: PrinterRoutingSettings,
    pairedPrinters: List<BluetoothPrinterTarget>,
    discoveryMessage: String?,
    onRefresh: () -> Unit,
    onSave: (PrinterRoutingSettings) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var editedSettings by remember(settings) { mutableStateOf(settings) }
    var savedMessageVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        onRefresh()
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
                        text = "Imprimantes",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Bluetooth ou Wi-Fi (TCP port 9100) pour chaque ticket",
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

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                SettingsSection(
                    title = "Impression automatique",
                    subtitle = "Choisissez les tickets sortis à la création. Le bon cuisine n'est jamais réimprimé automatiquement lors d'une reprise ou d'un encaissement ultérieur."
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TicketOutputTarget.entries.forEach { target ->
                            SelectionCard(
                                label = target.label,
                                selected = editedSettings.automaticOutput == target,
                                onClick = {
                                    editedSettings = editedSettings.copy(automaticOutput = target)
                                    savedMessageVisible = false
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                PrinterEndpointEditor(
                    title = "Imprimante caisse",
                    subtitle = "Tickets clients, tickets Z et commande d'ouverture du tiroir RJ12.",
                    printers = pairedPrinters,
                    value = editedSettings.customerPrinter,
                    onValueChange = { printer ->
                        editedSettings = editedSettings.copy(customerPrinter = printer)
                        savedMessageVisible = false
                    }
                )

                PrinterEndpointEditor(
                    title = "Imprimante cuisine",
                    subtitle = "Bons de préparation sans prix ni informations de paiement.",
                    printers = pairedPrinters,
                    value = editedSettings.kitchenPrinter,
                    onValueChange = { printer ->
                        editedSettings = editedSettings.copy(kitchenPrinter = printer)
                        savedMessageVisible = false
                    }
                )

                discoveryMessage?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = CaisseMuted
                    )
                }

                TextButton(onClick = onRefresh) {
                    Text("Actualiser les appareils Bluetooth associés")
                }
            }

            HorizontalDivider(color = WireframeBorder.copy(alpha = 0.25f))
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (savedMessageVisible) {
                    Text(
                        text = "Réglages enregistrés",
                        color = CaisseMuted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
                Button(
                    onClick = {
                        onSave(editedSettings)
                        savedMessageVisible = true
                    }
                ) {
                    Text("Enregistrer")
                }
            }
        }
    }
}

@Composable
private fun PrinterEndpointEditor(
    title: String,
    subtitle: String,
    printers: List<BluetoothPrinterTarget>,
    value: ConfiguredPrinter,
    onValueChange: (ConfiguredPrinter) -> Unit
) {
    SettingsSection(title = title, subtitle = subtitle) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PrinterLinkType.entries.forEach { linkType ->
                SelectionCard(
                    label = linkType.label,
                    selected = value.linkType == linkType,
                    onClick = { onValueChange(value.copy(linkType = linkType)) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        when (value.linkType) {
            PrinterLinkType.BLUETOOTH -> {
                if (printers.isEmpty()) {
                    Text(
                        text = "Aucune imprimante Bluetooth associée détectée.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CaisseMuted
                    )
                } else {
                    printers.forEach { printer ->
                        val isSelected = printer.deviceAddress.equals(
                            value.bluetooth.deviceAddress,
                            ignoreCase = true
                        )
                        Card(
                            onClick = {
                                onValueChange(value.copy(bluetooth = printer))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) {
                                    CaisseBlue.copy(alpha = 0.10f)
                                } else {
                                    WireframeNavBackground
                                }
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) CaisseBlue else WireframeBorder
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        onValueChange(value.copy(bluetooth = printer))
                                    }
                                )
                                Column {
                                    Text(
                                        text = printer.displayName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    printer.deviceAddress?.let { address ->
                                        Text(
                                            text = address,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = CaisseMuted
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            PrinterLinkType.WIFI -> {
                Text(
                    text = "L'imprimante doit être sur le même Wi-Fi que la tablette. Port raw ESC/POS : 9100 en général.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CaisseMuted
                )
                OutlinedTextField(
                    value = value.host.orEmpty(),
                    onValueChange = { onValueChange(value.copy(host = it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Adresse IP") },
                    placeholder = { Text("ex. 192.168.1.50") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                )
                OutlinedTextField(
                    value = value.port.toString(),
                    onValueChange = { raw ->
                        val digits = raw.filter { it.isDigit() }.take(5)
                        val port = digits.toIntOrNull() ?: ConfiguredPrinter.DefaultNetworkPort
                        onValueChange(value.copy(port = port))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Port") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, WireframeBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = CaisseMuted
            )
            content()
        }
    }
}

@Composable
private fun SelectionCard(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) CaisseBlue else WireframeNavBackground
        ),
        border = BorderStroke(1.dp, if (selected) CaisseBlue else WireframeBorder)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontWeight = FontWeight.SemiBold,
                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
