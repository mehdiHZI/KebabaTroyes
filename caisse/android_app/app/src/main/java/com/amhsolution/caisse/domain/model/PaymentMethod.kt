package com.amhsolution.caisse.domain.model

enum class PaymentMethod(val label: String) {
    ESPECES("Espèces"),
    CARTE_BANCAIRE("Carte bancaire"),
    TITRE_RESTAURANT("Ticket restaurant"),
    AUTRE("Autre"),
    /** Contre-opération / avoir (nouvelle transaction, montants négatifs). */
    AVOIR("Avoir"),
    /** Legacy Milano — ne plus créer ; conserver pour lecture historique. */
    ANNULE("Annulé")
}
