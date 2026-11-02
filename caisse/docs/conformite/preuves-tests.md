# Preuves de tests — conformité ISCA

Exécution : depuis `caisse/android_app/`

```bash
./gradlew test
```

## JUnit couvrant les piliers

| Test | Couverture |
|---|---|
| `TicketIntegrityTest` | Hash stable, chaînage, verify, avoir lié |
| `CreditNoteImmutabilityTest` | Refus avoir sur correction / ANNULE ; montant négatif |
| `CashClosureXzTest` | X ne verrouille pas ; Z verrouille ; exclusion FACTICE |
| `CashClosureTest` | Totaux jour / mois / année (agrégats) |

## Scénarios manuels (tablette)

1. Encaisser un ticket → empreinte visible en détail historique.
2. Tenter une correction → bouton **Émettre un avoir** uniquement (pas de modification paiement).
3. Générer rapport X puis Z ; second Z refusé.
4. Activer badge **FACTICE** → tickets exclus sync / clôture prod.
5. Accès clôture avec PIN (bootstrap 1er accès).
6. Générer archive fiscale pour une période.

## Build

Renseigner dans `README.md` le `sha256sum` de l’APK signé de la version attestée.
