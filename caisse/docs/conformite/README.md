# Dossier conformité ISCA — Kebaba Caisse

Logiciel : **Kebaba Caisse** (package Android `com.amhsolution.caisse`, en cours de rebrand).

| Champ | Valeur |
|---|---|
| versionName | `1.0` |
| versionCode | `1` |
| Voie légale | **Attestation individuelle éditeur** (CGI art. 286) — **pas** certification NF525 organisme |
| Hash build | À renseigner à chaque build de release (`sha256sum` de l’APK signé) |

## Contenu

| Document | Rôle |
|---|---|
| [matrice-isca.md](matrice-isca.md) | Exigence ISCA / BOFiP ↔ feature ↔ test |
| [procedures-cloture-archivage.md](procedures-cloture-archivage.md) | X / Z J-M-A, archive, purge |
| [notice-archive.md](notice-archive.md) | Notice FR format archive fiscale |
| [preuves-tests.md](preuves-tests.md) | Preuves JUnit / scénarios |
| [modele-attestation.md](modele-attestation.md) | Modèle à remplir / signer hors git secrets |
| [journal-versions.md](journal-versions.md) | Versions majeures / mineures |

## Règle absolue

Ne jamais afficher « Certifié NF525 », logo NF525, ou « conforme » en UI tant que l’attestation n’est pas émise pour **cette** version précise.

Les rules Milano « hors conformité » dans `caisse/.cursor/rules/` sont **neutralisées** : la source de vérité est le monorepo Kebaba (`caisse-conformite`, `caisse-projet`, `caisse-metier`).
