# Modélisation base de données - Looping

Date : `04-06-2026`  
Objectif : préparer le MCD du logiciel de caisse avant développement Android.

## 1. Périmètre du premier MCD

Le modèle doit couvrir le minimum fonctionnel :

- articles et catégories ;
- caisse physique du restaurant ;
- serveur qui prend ou encaisse la commande ;
- table du restaurant pour les commandes sur place ;
- commande sur place, à emporter ou livraison ;
- commande sur place payable tout de suite ou plus tard ;
- livraison dans une file dédiée avec livreur, numéro de commande, prénom et adresse ;
- encaissement avec un ou plusieurs moyens de paiement ;
- ticket caisse pour le client ;
- ticket cuisine pour préparer la commande ;
- imprimantes utilisées pour les tickets ;
- tiroir-caisse associé à la caisse ;
- historique minimal des événements importants.

## 2. Correspondance avec la liste de départ

La liste de départ contient :

- caisse ;
- ticket cuisine ;
- ticket caisse ;
- imprimante ;
- livreur ;
- imprimante client ;
- table ;
- serveur ;
- tiroir-caisse.

Pour garder un modèle simple :

| Élément demandé | Modélisation retenue | Remarque |
| --- | --- | --- |
| Caisse | Entité `CAISSE` | Terminal ou tablette utilisée pour encaisser |
| Tiroir-caisse | Entité `TIROIR_CAISSE` | Associé à une caisse |
| Serveur | Entité `SERVEUR` | Personne qui prend ou valide la commande |
| Table | Entité `TABLE_RESTAURANT` | Pour les commandes sur place |
| Livreur | Entité `LIVREUR` | Pour les commandes en livraison |
| Imprimante client | Entité `IMPRIMANTE` avec type `client` | Évite de créer une table par imprimante |
| Imprimante cuisine | Entité `IMPRIMANTE` avec type `cuisine` | Même logique |
| Ticket caisse | Entité `TICKET` avec type `caisse` | Ticket remis au client |
| Ticket cuisine | Entité `TICKET` avec type `cuisine` | Bon de préparation |

On ne crée donc pas deux entités séparées `TICKET_CAISSE` et `TICKET_CUISINE`.
On crée une seule entité `TICKET`, avec un attribut `type_ticket`.
Même logique pour les imprimantes : une seule entité `IMPRIMANTE`, avec un attribut
`type_imprimante`.

## 3. Règles métier à respecter

- Une commande a toujours un type : `sur_place`, `a_emporter` ou `livraison`.
- `sur_place` peut être encaissé directement ou placé dans `à encaisser sur place`.
- `a_emporter` est encaissé directement, mais le ticket doit garder l'information `à emporter`.
- `livraison` part dans `à encaisser livraison`.
- Une commande sur place peut être rattachée à une table.
- Une commande livraison peut être rattachée à un livreur.
- Une commande peut être payée avec plusieurs paiements : espèces, CB, tickets restaurant, autre.
- Une commande peut générer un ticket caisse et un ticket cuisine.
- Un ticket est imprimé sur une imprimante configurée.
- Les montants sont stockés en centimes, par exemple `1850` pour 18,50 EUR.
- Une ligne de commande doit garder le prix et le libellé au moment de la vente.
- Si le prix d'un article change plus tard, les anciens tickets ne doivent pas changer.
- Une commande encaissée ne doit pas être modifiée silencieusement.

## 4. Entités à créer dans Looping

### CATEGORIE

| Attribut | Remarque |
| --- | --- |
| `id_categorie` | Identifiant |
| `nom` | Exemple : Menus, Boissons |
| `ordre_affichage` | Pour l'écran tactile |
| `active` | Permet de masquer une catégorie |

### ARTICLE

| Attribut | Remarque |
| --- | --- |
| `id_article` | Identifiant |
| `nom` | Libellé affiché |
| `prix_ttc_centimes` | Prix courant |
| `taux_tva` | Exemple : 10, 20 |
| `ordre_affichage` | Pour l'écran tactile |
| `active` | Permet de masquer un article |

### SERVEUR

| Attribut | Remarque |
| --- | --- |
| `id_serveur` | Identifiant |
| `nom` | Nom du serveur ou caissier |
| `role` | `serveur`, `responsable`, `admin` |
| `actif` | Compte utilisable ou non |

### TABLE_RESTAURANT

