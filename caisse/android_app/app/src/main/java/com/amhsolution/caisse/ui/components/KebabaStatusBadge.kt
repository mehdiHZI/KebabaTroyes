package com.amhsolution.caisse.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.amhsolution.caisse.data.sync.SyncStatusSnapshot
import com.amhsolution.caisse.data.sync.SyncUiState
import com.amhsolution.caisse.ui.theme.CaisseRed
import com.amhsolution.caisse.ui.theme.CaisseRedSoft
import com.amhsolution.caisse.ui.theme.KebabaBordeaux
import com.amhsolution.caisse.ui.theme.KebabaCard
import com.amhsolution.caisse.ui.theme.KebabaCreamDeep
import com.amhsolution.caisse.ui.theme.KebabaInkMuted
import com.amhsolution.caisse.ui.theme.KebabaPendingBg
import com.amhsolution.caisse.ui.theme.KebabaSoftSelected

@Composable
fun KebabaStatusBadge(
    label: String,
    background: Color,
    foreground: Color,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(10.dp)
    val border = BorderStroke(1.dp, foreground.copy(alpha = 0.35f))
    Surface(
        modifier = if (onClick != null) {
            modifier.clickable(onClick = onClick)
        } else {
            modifier
        },
        shape = shape,
        color = background,
        border = border
    ) {
        BadgeLabel(label, foreground)
    }
}

@Composable
private fun BadgeLabel(label: String, foreground: Color) {
    Text(
        text = label,
        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = foreground
    )
}

@Composable
fun KebabaSyncBadge(status: SyncStatusSnapshot) {
    val (label, bg, fg) = when (status.state) {
        SyncUiState.OK -> Triple("Sync OK", KebabaSoftSelected, KebabaBordeaux)
        SyncUiState.PENDING -> Triple("Sync ${status.pendingCount}", KebabaPendingBg, KebabaBordeaux)
        SyncUiState.ERROR -> Triple("Sync erreur", CaisseRedSoft, CaisseRed)
        SyncUiState.DISABLED -> Triple("Sync off", KebabaCreamDeep, KebabaInkMuted)
    }
    KebabaStatusBadge(label = label, background = bg, foreground = fg)
}

@Composable
fun KebabaSimulationBadge(
    simulationEnabled: Boolean,
    onToggle: (() -> Unit)?
) {
    KebabaStatusBadge(
        label = if (simulationEnabled) "FACTICE" else "Prod",
        background = if (simulationEnabled) KebabaPendingBg else KebabaCard,
        foreground = if (simulationEnabled) KebabaBordeaux else KebabaInkMuted,
        onClick = onToggle
    )
}
