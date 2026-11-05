package com.amhsolution.caisse.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.amhsolution.caisse.R
import com.amhsolution.caisse.data.sync.SyncStatusSnapshot
import com.amhsolution.caisse.ui.components.KebabaAccentTile
import com.amhsolution.caisse.ui.components.KebabaHeroCta
import com.amhsolution.caisse.ui.components.KebabaNavTile
import com.amhsolution.caisse.ui.components.KebabaSimulationBadge
import com.amhsolution.caisse.ui.components.KebabaSyncBadge
import com.amhsolution.caisse.ui.theme.AMHCaisseTheme
import com.amhsolution.caisse.ui.theme.KebabaCreamDeep
import com.amhsolution.caisse.ui.theme.KebabaInk
import com.amhsolution.caisse.ui.theme.KebabaInkMuted
import com.amhsolution.caisse.ui.theme.KebabaPendingBg
import com.amhsolution.caisse.ui.theme.KebabaSoftSelected

enum class HomeDestination {
    Register,
    OnSiteToCollect,
    DeliveryToCollect,
    History,
    Statistics,
    PriceManagement,
    PrinterSettings,
    ServerSettings,
    ClosureArchive
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    syncStatus: SyncStatusSnapshot? = null,
    simulationEnabled: Boolean = false,
    onToggleSimulation: (() -> Unit)? = null,
    onActionSelected: (HomeDestination) -> Unit = {}
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            if (maxWidth < 700.dp) {
                CompactHomeLayout(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    syncStatus = syncStatus,
                    simulationEnabled = simulationEnabled,
                    onToggleSimulation = onToggleSimulation,
                    onActionSelected = onActionSelected
                )
            } else {
                TabletHomeLayout(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 40.dp, vertical = 28.dp),
                    syncStatus = syncStatus,
                    simulationEnabled = simulationEnabled,
                    onToggleSimulation = onToggleSimulation,
                    onActionSelected = onActionSelected
                )
            }
        }
    }
}