| Attribut | Remarque |
| --- | --- |
| `id_table` | Identifiant |
| `numero_table` | Exemple : 1, 2, 3 |
| `nom_table` | Exemple : Table 4, Terrasse 2 |
| `zone` | `salle`, `terrasse`, `autre` |
| `active` | Table utilisable ou non |

### LIVREUR

| Attribut | Remarque |
| --- | --- |
| `id_livreur` | Identifiant |
| `nom` | Exemple : Livreur 1 |
| `telephone` | Facultatif |
| `actif` | Livreur disponible ou non |

### CAISSE

| Attribut | Remarque |
| --- | --- |
| `id_caisse` | Identifiant |
| `nom` | Exemple : Caisse principale |
| `emplacement` | Comptoir, salle, etc. |
| `active` | Caisse utilisable ou non |

### TIROIR_CAISSE

| Attribut | Remarque |
| --- | --- |
| `id_tiroir` | Identifiant |
| `nom` | Exemple : Tiroir comptoir |
| `mode_ouverture` | Manuel, imprimante, autre |
| `active` | Tiroir utilisable ou non |

### IMPRIMANTE

| Attribut | Remarque |
| --- | --- |
| `id_imprimante` | Identifiant |
| `nom` | Exemple : Imprimante client, imprimante cuisine |
| `type_imprimante` | `client`, `cuisine`, `autre` |
| `mode_connexion` | USB, Bluetooth, réseau, intégrée |
| `adresse` | Adresse IP, nom Bluetooth ou valeur de configuration |
| `active` | Imprimante utilisable ou non |

### COMMANDE

| Attribut | Remarque |
| --- | --- |
| `id_commande` | Identifiant |
| `numero_commande` | Numéro visible restaurant |
| `type_commande` | `sur_place`, `a_emporter`, `livraison` |
| `statut` | `en_cours`, `a_regler`, `a_regler_livraison`, `encaissee`, `annulee` |
| `date_creation` | Date et heure |
| `date_validation` | Rempli à l'encaissement |
| `total_ttc_centimes` | Total final |
| `note_interne` | Facultatif |

### LIGNE_COMMANDE

| Attribut | Remarque |
| --- | --- |
| `id_ligne_commande` | Identifiant |
| `quantite` | Quantité commandée |
| `libelle_snapshot` | Nom de l'article au moment de la vente |
| `prix_unitaire_ttc_centimes` | Prix au moment de la vente |
| `taux_tva_snapshot` | TVA au moment de la vente |
| `total_ligne_centimes` | Total de la ligne |

### PAIEMENT

| Attribut | Remarque |
| --- | --- |
| `id_paiement` | Identifiant |
| `moyen_paiement` | `cb`, `especes`, `ticket_restaurant`, `autre` |
| `montant_centimes` | Montant réellement payé par ce moyen |
| `montant_recu_centimes` | Utile pour espèces |
| `rendu_centimes` | Rendu monnaie |
| `date_paiement` | Date et heure |

### DETAIL_LIVRAISON

| Attribut | Remarque |
| --- | --- |
| `id_detail_livraison` | Identifiant |
| `prenom_client` | Prénom sur la commande |
| `adresse_client` | Adresse de livraison |
| `statut_livraison` | `en_attente`, `en_livraison`, `retour_paye`, `annulee` |

### TICKET

| Attribut | Remarque |
| --- | --- |
| `id_ticket` | Identifiant |
| `numero_ticket` | Numéro de justificatif |
| `type_ticket` | `caisse` ou `cuisine` |
| `date_generation` | Date et heure |
| `total_ttc_centimes` | Total figé |
| `contenu_json` | Snapshot complet du ticket |
| `mode_test` | `true` si ticket factice |

### EVENEMENT_COMMANDE

| Attribut | Remarque |
| --- | --- |
| `id_evenement` | Identifiant |
| `type_evenement` | `creation`, `modification`, `encaissement`, `annulation`, `reimpression` |
| `date_evenement` | Date et heure |
| `motif` | Facultatif |
| `details_json` | Détail libre si besoin |

## 5. Associations et cardinalités Looping

À représenter dans Looping :

