# Caisse Kebaba — changelog

Base : [`mehdiHZI/LogicielCaisseMilanoGusto`](https://github.com/mehdiHZI/LogicielCaisseMilanoGusto) (clone dans `caisse/`).

## Repris

- Stack Android / Kotlin / Jetpack Compose / Room / DataStore
- Parcours caisse : catégories, panier, paiement (CB, espèces, TR, mixte), historique, clôtures, impression ESC/POS
- Montants en **centimes** entiers
- Architecture packages UI / domain / data

## Changé pour Kebaba

- Marque UI : **Kebaba Caisse** (plus AMH / Milano Gusto)
- Palette : crème / bordeaux alignée sur `click-collect` (`#f3efe6`, `#6b1220`, …)
- Catalogue pizza / focaccia / pasta **remplacé** par la carte Kebaba (PDF Jean Jaurès + étapes)
- Dialog de personnalisation : `KebabaCustomizationDialog` (pain, frites menu, garnitures, suppléments, sauces)
- Tickets / archives : nom resto **Kebaba**
- `PizzaCatalog` vidé (stubs pour écrans legacy non branchés)

## Sync VPS (ajouté)

- API `caisse-api/` : `POST /v1/tickets` idempotent (`ticketId`), auth Bearer
- Android : outbox Room + OkHttp + retry 30s + badge sync accueil
- Config : `local.properties` (`caisse.api.baseUrl`, `caisse.api.token`) — voir `android_app/local.properties.example`
- Déploiement : DNS `caisse-api.kebaba.fr` + `docker compose` sur `proxy-net`

## Multi-canal Uber / Deliveroo (ajouté)

- `POST /v1/orders` : ingestion normalisée (canal `uber_eats` / `deliveroo` / …)
- Caisse : pull toutes les 15 s → écran **À encaisser livraison**
- Doc : `caisse-api/INTEGRATION_AGGREGATEUR.md` (brancher Deliverect ensuite)
- Demo : `POST /v1/orders/demo`

## À faire ensuite

- Durcir conformité ISCA (chaînage, Z/X, archives) — règle `caisse-conformite`
- applicationId / package `com.kebaba…` (encore `com.amhsolution.caisse`)
- Matériel imprimante / TPE Kebaba
- Menu étudiant 11h–14h (contrainte horaire)
- UI réglages serveur dans l’app (aujourd’hui BuildConfig / DataStore)
- Hooks stock / cuisine / dashboard depuis l’API tickets