@Composable
private fun TabletHomeLayout(
    modifier: Modifier = Modifier,
    syncStatus: SyncStatusSnapshot?,
    simulationEnabled: Boolean = false,
    onToggleSimulation: (() -> Unit)? = null,
    onActionSelected: (HomeDestination) -> Unit
) {
    Column(modifier = modifier) {
        HomeBrandHeader(
            syncStatus = syncStatus,
            simulationEnabled = simulationEnabled,
            onToggleSimulation = onToggleSimulation
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            KebabaNavTile("Historique", Modifier.weight(1f)) {
                onActionSelected(HomeDestination.History)
            }
            KebabaNavTile("Statistiques", Modifier.weight(1f)) {
                onActionSelected(HomeDestination.Statistics)
            }
            KebabaNavTile("Catalogue", Modifier.weight(1f)) {
                onActionSelected(HomeDestination.PriceManagement)
            }
            KebabaNavTile("Clôture", Modifier.weight(1f)) {
                onActionSelected(HomeDestination.ClosureArchive)
            }
            KebabaNavTile("Imprimantes", Modifier.weight(1f)) {
                onActionSelected(HomeDestination.PrinterSettings)
            }
            KebabaNavTile("Serveur", Modifier.weight(1f)) {
                onActionSelected(HomeDestination.ServerSettings)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KebabaAccentTile(
                label = "À encaisser\nsur place /\nà emporter",
                onClick = { onActionSelected(HomeDestination.OnSiteToCollect) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                containerColor = KebabaSoftSelected,
                contentColor = KebabaInk
            )
            KebabaHeroCta(
                title = "CAISSE",
                subtitle = "Commander / encaisser",
                onClick = { onActionSelected(HomeDestination.Register) },
                modifier = Modifier
                    .weight(2f)
                    .fillMaxHeight()
            )
            KebabaAccentTile(
                label = "À encaisser\nlivraison",
                onClick = { onActionSelected(HomeDestination.DeliveryToCollect) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                containerColor = KebabaPendingBg,
                contentColor = KebabaInk
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        HomeFooter()
    }
}

@Composable
private fun CompactHomeLayout(
    modifier: Modifier = Modifier,
    syncStatus: SyncStatusSnapshot?,
    simulationEnabled: Boolean = false,
    onToggleSimulation: (() -> Unit)? = null,
    onActionSelected: (HomeDestination) -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HomeBrandHeader(
            syncStatus = syncStatus,
            simulationEnabled = simulationEnabled,
            onToggleSimulation = onToggleSimulation
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KebabaNavTile("Historique", Modifier.weight(1f)) {
                onActionSelected(HomeDestination.History)
            }
            KebabaNavTile("Stats", Modifier.weight(1f)) {
                onActionSelected(HomeDestination.Statistics)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KebabaNavTile("Catalogue", Modifier.weight(1f)) {
                onActionSelected(HomeDestination.PriceManagement)
            }
            KebabaNavTile("Clôture", Modifier.weight(1f)) {
                onActionSelected(HomeDestination.ClosureArchive)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KebabaNavTile("Imprimantes", Modifier.weight(1f)) {
                onActionSelected(HomeDestination.PrinterSettings)
            }
            KebabaNavTile("Serveur", Modifier.weight(1f)) {
                onActionSelected(HomeDestination.ServerSettings)
            }
        }

        KebabaHeroCta(
            title = "CAISSE",
            subtitle = "Commander / encaisser",
            onClick = { onActionSelected(HomeDestination.Register) },
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KebabaAccentTile(
                label = "À encaisser\nsur place /\nà emporter",
                onClick = { onActionSelected(HomeDestination.OnSiteToCollect) },
                modifier = Modifier
                    .weight(1f)
                    .height(110.dp)
                    .widthIn(min = 120.dp),
                containerColor = KebabaSoftSelected,
                contentColor = KebabaInk
            )
            KebabaAccentTile(
                label = "À encaisser\nlivraison",
                onClick = { onActionSelected(HomeDestination.DeliveryToCollect) },
                modifier = Modifier
                    .weight(1f)
                    .height(110.dp)
                    .widthIn(min = 120.dp),
                containerColor = KebabaPendingBg,
                contentColor = KebabaInk
            )
        }
        HomeFooter()
    }
}

@Composable
private fun HomeFooter() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterEnd
    ) {
        Text(
            text = "Kebaba Troyes",
            style = MaterialTheme.typography.labelSmall,
            color = KebabaInkMuted
        )
    }
}

@Composable
private fun HomeBrandHeader(
    syncStatus: SyncStatusSnapshot? = null,
    simulationEnabled: Boolean = false,
    onToggleSimulation: (() -> Unit)? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.logo_kebaba),
                    contentDescription = "Logo Kebaba",
                    modifier = Modifier.size(width = 72.dp, height = 52.dp),
                    contentScale = ContentScale.Fit
                )
                Column {
                    Text(
                        text = "Kebaba Caisse",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = KebabaInk
                    )
                    Text(
                        text = "Döner & Co · Troyes",
                        style = MaterialTheme.typography.bodySmall,
                        color = KebabaInkMuted
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (simulationEnabled || onToggleSimulation != null) {
                    KebabaSimulationBadge(
                        simulationEnabled = simulationEnabled,
                        onToggle = onToggleSimulation
                    )
                }
                syncStatus?.let { KebabaSyncBadge(it) }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        androidx.compose.material3.HorizontalDivider(
            color = KebabaCreamDeep
        )
    }
}

@Preview(showBackground = true, widthDp = 1280, heightDp = 800)
@Composable
private fun HomeScreenTabletPreview() {
    AMHCaisseTheme {
        HomeScreen()
    }
}

@Preview(showBackground = true, widthDp = 420, heightDp = 900)
@Composable
private fun HomeScreenCompactPreview() {
    AMHCaisseTheme {
        HomeScreen()
    }
}