| Association | Cardinalité côté gauche | Cardinalité côté droit | Sens métier |
| --- | --- | --- | --- |
| `CATEGORIE` - `ARTICLE` | `CATEGORIE 0,n` | `ARTICLE 1,1` | Une catégorie contient plusieurs articles ; un article appartient à une catégorie. |
| `SERVEUR` - `COMMANDE` | `SERVEUR 0,n` | `COMMANDE 1,1` | Un serveur crée plusieurs commandes ; une commande est créée par un serveur. |
| `CAISSE` - `COMMANDE` | `CAISSE 0,n` | `COMMANDE 1,1` | Une commande est créée depuis une caisse. |
| `TABLE_RESTAURANT` - `COMMANDE` | `TABLE_RESTAURANT 0,n` | `COMMANDE 0,1` | Une commande sur place peut être associée à une table. |
| `COMMANDE` - `LIGNE_COMMANDE` | `COMMANDE 1,n` | `LIGNE_COMMANDE 1,1` | Une commande contient au moins une ligne. |
| `ARTICLE` - `LIGNE_COMMANDE` | `ARTICLE 0,n` | `LIGNE_COMMANDE 1,1` | Une ligne vient d'un article de la carte. |
| `COMMANDE` - `PAIEMENT` | `COMMANDE 0,n` | `PAIEMENT 1,1` | Une commande peut avoir plusieurs paiements. |
| `SERVEUR` - `PAIEMENT` | `SERVEUR 0,n` | `PAIEMENT 1,1` | Un serveur ou responsable valide un paiement. |
| `COMMANDE` - `DETAIL_LIVRAISON` | `COMMANDE 0,1` | `DETAIL_LIVRAISON 1,1` | Seules les commandes livraison ont un détail livraison. |
| `LIVREUR` - `DETAIL_LIVRAISON` | `LIVREUR 0,n` | `DETAIL_LIVRAISON 0,1` | Une livraison peut être confiée à un livreur. |
| `COMMANDE` - `TICKET` | `COMMANDE 0,n` | `TICKET 1,1` | Une commande peut générer un ticket caisse et un ticket cuisine. |
| `IMPRIMANTE` - `TICKET` | `IMPRIMANTE 0,n` | `TICKET 0,1` | Un ticket peut être imprimé sur une imprimante. |
| `CAISSE` - `TIROIR_CAISSE` | `CAISSE 1,1` | `TIROIR_CAISSE 0,1` | Une caisse peut être associée à un tiroir-caisse. |
| `COMMANDE` - `EVENEMENT_COMMANDE` | `COMMANDE 0,n` | `EVENEMENT_COMMANDE 1,1` | Les actions importantes sont historisées. |
| `SERVEUR` - `EVENEMENT_COMMANDE` | `SERVEUR 0,n` | `EVENEMENT_COMMANDE 1,1` | Un événement est rattaché à un serveur ou responsable. |

## 6. Pourquoi ce modèle reste simple

Le paiement mixte ne demande pas de colonne spéciale dans `COMMANDE`.
Il suffit d'avoir plusieurs lignes dans `PAIEMENT` pour une même commande.

Exemple :

| Commande | Moyen | Montant |
| --- | --- | --- |
| `#1201` | `especes` | `2000` |
| `#1201` | `cb` | `2200` |

Total payé : `4200`, donc 42,00 EUR.

Même logique pour les tickets :

| Commande | Type ticket | Imprimante |
| --- | --- | --- |
| `#1201` | `cuisine` | `Imprimante cuisine` |
| `#1201` | `caisse` | `Imprimante client` |

On garde une seule table `ticket`, mais chaque ligne précise son type.

## 7. Tables probablement générées en MLD

Après génération du MLD, on doit obtenir environ :

- `categorie`
- `article`
- `serveur`
- `table_restaurant`
- `livreur`
- `caisse`
- `tiroir_caisse`
- `imprimante`
- `commande`
- `ligne_commande`
- `paiement`
- `detail_livraison`
- `ticket`
- `evenement_commande`

## 8. À ne pas modéliser maintenant

Pour éviter de compliquer inutilement :

- pas de stock ;
- pas de fidélité client ;
- pas de réservation ;
- pas de multi-restaurant ;
- pas de synchronisation cloud ;
- pas de comptabilité complète ;
- pas de planning avancé des livreurs ;
- pas de maintenance technique détaillée des imprimantes ;
- pas de journal complet d'ouverture physique du tiroir-caisse.

Ces éléments peuvent être ajoutés plus tard si le client les demande et les finance.
