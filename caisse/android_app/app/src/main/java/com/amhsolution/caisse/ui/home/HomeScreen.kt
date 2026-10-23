package com.amhsolution.caisse.ui.home

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amhsolution.caisse.data.sync.SyncStatusSnapshot
import com.amhsolution.caisse.data.sync.SyncUiState
import com.amhsolution.caisse.ui.theme.AMHCaisseTheme
import com.amhsolution.caisse.ui.theme.CaisseRed
import com.amhsolution.caisse.ui.theme.KebabaBordeaux
import com.amhsolution.caisse.ui.theme.WireframeBorder
import com.amhsolution.caisse.ui.theme.WireframeGreenBackground
import com.amhsolution.caisse.ui.theme.WireframeGreenBorder
import com.amhsolution.caisse.ui.theme.WireframeGreenText
import com.amhsolution.caisse.ui.theme.WireframeNavBackground
import com.amhsolution.caisse.ui.theme.WireframeSubtitle
import com.amhsolution.caisse.ui.theme.WireframeYellowBackground

enum class HomeDestination {
    Register,
    OnSiteToCollect,
    DeliveryToCollect,
    History,
    Statistics,
    PriceManagement,
    PrinterSettings,
    ClosureArchive
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    syncStatus: SyncStatusSnapshot? = null,
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
                    onActionSelected = onActionSelected
                )
            } else {
                WireframeHomeLayout(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 40.dp, vertical = 28.dp),
                    syncStatus = syncStatus,
                    onActionSelected = onActionSelected
                )
            }
        }
    }
}

@Composable
private fun WireframeHomeLayout(
    modifier: Modifier = Modifier,
    syncStatus: SyncStatusSnapshot?,
    onActionSelected: (HomeDestination) -> Unit
) {
    Column(modifier = modifier) {
        HomeWireframeHeader(syncStatus = syncStatus)

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            NavButton("Historique", Modifier.weight(1f)) {
                onActionSelected(HomeDestination.History)
            }
            NavButton("Statistiques", Modifier.weight(1f)) {
                onActionSelected(HomeDestination.Statistics)
            }
            NavButton("Prix", Modifier.weight(1f)) {
                onActionSelected(HomeDestination.PriceManagement)
            }
            NavButton("Clôture", Modifier.weight(1f)) {
                onActionSelected(HomeDestination.ClosureArchive)
            }
            NavButton("Imprimantes", Modifier.weight(1f)) {
                onActionSelected(HomeDestination.PrinterSettings)
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
            PendingButton(
                label = "À encaisser\nsur place /\nà emporter",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                onClick = { onActionSelected(HomeDestination.OnSiteToCollect) }
            )
            CaisseButton(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxHeight(),
                onClick = { onActionSelected(HomeDestination.Register) }
            )
            PendingButton(
                label = "À encaisser\nlivraison",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                onClick = { onActionSelected(HomeDestination.DeliveryToCollect) }
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        AmhAttribution()
    }
}

@Composable
private fun CompactHomeLayout(
    modifier: Modifier = Modifier,
    syncStatus: SyncStatusSnapshot?,
    onActionSelected: (HomeDestination) -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HomeWireframeHeader(syncStatus = syncStatus)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NavButton("Historique", Modifier.weight(1f)) {
                onActionSelected(HomeDestination.History)
            }
            NavButton("Stats", Modifier.weight(1f)) {
                onActionSelected(HomeDestination.Statistics)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NavButton("Prix", Modifier.weight(1f)) {
                onActionSelected(HomeDestination.PriceManagement)
            }
            NavButton("Clôture", Modifier.weight(1f)) {
                onActionSelected(HomeDestination.ClosureArchive)
            }
        }
        NavButton("Imprimantes", Modifier.fillMaxWidth()) {
            onActionSelected(HomeDestination.PrinterSettings)
        }

        CaisseButton(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp),
            onClick = { onActionSelected(HomeDestination.Register) }
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PendingButton(
                label = "À encaisser\nsur place /\nà emporter",
                modifier = Modifier
                    .weight(1f)
                    .height(110.dp),
                onClick = { onActionSelected(HomeDestination.OnSiteToCollect) }
            )
            PendingButton(
                label = "À encaisser\nlivraison",
                modifier = Modifier
                    .weight(1f)
                    .height(110.dp),
                onClick = { onActionSelected(HomeDestination.DeliveryToCollect) }
            )
        }
        AmhAttribution()
    }
}

@Composable
private fun AmhAttribution() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterEnd
    ) {
        Text(
            text = "Kebaba Troyes",
            style = MaterialTheme.typography.labelSmall,
            color = WireframeSubtitle
        )
    }
}

@Composable
private fun HomeWireframeHeader(syncStatus: SyncStatusSnapshot? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Kebaba Caisse",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            syncStatus?.let { SyncStatusBadge(it) }
        }
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = WireframeBorder.copy(alpha = 0.25f))
    }
}

@Composable
private fun SyncStatusBadge(status: SyncStatusSnapshot) {
    val (label, bg, fg) = when (status.state) {
        SyncUiState.OK -> Triple("Sync OK", WireframeGreenBackground, WireframeGreenText)
        SyncUiState.PENDING -> Triple(
            "Sync ${status.pendingCount}",
            WireframeYellowBackground,
            KebabaBordeaux
        )
        SyncUiState.ERROR -> Triple(
            "Sync erreur",
            Color(0xFFFFEBEE),
            CaisseRed
        )
        SyncUiState.DISABLED -> Triple("Sync off", WireframeNavBackground, WireframeSubtitle)
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = bg),
        border = BorderStroke(1.dp, fg.copy(alpha = 0.45f)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = fg
        )
    }
}

@Composable
private fun NavButton(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    WireframeCard(
        modifier = modifier.height(56.dp),
        containerColor = WireframeNavBackground,
        borderColor = WireframeBorder,
        borderWidth = 1.dp,
        cornerRadius = 12.dp,
        onClick = onClick
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun CaisseButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    WireframeCard(
        modifier = modifier,
        containerColor = WireframeGreenBackground,
        borderColor = WireframeGreenBorder,
        borderWidth = 3.dp,
        cornerRadius = 14.dp,
        onClick = onClick
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "CAISSE",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = WireframeGreenText,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Commander / encaisser",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = WireframeGreenText,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PendingButton(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    WireframeCard(
        modifier = modifier.widthIn(min = 120.dp),
        containerColor = WireframeYellowBackground,
        borderColor = WireframeBorder,
        borderWidth = 1.dp,
        cornerRadius = 12.dp,
        onClick = onClick
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}

@Composable
private fun WireframeCard(
    modifier: Modifier = Modifier,
    containerColor: Color,
    borderColor: Color,
    borderWidth: androidx.compose.ui.unit.Dp,
    cornerRadius: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(borderWidth, borderColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
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
