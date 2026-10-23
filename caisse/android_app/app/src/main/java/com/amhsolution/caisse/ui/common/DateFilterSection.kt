package com.amhsolution.caisse.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.amhsolution.caisse.ui.theme.CaisseBlue
import com.amhsolution.caisse.ui.theme.CaisseBlueDark
import com.amhsolution.caisse.ui.theme.WireframeBorder
import com.amhsolution.caisse.ui.theme.WireframeNavBackground

@Composable
fun DateFilterSection(
    dateFilterLabel: String,
    onOpenCalendar: () -> Unit,
    onResetToday: () -> Unit,
    modifier: Modifier = Modifier,
    hint: String = "Filtre : journee ou periode"
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = WireframeNavBackground),
        border = BorderStroke(1.dp, WireframeBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = hint,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(0.9f)
            )
            Text(
                text = dateFilterLabel,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f)
            )
            DateFilterChipButton(
                label = "Choisir une date",
                onClick = onOpenCalendar
            )
            TextButton(onClick = onResetToday) {
                Text("Aujourd'hui")
            }
        }
    }
}

@Composable
private fun DateFilterChipButton(
    label: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(999.dp),
        colors = CardDefaults.cardColors(containerColor = CaisseBlue),
        border = BorderStroke(1.dp, CaisseBlueDark)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }
}
