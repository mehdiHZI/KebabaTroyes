package com.amhsolution.caisse.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.amhsolution.caisse.data.sync.CaisseApiClient
import com.amhsolution.caisse.data.sync.CaisseApiResult
import com.amhsolution.caisse.data.sync.ServerConfig
import com.amhsolution.caisse.ui.theme.CaisseMuted
import com.amhsolution.caisse.ui.theme.WireframeBorder
import com.amhsolution.caisse.ui.theme.WireframeGreenText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ServerSettingsScreen(
    initialConfig: ServerConfig,
    onSave: suspend (baseUrl: String, apiToken: String, registerId: String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var baseUrl by remember(initialConfig) { mutableStateOf(initialConfig.baseUrl) }
    var apiToken by remember(initialConfig) { mutableStateOf(initialConfig.apiToken) }
    var registerId by remember(initialConfig) {
        mutableStateOf(initialConfig.registerId.ifBlank { "caisse-1" })
    }
    var showToken by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var statusOk by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val apiClient = remember { CaisseApiClient() }

    fun currentConfig() = ServerConfig(
        baseUrl = baseUrl.trim().trimEnd('/'),
        apiToken = apiToken.trim(),
        registerId = registerId.trim().ifBlank { "caisse-1" }
    )

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
                        text = "Serveur / sync",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "URL API VPS + token Bearer (même valeur que sur le serveur)",
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = baseUrl,
                    onValueChange = {
                        baseUrl = it
                        statusMessage = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("URL API") },
                    placeholder = { Text("https://caisse-api.kebaba.fr") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    supportingText = {
                        Text("Sans slash final. Ex. https://caisse-api.kebaba.fr")
                    }
                )

                OutlinedTextField(
                    value = apiToken,
                    onValueChange = {
                        apiToken = it
                        statusMessage = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Token API") },
                    visualTransformation = if (showToken) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        TextButton(onClick = { showToken = !showToken }) {
                            Text(if (showToken) "Masquer" else "Voir")
                        }
                    },
                    supportingText = {
                        Text("Identique à CAISSE_API_TOKEN sur le VPS")
                    }
                )

                OutlinedTextField(
                    value = registerId,
                    onValueChange = {
                        registerId = it
                        statusMessage = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("ID caisse") },
                    placeholder = { Text("caisse-1") },
                    supportingText = {
                        Text("Identifiant de cette tablette côté serveur")
                    }
                )

                statusMessage?.let { message ->
                    Text(
                        text = message,
                        color = if (statusOk) {
                            WireframeGreenText
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End)
            ) {
                TextButton(
                    enabled = !busy && baseUrl.isNotBlank(),
                    onClick = {
                        scope.launch {
                            busy = true
                            statusMessage = null
                            val result = withContext(Dispatchers.IO) {
                                apiClient.testConnection(currentConfig())
                            }
                            statusOk = result is CaisseApiResult.Success
                            statusMessage = when (result) {
                                CaisseApiResult.Success ->
                                    "OK : réseau + token acceptés (catalogue)."
                                is CaisseApiResult.Failure -> "Test échoué : ${result.message}"
                            }
                            busy = false
                        }
                    }
                ) {
                    Text("Tester")
                }
                Button(
                    enabled = !busy,
                    onClick = {
                        scope.launch {
                            busy = true
                            statusMessage = null
                            val cfg = currentConfig()
                            if (!cfg.isConfigured) {
                                statusOk = false
                                statusMessage = "URL et token obligatoires."
                                busy = false
                                return@launch
                            }
                            onSave(cfg.baseUrl, cfg.apiToken, cfg.registerId)
                            val verify = withContext(Dispatchers.IO) {
                                apiClient.testConnection(cfg)
                            }
                            statusOk = verify is CaisseApiResult.Success
                            statusMessage = when (verify) {
                                CaisseApiResult.Success ->
                                    "Enregistré et authentifié (token ${cfg.apiToken.trim().length} car.)."
                                is CaisseApiResult.Failure ->
                                    "Enregistré mais auth KO : ${verify.message}"
                            }
                            busy = false
                        }
                    }
                ) {
                    Text("Enregistrer")
                }
            }
        }
    }
}
