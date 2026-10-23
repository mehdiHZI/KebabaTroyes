# Cahier des charges initial - logiciel de caisse restaurant

Version : `2`  
Statut : cahier des charges à valider par Mehdi et le client  
Date : `05-06-2026`

## 1. Objet du projet

Développer, installer et mettre en service un logiciel de caisse sur mesure pour un restaurant.

Le logiciel doit permettre :

- la prise de commandes sur place, à emporter et en livraison ;
- l'encaissement par carte bancaire, espèces, titres-restaurant et autres moyens courants ;
- le paiement d'une commande avec plusieurs moyens de paiement ;
- le calcul du rendu de monnaie ;
- la consultation de l'historique des paiements ;
- l'affichage de statistiques simples ;
- l'impression d'une note client ;
- l'installation sur le matériel du client ;
- une formation initiale des utilisateurs.

Le logiciel est cédé en pleine propriété au client. Les modalités de maintenance et de mise à jour devront lui être communiquées.

## 2. Cadre de la première version

La première version suit un principe `local-first` : le logiciel fonctionne entièrement en local
sur un terminal POS Android ou une tablette Android. Une vente est enregistrée dans la base locale
et ne dépend pas d'un accès Internet.

Si un pilotage distant est demandé plus tard, une synchronisation facultative pourra être ajoutée
séparément. La caisse devra continuer à encaisser si Internet ou le service distant est indisponible.

Le logiciel est développé comme une solution non certifiée. Les conditions d'une éventuelle utilisation en production seront définies par les responsables du projet.

Une transaction encaissée doit rester traçable : elle ne peut pas être modifiée ou supprimée silencieusement. Une correction ou une annulation crée une nouvelle opération liée à la transaction d'origine.

## 3. Hypothèses de départ

- un seul établissement ;
- une seule caisse principale ;
- un terminal POS Android ou une tablette Android ;
- un TPE bancaire autonome : le caissier saisit le montant sur le TPE puis confirme le paiement dans la caisse ;
- une imprimante thermique pour les notes client ;
- un catalogue de taille modérée.

## 4. Utilisateurs

### Caissier ou serveur

- créer et modifier une commande non encaissée ;
- choisir le type de commande ;
- encaisser ;
- imprimer une note ;
- consulter les commandes récentes.

### Responsable

- consulter l'historique ;
- effectuer une correction ou une annulation traçable ;
- consulter les statistiques ;
- effectuer une clôture journalière ;
- exporter une sauvegarde.

### Administrateur technique

- installer et configurer le logiciel ;
- gérer les sauvegardes et restaurations ;
- consulter la documentation technique.

## 5. Périmètre fonctionnel

### 5.1 Catalogue

- classer les articles par catégorie ;
- définir le libellé, le prix TTC et le taux de TVA d'un article ;
- adapter si nécessaire le prix selon le type de commande.

### 5.2 Commandes

- créer une commande ;
- ajouter, retirer ou modifier la quantité d'un article avant encaissement ;
- choisir `sur_place`, `a_emporter` ou `livraison` ;
- saisir une note interne facultative ;
- afficher le total ;
- identifier la table si nécessaire ;
- saisir les informations strictement nécessaires à une livraison ;
- gérer les statuts : `a_regler`, `a_regler_livraison`, `encaissee`, `annulee`.

Une commande peut être transmise en cuisine avant l'encaissement. Le mode de transmission reste à valider avec le client.

### 5.3 Encaissement

- enregistrer un ou plusieurs règlements pour une commande ;
- prendre en charge `carte_bancaire`, `especes`, `titre_restaurant` et `autre` ;
- permettre un paiement mixte ;
- calculer le rendu de monnaie ;
- conserver l'opérateur, la date et l'heure ;
- enregistrer les corrections et annulations dans l'historique.

### 5.4 Note client

- générer une note lisible ;
- afficher les informations de l'établissement, le détail des articles, les quantités, les prix, le total, la date et l'heure ;
- imprimer la note ;
- permettre une réimpression traçable ;
- afficher `FACTICE` ou `SIMULATION` sur une note issue du mode test.

### 5.5 Historique et statistiques

- rechercher une transaction par période, type de commande ou moyen de paiement ;
- consulter le détail d'une transaction et ses corrections ;
- afficher le chiffre d'affaires TTC ;
- afficher le volume de commandes et de règlements ;
- afficher la répartition par moyen de paiement ;
- afficher les totaux journaliers.

### 5.6 Configuration et sauvegarde

- renseigner les informations de l'établissement ;
- configurer les articles, catégories, taux de TVA et moyens de paiement ;
- configurer l'imprimante ;
- gérer les utilisateurs et leurs rôles ;
- afficher la version du logiciel ;
- exporter une sauvegarde locale en `JSON` ;
- exporter des données lisibles en `CSV` ;
- restaurer une sauvegarde.

## 6. Données et contraintes techniques

### 6.1 Données à conserver

Pour chaque transaction :

- numéro de justificatif ;
- date et heure ;
- opérateur ;
- type de commande ;
- lignes de commande : libellé, quantité, prix et taux de TVA ;
- total TTC ;
- moyen ou moyens de paiement ;
- rendu de monnaie éventuel ;
- correction ou annulation éventuelle.

Les montants sont stockés en centimes sous forme d'entiers.

### 6.2 Traçabilité

- une transaction validée n'est pas modifiée directement ;
- une transaction validée n'est pas supprimée depuis l'interface ;
- une correction ou une annulation crée une nouvelle opération ;
- les réimpressions sont enregistrées ;
- une clôture journalière conserve les totaux de la journée.

