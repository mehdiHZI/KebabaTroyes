package com.amhsolution.caisse.domain.model

enum class OrderType(val label: String, val ticketLabel: String) {
    SUR_PLACE("Sur place", "sur place"),
    A_EMPORTER("À emporter", "à emporter"),
    LIVRAISON("Livraison", "livraison")
}
