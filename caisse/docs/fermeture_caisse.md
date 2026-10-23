# Fermeture de caisse

Date de vérification : `23-06-2026`
Statut : note de cadrage technique, non juridique

## 1. Pourquoi cette page existe

La fermeture de caisse est un sujet sensible, parce qu'on ne parle pas seulement d'un écran de
statistiques. On parle d'une opération qui doit arrêter les ventes d'une période, conserver les
totaux, garder une trace des corrections et permettre de justifier ce qui a été encaissé.

Dans notre application, il faut donc être très clair sur deux points :

- ce que la V1 sait déjà faire ;
- ce qui manque encore avant de pouvoir parler d'une vraie caisse exploitable en production.

Cette page sert à cadrer le sujet pour le développement. Elle ne remplace pas l'avis d'un
expert-comptable, d'un conseil fiscal, de l'administration ou d'un organisme certificateur.

## 2. Sources utilisées

Sources officielles consultées :

- [BOFiP - Obligation d'utilisation de logiciels ou systèmes de caisse sécurisés](https://bofip.impots.gouv.fr/bofip/10691-PGP.html/identifiant%3DBOI-TVA-DECLA-30-10-30-20260325)
- [Ministère de l'Économie - Certification des logiciels de caisse](https://www.economie.gouv.fr/entreprises/gerer-son-entreprise-au-quotidien/gerer-sa-comptabilite-et-ses-demarches/ce-quil-faut-savoir-sur-la-certification-des-logiciels-de-caisse)
- [impots.gouv.fr - Obligation d'avoir un logiciel de caisse sécurisé](https://www.impots.gouv.fr/suis-je-oblige-davoir-un-logiciel-de-caisse-securise)
- [Article 286 du Code général des impôts](https://www.legifrance.gouv.fr/codes/article_lc/LEGIARTI000053039623)
- [Article 1770 duodecies du Code général des impôts](https://www.legifrance.gouv.fr/codes/article_lc/LEGIARTI000036430548)

Ressource métier non officielle, utilisée seulement pour vulgariser les tickets X et Z :

- [Innovorder - Ticket X et ticket Z](https://www.innovorder.com/blog/ticket-x-z-caisse)

En cas de doute, les sources officielles passent avant les articles de vulgarisation.

## 3. À retenir rapidement

Pour notre projet, il faut retenir ceci :

- `Quitter` ne ferme pas la caisse : ça ferme seulement l'application ou l'écran.
- Le rapport `X` sert à consulter les totaux sans rien figer.
- La clôture `Z` fige une période et crée un enregistrement qui ne doit plus être modifié.
- Notre objectif actuel est une clôture de gestion restaurant, pas une certification de caisse.
- La V1 crée une clôture journalière automatique à partir de `23:59`.
- Les ventes déjà clôturées ne doivent pas être modifiées directement.
- Les corrections doivent être tracées comme de nouvelles opérations.
- Notre V1 n'est pas certifiée par un organisme accrédité.
- Notre V1 n'a pas encore d'attestation individuelle éditeur.
- On ne doit donc pas présenter l'application comme conforme ou certifiée pour l'instant.

## 4. Ce que demande le cadre réglementaire

Le BOFiP explique que lorsqu'un professionnel assujetti à la TVA utilise un logiciel ou système de
caisse pour enregistrer les règlements de clients particuliers, la partie caisse doit respecter
quatre conditions :

- inaltérabilité ;
- sécurisation ;
- conservation ;
- archivage.

Dit plus simplement : une vente encaissée doit rester traçable. On doit pouvoir savoir ce qui a été
vendu, quand, comment ça a été payé, et si quelque chose a été corrigé ensuite.

Les données importantes sont notamment :

- le numéro du ticket ou justificatif ;
- la date et l'heure ;
- le numéro de caisse ;
- le montant total TTC ;
- le détail des produits vendus ;
- le moyen de paiement ;
- les modifications, corrections ou annulations ;
- les informations qui permettent de vérifier l'intégrité des données.

Le BOFiP indique aussi que les logiciels ou systèmes de caisse doivent prévoir des clôtures
journalières, mensuelles et annuelles. Ces clôtures doivent conserver des données cumulatives et
récapitulatives.

## 5. Certification, attestation et vocabulaire

Il faut faire attention au vocabulaire. Dire "le logiciel est certifié" sans précision serait faux
ou au minimum trop vague.

Deux cas existent :

| Cas | Ce que ça veut dire |
| --- | --- |
| Certificat d'un organisme accrédité | Un organisme certificateur reconnu a contrôlé le logiciel. |
| Attestation individuelle de l'éditeur | L'éditeur du logiciel atteste lui-même que sa version respecte les exigences. |

La loi de finances pour 2026 a rétabli la possibilité de justifier la conformité par une
attestation individuelle de l'éditeur.

Pour notre projet, la formulation correcte est :

```text
Le logiciel n'est pas certifié par un organisme accrédité.
Il ne pourra être présenté comme conforme que si l'éditeur produit une attestation individuelle
valable pour cette version, ou si un certificat d'organisme accrédité est obtenu.
```

Une attestation éditeur n'est pas un simple "on pense que c'est bon". Elle engage l'éditeur sur une
version précise du logiciel. Si on change des parties sensibles comme les tickets, les corrections,
les clôtures ou l'archivage, il faudra revérifier la conformité de la version.

## 6. État actuel de notre application

La V1 ne doit pas être présentée comme une caisse conforme ou certifiée.

Ce qui existe deja :

- une premiere cloture Z journaliere enregistree en base locale ;
- une **cloture manuelle illimitee** (provisoire) : recalcule et ecrase la Z du jour, sans
  verrouiller les tickets ;
- une creation automatique des clotures de journees precedentes a partir de `23:59`
  (definitive : ecrase la manuelle et **verrouille** les tickets du jour) ;
- une creation automatique des clotures mensuelles quand le mois est termine ;
- une creation automatique des clotures annuelles quand l'annee est terminee ;
- un export JSON dans le dossier interne `cloture/annee/mois/` ;
- un export PDF partageable depuis le detail d'une cloture ;
- un ecran `Cloture` qui remplace l'ancien bouton `Parametres` ;
- un mot de passe provisoire `1234` avant l'ouverture de l'ecran `Cloture` ;
- un tri des clotures par annee, mois et jour ;
- un calcul des totaux depuis les tickets encaisses ;
- une seule Z journaliere par jour (toujours la derniere version) ;
- un verrouillage des tickets d'une journee **uniquement apres** la cloture Z automatique.

Ce qui manque encore :

- pas de rapport X provisoire ;
- impression Bluetooth du ticket Z implementee, mais encore a valider sur l'imprimante physique du restaurant ;
- pas d'archive fiscale ouverte et relisible ;
- pas de gestion stricte des corrections après paiement sous forme d'avoir ou de contre-ticket ;
- pas d'attestation individuelle éditeur ;
- pas de certificat d'organisme accrédité.

L'historique Room est utile : il garde les tickets après redémarrage de l'application. Mais ce n'est
pas suffisant pour parler de conformité fiscale. Pour l'instant, c'est une base technique, pas une
preuve réglementaire complète.

## 7. Quitter, rapport X et clôture Z

Il ne faut pas confondre trois actions :

| Action | Effet réel |
| --- | --- |
| Quitter l'application | Ferme l'écran ou l'application. Aucun effet comptable. |
| Rapport X | Affiche les totaux provisoires. Rien n'est figé. |
| Clôture Z | Arrête une période et crée un enregistrement définitif. |

Le rapport X est un contrôle. Le restaurateur peut l'utiliser dans la journée pour voir où il en est.
Il peut être consulté ou imprimé plusieurs fois.

La clôture Z est différente : elle fige la période. Après une clôture Z, les tickets concernés ne
doivent plus être modifiés directement. Si une erreur est trouvée, il faut créer une correction
tracée, pas modifier silencieusement l'ancien ticket.

Dans la V1, cette règle est appliquée de façon pragmatique : l'historique autorise encore une
correction du paiement ou une annulation tant que la journée n'est pas clôturée. Dès que la clôture
Z journalière existe, le ticket devient consultable uniquement. Cette logique ne remplace pas encore
une vraie gestion d'avoir ou de contre-ticket, mais elle évite de modifier après coup les ventes
déjà figées.

## 8. Ce qu'une clôture doit figer

Une clôture journalière doit au minimum figer :

- l'identifiant de clôture ;
- le type de clôture : journalière, mensuelle ou annuelle ;
- la période couverte ;
- la date et l'heure de clôture ;
- le numéro de caisse ;
- le nombre de tickets ;
- le premier et le dernier numéro de ticket ;
- le chiffre d'affaires total ;
- le chiffre d'affaires par type de commande ;
- la répartition par moyen de paiement ;
- les corrections et annulations ;
- le grand total de la période ;
- le total perpétuel depuis le début d'utilisation ;
- le fichier JSON de clôture.

Les commandes en attente ne doivent pas être comptées comme ventes. Tant qu'une commande n'est pas
encaissée, elle ne doit pas entrer dans le chiffre d'affaires clôturé.

## 9. Modèle technique proposé

Future entité possible : `CashClosure`.

```text
id
closureType
periodStart
periodEnd
closedAtEpochMillis
cashRegisterId
ticketCount
firstTicketNumber
lastTicketNumber
grossTotalCentimes
orderTypesBreakdown
paymentsByMethod
expectedCashCentimes
countedCashCentimes
cashDifferenceCentimes
correctionTotalCentimes
periodGrandTotalCentimes
perpetualTotalCentimes
archiveFileName
appVersion
createdBy
```

Types de clôture à prévoir :

| Type | Base de calcul | Rôle |
| --- | --- | --- |
| `DAILY` | Tickets encaissés de la journée métier | Clôture Z journalière. |
| `MONTHLY` | Clôtures journalières du mois | Récapitulatif mensuel. |
| `YEARLY` | Clôtures mensuelles ou journalières de l'année | Récapitulatif annuel. |

Le mensuel et l'annuel ne recalculent pas les tickets chacun de leur côté si des clôtures
journalières existent déjà. Ils s'appuient sur les données journalières déjà figées.

## 10. Archive interne et export de clôture

La V1 ne cherche pas à mettre en place un mécanisme de certification. Elle conserve un JSON interne
exploitable par l'application et produit, à la demande, un PDF lisible et partageable.

Chaque clôture journalière génère un fichier dans le stockage interne de l'application :

```text
cloture/
  2026/
    06/
      2026-06-24-ticket-z.json
      2026-06-ticket-z-mensuel.json
    2026-ticket-z-annuel.json
```

Ce JSON reprend la logique du ticket Z fourni en référence :

- informations restaurant et caisse ;
- type de clôture : journalier, mensuel ou annuel ;
- période couverte ;
- date de clôture ;
- nombre de tickets ;
- total des tickets ;
- total encaissé ;
- nombre d'articles ;
- nombre de commandes annulées ;
- détail des encaissements par moyen de paiement ;
- chiffre d'affaires total TTC ;
- informations de clôture : date et heure de clôture uniquement.

La TVA détaillée reste une évolution à traiter séparément. Le fond de caisse et l'écart de caisse
sont volontairement absents du ticket Z actuel : l'application ne dispose pas d'un comptage physique
fiable permettant de les calculer. On ne doit pas inventer ces montants.

## 11. Archivage

Il faut distinguer sauvegarde et archive.

Une sauvegarde sert à restaurer l'application en cas de panne.

Une archive sert à conserver des données figées, lisibles et exploitables en cas de contrôle. Elle
doit être dans un format ouvert, accompagnée d'une notice de lecture, et conserver la trace de sa
génération.

Une archive minimale devrait contenir :

- les tickets ;
- les lignes de tickets ;
- les paiements ;
- les corrections ;
- les clôtures ;
- la version de l'application ;
- une notice de lecture du format.

## 12. État implémenté au 22/07/2026

Une première base de clôture Z journalière est maintenant implémentée dans l'application Android.

Ce qui existe :

- un modèle métier `CashClosure` ;
- des types de clôture `DAILY_Z`, `MONTHLY_Z` et `YEARLY_Z` ;
- une table Room `cash_closures` ;
- une migration Room qui ajoute puis met a jour cette table sans supprimer l'historique existant ;
- une clôture automatique des journées déjà terminées ;
- une clôture mensuelle automatique pour les mois terminés ;
- une clôture annuelle automatique pour les années terminées ;
- une agrégation mensuelle et annuelle basée sur les clôtures journalières ;
- un dossier JSON interne `cloture/annee/mois/` ;
- un export PDF partageable pour chaque ticket Z ;
- un écran `Cloture` consultable depuis l'accueil ;
- une protection contre la double clôture d'une même journée ;
- un calcul des totaux depuis les tickets encaissés ;
- un nombre d'articles ;
- un nombre de commandes annulées ;
- une reprise de ce compteur dans les clôtures mensuelles et annuelles ;
- un total par type de commande : sur place, à emporter, livraison ;
- un total par moyen de paiement : espèces, carte bancaire, ticket restaurant, autre ;
- une plage de tickets : premier ticket / dernier ticket.

Ce qui n'existe pas encore :

- rapport X provisoire ;
- export CSV de la clôture ;
- validation du PDF et de l'impression du ticket Z sur le matériel réel ;
- corrections comptables tracées après clôture ;
- total perpétuel fiscalement robuste ;
- archivage fiscal complet ;
- attestation individuelle éditeur ;
- certificat d'organisme accrédité.

Cette implémentation est donc une base technique. Elle aide à structurer la future fermeture de
caisse, mais elle ne permet pas encore de présenter le logiciel comme conforme ou certifié.

## 13. Parcours utilisateur recommandé

Rapport X :

1. Le restaurateur ouvre `Fermeture de caisse`.
2. L'application affiche les totaux provisoires de la journée.
3. Le restaurateur peut imprimer ou exporter le rapport.
4. Rien n'est figé.

Clôture Z journalière :

1. L'application crée automatiquement une `CashClosure` après `23:59`.
2. Elle sépare les tickets encaissés des commandes annulées.
3. Elle calcule les totaux et le nombre d'annulations de la journée.
4. Elle génère le JSON technique du ticket Z.
5. L'écran `Cloture` permet de retrouver le rapport par année, mois et jour.
6. Le restaurateur peut imprimer le ticket Z ou l'exporter en PDF.
7. Une nouvelle journée métier peut commencer sans supprimer les données précédentes.

Clôture Z mensuelle et annuelle :

1. Les clôtures journalières sont créées en premier.
2. Quand un mois est terminé, l'application additionne les clôtures journalières du mois, y compris
   leur nombre de commandes annulées.
3. Elle crée un ticket Z mensuel en JSON.
4. Quand une année est terminée, l'application additionne les clôtures journalières de l'année.
5. Elle crée un ticket Z annuel en JSON.

Dans la V1 actuelle, la génération automatique, l'archivage JSON interne, l'impression ESC/POS et
l'export PDF sont implémentés. La validation finale sur les imprimantes physiques reste nécessaire.

Avant une installation chez un client, les données de démonstration doivent être vidées. Les tickets,
historiques et clôtures sont persistés en base locale : fermer l'application ne remet pas la caisse à
zéro.

## 14. Contenu minimal du ticket Z

Le ticket Z journalier devrait au minimum afficher :

```text
Nom du restaurant
Numéro de caisse
Type : clôture Z journalière
Période
Date de clôture
Nombre de tickets
Premier ticket / dernier ticket
Nombre d'articles
Nombre de commandes annulées
CA total
CA sur place
CA à emporter
CA livraison
Total espèces
Total carte bancaire
Total ticket restaurant
Total autres paiements
```

Techniquement, l'application conserve une archive JSON interne, construit le même contenu métier
pour l'impression Bluetooth et permet un export PDF lisible. Cette partie est détaillée dans
`docs/impression_tickets_json.md`.

## 15. Choix techniques retenus pour la V1

### Pourquoi une table dédiée

La clôture n'est pas stockée dans l'historique des tickets. Elle possède sa propre table
`cash_closures`, parce qu'une clôture représente un acte de synthèse : elle fige un ensemble de
tickets à un moment donné.

Cette séparation permet de :

- garder les tickets encaissés dans `history_entries` ;
- garder les clôtures dans `cash_closures` ;
- empêcher plus facilement deux clôtures Z pour la même journée ;
- conserver le même modèle pour les clôtures journalières, mensuelles et annuelles.

### Pourquoi un ecran Cloture dédié

La clôture n'est plus lancée depuis `Statistiques`. L'écran `Statistiques` reste un écran de
consultation des chiffres. L'écran `Cloture` sert à retrouver les rapports générés automatiquement
par date :

- année ;
- mois ;
- jour ;
- détail du ticket Z.

À terme, cet écran pourra aussi contenir :

- rapport X ;
- comptage espèces ;
- export complémentaire si un autre format devient nécessaire.

## 16. Remise à zéro avant livraison client

Le fait que les tickets, statistiques et clôtures restent visibles après fermeture de l'application
est normal. Les données sont stockées dans la base locale Room : fermer l'application ne doit pas
effacer l'historique d'une caisse.

Avant une première mise en service chez un client, il faut en revanche repartir d'une base propre.
La remise à zéro concerne uniquement :

- les tickets de test ;
- les clôtures de test ;
- les prix modifiés pendant les essais, si le client doit recevoir les prix par défaut ;
- les données locales créées pendant la démonstration.

Procédure recommandée avant livraison :

1. Vérifier que les données présentes sont bien des données de test.
2. Désinstaller l'application de la tablette, ou vider son stockage depuis Android.
3. Réinstaller la version destinée au client.
4. Ouvrir l'application et contrôler que l'historique, les statistiques et les clôtures sont vides.
5. Créer une première commande de test, puis la supprimer en refaisant une remise à zéro si besoin.

Sur une tablette Android, la méthode simple est :

```text
Paramètres Android
-> Applications
-> AMH Caisse
-> Stockage
-> Effacer les données / Vider le stockage
```

Sur l'émulateur ou en développement, on peut aussi désinstaller l'application :

```text
adb uninstall com.amhsolution.caisse
```

Attention : cette remise à zéro ne doit pas être utilisée après le démarrage réel de l'exploitation,
sauf procédure exceptionnelle validée avec le responsable, le comptable et la personne chargée de
l'archivage. Une caisse en production doit conserver ses tickets, ses clôtures et ses archives.

## 17. Ordre de développement conseillé

1. Persister les commandes en attente.
2. Rendre les tickets payés immuables.
3. Remplacer les modifications directes par des corrections tracées.
4. Ajouter les totaux perpétuels et les compteurs de période.
5. Valider les clôtures Z, leur PDF et leur impression sur le matériel réel.
6. Implémenter le rapport X.
7. Consolider les clôtures mensuelles et annuelles avec des contrôles métier plus stricts.
8. Générer les archives dans un format ouvert.
9. Préparer une attestation individuelle éditeur seulement si les conditions techniques sont réunies.
10. Faire relire par le maître de stage, le comptable et, si besoin, un organisme spécialisé.
