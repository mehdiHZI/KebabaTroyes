package com.amhsolution.caisse.domain.model

enum class PaymentMethod(val label: String) {
    ESPECES("Espèces"),
    CARTE_BANCAIRE("Carte bancaire"),
    TITRE_RESTAURANT("Ticket restaurant"),
    AUTRE("Autre"),
    ANNULE("Annulé")
}
