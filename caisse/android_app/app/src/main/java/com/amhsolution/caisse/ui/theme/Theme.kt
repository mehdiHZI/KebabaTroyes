package com.amhsolution.caisse.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun AMHCaisseTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        darkCaisseColorScheme()
    } else {
        lightCaisseColorScheme()
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

private fun lightCaisseColorScheme() = lightColorScheme(
    primary = CaisseBlue,
    onPrimary = Color.White,
    primaryContainer = CaisseBlueSoft,
    onPrimaryContainer = CaisseBlueDark,
    secondary = CaisseGreen,
    onSecondary = Color.White,
    secondaryContainer = CaisseGreenSoft,
    onSecondaryContainer = CaisseGreenDark,
    tertiary = CaisseOrange,
    onTertiary = Color.White,
    tertiaryContainer = CaisseOrangeSoft,
    onTertiaryContainer = CaisseOrangeDark,
    background = CaisseBackground,
    onBackground = CaisseInk,
    surface = CaisseSurface,
    onSurface = CaisseInk,
    surfaceVariant = CaisseSurfaceVariant,
    onSurfaceVariant = CaisseMuted,
    outline = CaisseOutline,
    outlineVariant = CaisseSurfaceVariant,
    error = CaisseRed,
    onError = Color.White,
    errorContainer = CaisseRedSoft,
    onErrorContainer = CaisseRed
)

private fun darkCaisseColorScheme() = darkColorScheme(
    primary = CaisseBlueSoft,
    onPrimary = CaisseBlueDark,
    primaryContainer = CaisseBlueDark,
    onPrimaryContainer = CaisseBlueSoft,
    secondary = CaisseGreenSoft,
    onSecondary = CaisseGreenDark,
    secondaryContainer = CaisseGreenDark,
    onSecondaryContainer = CaisseGreenSoft,
    tertiary = CaisseOrangeSoft,
    onTertiary = CaisseOrangeDark,
    background = Color(0xFF1A0A0E),
    onBackground = Color(0xFFF3EFE6),
    surface = Color(0xFF2A1218),
    onSurface = Color(0xFFF3EFE6),
    surfaceVariant = Color(0xFF3D1A22),
    onSurfaceVariant = Color(0xFFD4C4C8),
    outline = Color(0xFF8A6A72),
    outlineVariant = Color(0xFF4A2A32),
    error = CaisseRed,
    onError = Color.White,
    errorContainer = Color(0xFF6D1510),
    onErrorContainer = CaisseRedSoft
)
