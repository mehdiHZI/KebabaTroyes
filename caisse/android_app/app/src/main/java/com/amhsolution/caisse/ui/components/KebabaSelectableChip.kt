package com.amhsolution.caisse.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.amhsolution.caisse.ui.theme.KebabaBordeauxDeep
import com.amhsolution.caisse.ui.theme.KebabaCard
import com.amhsolution.caisse.ui.theme.KebabaCreamDeep
import com.amhsolution.caisse.ui.theme.KebabaDivider
import com.amhsolution.caisse.ui.theme.KebabaInk
import com.amhsolution.caisse.ui.theme.KebabaSelectedBg
import com.amhsolution.caisse.ui.theme.KebabaSelectedFg

private val ChipShape = RoundedCornerShape(12.dp)

@Composable
fun KebabaSelectableChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 44.dp,
    softUnselected: Boolean = false
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(height),
        shape = ChipShape,
        colors = CardDefaults.cardColors(
            containerColor = when {
                selected -> KebabaSelectedBg
                softUnselected -> KebabaCreamDeep
                else -> KebabaCard
            }
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) KebabaBordeauxDeep else KebabaDivider.copy(alpha = 0.75f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (selected) 1.dp else 0.dp
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) KebabaSelectedFg else KebabaInk,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}
