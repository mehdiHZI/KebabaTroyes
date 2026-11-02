# Procédures clôture & archivage

## Rapport X (provisoire)

1. Stats → sélectionner une journée.
2. **Générer / actualiser rapport X**.
3. Effet : totaux provisoires, **ne fige pas** le jour, tickets encore corrigeables par avoir.
4. Illimité dans la journée.

## Clôture Z journalière (définitive)

1. Stats → **Clôture Z définitive** (responsable) **ou** automatique en fin de journée métier si absente.
2. Une seule Z par `businessDay`. Refus si déjà `isFinal`.
3. Après Z : pas de réécriture de tickets ; corrections = **avoir** uniquement.
4. Archive JSON locale sous `files/cloture/…`.

## Z mensuelle / annuelle

Générées automatiquement à partir des **Z journalières finales** uniquement (`createAutomaticClosures`).

## Totaux perpétuels

Compteurs `perpetual_totals` (CA TTC, nb tickets, nb avoirs) depuis la 1ère utilisation. Remise à zéro **interdite** sans procédure changement matériel/logiciel + archivage des anciens compteurs.

## Archive fiscale

1. Appeler `FiscalArchiveRepository.createArchive(periodFrom, periodTo)`.
2. Fichier JSON + `archiveHash` + notice FR.
3. Journal Room `fiscal_archives`.
4. Purge des tickets de détail : **uniquement** si une archive couvre la période.

## Accès responsable

PIN 4–12 chiffres, stocké hashé (sel + SHA-256). Premier accès = définition du PIN. Plus de mot de passe `1234` en clair.
