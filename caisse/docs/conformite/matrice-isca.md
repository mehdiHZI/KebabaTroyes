# Matrice ISCA ↔ features ↔ tests

| Pilier | Exigence | Feature code | Test |
|---|---|---|---|
| **I** Inaltérabilité | Ticket validé non modifiable / non supprimable | `HistoryRepository.updatePayments` lève une erreur ; UI « Émettre un avoir » | `CreditNoteImmutabilityTest`, UI historique |
| **I** | Correction = nouvelle transaction liée | `toCreditNote` + `insertCreditNote` (`linkedTicketId`, `isCorrection`) | `TicketIntegrityTest` avoir, `CreditNoteImmutabilityTest` |
| **S** Sécurisation | Empreinte chaînée tickets | `TicketIntegrity` SHA-256 + `IntegrityHmac` (Keystore) ; colonnes Room `integrityHash` / `previousIntegrityHash` | `TicketIntegrityTest` |
| **S** | Mode école séparé | `isSimulation` / badge FACTICE ; exclus totaux prod + sync | `CashClosureXzTest` FACTICE |
| **S** | Auth responsable | PIN hashé DataStore (`ResponsibleAuthRepository`) — plus de `1234` en clair | Manuel / bootstrap PIN |
| **C** Conservation | Rapport X provisoire | `CashClosureType.DAILY_X` / `createRapportX` | `CashClosureXzTest`, `CashClosureTest` |
| **C** | Z définitive non écrasable | `createFinalDailyZ` + `insert` ABORT ; auto fin de journée | `CashClosureXzTest` |
| **C** | Totaux perpétuels | `perpetual_totals` incrémentés à chaque ticket prod | Persistance Room + observe AppContainer |
| **C** | Z mois / année sur Z jour finales | `buildMonthlyCashClosure` / `buildYearlyCashClosure` filtrés `isFinal` | `CashClosureTest` agrégats |
| **A** Archivage | Archive fiscale figée + notice | `FiscalArchiveRepository` + `notice-archive.md` | Export JSON + journal `fiscal_archives` |
| **A** | Purge après archive seulement | `purgeProductionTicketsIfArchived` refuse sans couverture | Check métier repository |

## Données minimales ticket (BOFiP)

Enregistrées dans `HistoryRecord` / JSON sync : n° ticket, datetime, caisse (`registerId`), total TTC, lignes, paiements, lien correction, empreintes.
