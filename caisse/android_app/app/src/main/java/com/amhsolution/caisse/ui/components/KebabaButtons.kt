package com.amhsolution.caisse.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.amhsolution.caisse.ui.theme.CaisseRed
import com.amhsolution.caisse.ui.theme.CaisseRedSoft
import com.amhsolution.caisse.ui.theme.KebabaBordeaux
import com.amhsolution.caisse.ui.theme.KebabaBordeauxDeep
import com.amhsolution.caisse.ui.theme.KebabaCard
import com.amhsolution.caisse.ui.theme.KebabaCreamDeep
import com.amhsolution.caisse.ui.theme.KebabaDivider
import com.amhsolution.caisse.ui.theme.KebabaInk
import com.amhsolution.caisse.ui.theme.KebabaPendingBg
import com.amhsolution.caisse.ui.theme.KebabaPendingFg

private val ButtonShape = RoundedCornerShape(12.dp)

@Composable
fun KebabaPrimaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    height: Dp = 48.dp
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(height),
        shape = ButtonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = KebabaBordeaux,
            contentColor = Color.White,
            disabledContainerColor = KebabaBordeaux.copy(alpha = 0.4f),
            disabledContentColor = Color.White.copy(alpha = 0.7f)
        )
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleSmall,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun KebabaSecondaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    height: Dp = 48.dp
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(height),
        shape = ButtonShape,
        border = BorderStroke(1.dp, KebabaDivider),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = KebabaInk,
            containerColor = KebabaCard,
            disabledContentColor = KebabaInk.copy(alpha = 0.4f)
        )
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.titleSmall,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun KebabaPendingButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    height: Dp = 48.dp
) {
    Card(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(height),
        shape = ButtonShape,
        colors = CardDefaults.cardColors(
            containerColor = KebabaPendingBg,
            disabledContainerColor = KebabaPendingBg.copy(alpha = 0.45f)
        ),
        border = BorderStroke(1.dp, KebabaPendingFg.copy(alpha = 0.25f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(Modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = label,
                modifier = Modifier.padding(horizontal = 8.dp),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall,
                color = KebabaPendingFg,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun KebabaDangerButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    height: Dp = 48.dp
) {
    Card(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(height),
        shape = ButtonShape,
        colors = CardDefaults.cardColors(
            containerColor = CaisseRedSoft,
            disabledContainerColor = CaisseRedSoft.copy(alpha = 0.45f)
        ),
        border = BorderStroke(1.dp, CaisseRed.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = label,
                modifier = Modifier.padding(horizontal = 8.dp),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall,
                color = CaisseRed,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun KebabaNavTile(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 52.dp
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(height),
        shape = ButtonShape,
        colors = CardDefaults.cardColors(containerColor = KebabaCreamDeep),
        border = BorderStroke(1.dp, KebabaDivider.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = label,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleMedium,
                color = KebabaInk,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}

@Composable
fun KebabaHeroCta(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = KebabaBordeaux),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun KebabaAccentTile(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = KebabaPendingBg,
    contentColor: Color = KebabaBordeauxDeep
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, contentColor.copy(alpha = 0.18f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = contentColor,
                textAlign = TextAlign.Center,
                lineHeight = MaterialTheme.typography.titleMedium.lineHeight
            )
        }
    }
}
