package com.amhsolution.caisse.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Palette Kebaba — alignée sur click-collect (`globals.css`).
 * cream #f3efe6 · cream-deep #ebe4d6 · bordeaux #6b1220 · bordeaux-deep #4a0c16
 * ink #2a1218 · ink-muted #5c4a4e · card #ffffff
 */

val KebabaCream = Color(0xFFF3EFE6)
val KebabaCreamDeep = Color(0xFFEBE4D6)
val KebabaBordeaux = Color(0xFF6B1220)
val KebabaBordeauxDeep = Color(0xFF4A0C16)
val KebabaInk = Color(0xFF2A1218)
val KebabaInkMuted = Color(0xFF5C4A4E)
val KebabaCard = Color(0xFFFFFFFF)

/** Alias UI historique (écrans repris Milano) → palette Kebaba */
val CaisseBlue = KebabaBordeaux
val CaisseBlueDark = KebabaBordeauxDeep
val CaisseBlueSoft = Color(0xFFF0E4E7)

val CaisseGreen = KebabaBordeaux
val CaisseGreenDark = KebabaBordeauxDeep
val CaisseGreenSoft = Color(0xFFF0E4E7)

val CaisseOrange = Color(0xFFC45C26)
val CaisseOrangeDark = Color(0xFF8A3A12)
val CaisseOrangeSoft = Color(0xFFF8E6D9)

val CaisseRed = Color(0xFFB42318)
val CaisseRedSoft = Color(0xFFF9E0DE)

val CaisseBackground = KebabaCream
val CaisseSurface = KebabaCard
val CaisseSurfaceVariant = KebabaCreamDeep
val CaisseInk = KebabaInk
val CaisseMuted = KebabaInkMuted
val CaisseOutline = Color(0xFFD4C4C8)

// Accents wireframe / chips (bordeaux au lieu du vert Milano)
val WireframeNavBackground = KebabaCreamDeep
val WireframeGreenBackground = Color(0xFFF0E4E7)
val WireframeGreenBorder = KebabaBordeaux
val WireframeGreenText = KebabaBordeauxDeep
val WireframeYellowBackground = Color(0xFFF8E6D9)
val WireframeRedBackground = CaisseRedSoft
val WireframeRedBorder = CaisseRed
val WireframeRedText = CaisseRed
val WireframeBorder = KebabaBordeauxDeep
val WireframeSubtitle = KebabaInkMuted
val WireframeFooter = WireframeSubtitle