### 6.3 Ergonomie et continuité de service

- interface tactile simple avec de gros boutons ;
- actions fréquentes accessibles rapidement ;
- fonctionnement sans connexion Internet ;
- enregistrement local d'une vente sans dépendance à un serveur distant ;
- message clair en cas d'échec d'impression ;
- sauvegarde sur un support distinct du terminal ;
- restauration de sauvegarde testée.

### 6.4 Préparation d'une évolution facultative

- chaque opération possède un identifiant stable, une date et une heure ;
- les données nécessaires à une vente restent disponibles localement ;
- une éventuelle synchronisation distante transmettra les opérations après leur enregistrement local ;
- une indisponibilité de la synchronisation ne bloque pas l'encaissement.

## 7. Choix de la stack technique

La première version cible une tablette Android ou un terminal POS Android. Les anciennes pistes
Windows desktop restent utiles comme documents de réflexion, mais ne sont plus la cible principale
du développement.

| Besoin | Choix retenu |
| --- | --- |
| Plateforme | `Android` |
| Langage | `Kotlin` |
| Interface tactile | `Jetpack Compose` |
| Base locale | `SQLite`, via `Room` quand les règles métier seront stabilisées |
| Paramètres simples | `DataStore`, si nécessaire |
| Tickets et sauvegardes | `JSON` |
| Exports | `CSV` |
| Livraison | `APK` installable sur tablette ou terminal POS Android |
| Tests | Tests unitaires Kotlin sur les calculs critiques |
| Gestion du code | `Git` |

Cette stack est retenue parce qu'elle correspond mieux au matériel visé, au budget probable et à
l'usage tactile au comptoir. Le développement commence sans base de données persistante complexe :
les modèles métier et les calculs sont d'abord validés en mémoire, puis la persistance locale est
ajoutée progressivement.

La proposition Android courte est disponible dans [stack_simple_android.pdf](stack_simple_android.pdf).
La proposition Windows initiale reste disponible dans [proposition-stack-locale-v1.pdf](proposition-stack-locale-v1.pdf)
comme document historique.

## 8. Matériel nécessaire

Les références exactes seront ajoutées après recherche et validation avec le client. Le matériel
à étudier en priorité est le suivant :

| Matériel | Remarque |
| --- | --- |
| Tablette Android ou terminal POS Android tactile | Appareil principal pour faire tourner le logiciel de caisse |
| Support tablette ou support POS | Support stable pour une utilisation au comptoir |
| Tiroir-caisse | Nécessaire si le restaurant encaisse des espèces |
| Imprimante ticket n°1 compatible Android | Impression principale des notes client |
| Imprimante ticket n°2 compatible Android | Deuxième point d'impression à valider : comptoir, cuisine ou secours |
| TPE bancaire autonome | Fourni ou validé par le prestataire de paiement |
| Support de sauvegarde distinct | Support externe ou emplacement sécurisé |
| Rouleaux papier | Adaptés aux imprimantes retenues |

## 9. Livrables

- APK Android installable ;
- configuration initiale du restaurant ;
- export `JSON` et `CSV` ;
- procédure de sauvegarde et restauration ;
- documentation d'installation ;
- guide utilisateur court ;
- tests automatisés sur les calculs critiques ;
- installation et formation initiale.

## 10. Critères de recette

| ID | Critère |
| --- | --- |
| R01 | Créer une commande sur place, à emporter ou en livraison |
| R02 | Ajouter et retirer des articles avant encaissement |
| R03 | Encaisser en espèces et calculer correctement le rendu |
| R04 | Encaisser par carte avec confirmation manuelle du paiement TPE |
| R05 | Encaisser avec plusieurs moyens de paiement |
| R06 | Imprimer et réimprimer une note |
| R07 | Retrouver une transaction dans l'historique |
| R08 | Annuler ou corriger une transaction sans modifier l'original |
| R09 | Produire une clôture journalière |
| R10 | Afficher les statistiques principales |
| R11 | Exporter et restaurer une sauvegarde |
| R12 | Continuer à encaisser sans accès Internet |
| R13 | Identifier clairement une note issue du mode test |

## 11. Questions à valider

1. La caisse reste-t-elle fixée au comptoir ou doit-elle être déplacée ?
2. Une imprimante thermique est-elle déjà disponible ?
3. Une imprimante cuisine est-elle nécessaire et comment les commandes lui sont-elles transmises ?
4. Faut-il ouvrir automatiquement un tiroir-caisse ?
5. Quels moyens de paiement sont utilisés aujourd'hui ?
6. Les titres-restaurant sont-ils papier, carte ou les deux ?
7. Faut-il gérer les remises ou les remboursements ?
8. Les prix diffèrent-ils entre sur place, à emporter et en livraison ?
9. Quels taux de TVA sont utilisés ?
10. Quelles données client sont nécessaires pour une livraison ?
11. Quels utilisateurs peuvent annuler ou corriger une transaction ?
12. Quel terminal POS Android ou quelle tablette Android est disponible ou envisagé ?
13. L'installation d'un APK hors Play Store est-elle acceptée sur le matériel retenu ?
14. Quel mode de connexion d'imprimante est possible sur le matériel Android : Bluetooth, USB OTG, réseau ou imprimante intégrée ?
15. Un pilotage distant ou une consultation des statistiques hors du restaurant est-il envisagé plus tard ?
