package com.amhsolution.caisse.domain.closure

/**
 * Compteurs cumulatifs depuis la 1ère utilisation.
 * Ne reviennent jamais à zéro sauf changement matériel/logiciel avec archivage documenté.
 */
data class PerpetualTotals(
    val totalRevenueCentimes: Long = 0L,
    val ticketCount: Long = 0L,
    val correctionCount: Long = 0L,
    val lastUpdatedEpochMillis: Long = 0L,
    val integrityHash: String = ""
)
