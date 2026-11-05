package com.amhsolution.caisse.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    height: Dp = 48.dp,
    onClick: () -> Unit
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
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    height: Dp = 48.dp,
    onClick: () -> Unit
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
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    height: Dp = 48.dp,
    onClick: () -> Unit
) {
    KebabaToneButton(
        label = label,
        modifier = modifier,
        enabled = enabled,
        height = height,
        containerColor = KebabaPendingBg,
        contentColor = KebabaPendingFg,
        borderColor = KebabaPendingFg.copy(alpha = 0.25f),
        onClick = onClick
    )
}

@Composable
fun KebabaDangerButton(
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    height: Dp = 48.dp,
    onClick: () -> Unit
) {
    KebabaToneButton(
        label = label,
        modifier = modifier,
        enabled = enabled,
        height = height,
        containerColor = CaisseRedSoft,
        contentColor = CaisseRed,
        borderColor = CaisseRed.copy(alpha = 0.35f),
        onClick = onClick
    )
}

@Composable
fun KebabaNavTile(
    label: String,
    modifier: Modifier = Modifier,
    height: Dp = 52.dp,
    onClick: () -> Unit
) {
    KebabaToneButton(
        label = label,
        modifier = modifier,
        enabled = true,
        height = height,
        containerColor = KebabaCreamDeep,
        contentColor = KebabaInk,
        borderColor = KebabaDivider.copy(alpha = 0.6f),
        textStyleBold = false,
        onClick = onClick
    )
}

@Composable
fun KebabaHeroCta(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = KebabaBordeaux,
        shadowElevation = 2.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
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
    modifier: Modifier = Modifier,
    containerColor: Color = KebabaPendingBg,
    contentColor: Color = KebabaBordeauxDeep,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        border = BorderStroke(1.dp, contentColor.copy(alpha = 0.18f))
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

@Composable
private fun KebabaToneButton(
    label: String,
    modifier: Modifier,
    enabled: Boolean,
    height: Dp,
    containerColor: Color,
    contentColor: Color,
    borderColor: Color,
    textStyleBold: Boolean = true,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(height)
            .clickable(enabled = enabled, onClick = onClick),
        shape = ButtonShape,
        color = if (enabled) containerColor else containerColor.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                modifier = Modifier.padding(horizontal = 8.dp),
                fontWeight = if (textStyleBold) FontWeight.Bold else FontWeight.SemiBold,
                style = if (textStyleBold) {
                    MaterialTheme.typography.titleSmall
                } else {
                    MaterialTheme.typography.titleMedium
                },
                color = contentColor,
                textAlign = TextAlign.Center
            )
        }
    }
}
