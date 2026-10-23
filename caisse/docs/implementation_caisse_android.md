# Implementation Android - caisse V1

Date : `29-06-2026`
Statut : documentation technique a versionner
Perimetre : caisse locale Android, catalogue Milano Gusto, encaissement, commandes en attente, historique

## 1. Objectif

Cette V1 transforme l'application en caisse tactile locale pour tablette Android.
L'objectif est de disposer d'un parcours utilisable pour tester le restaurant avant de brancher
toutes les fonctions finales comme l'impression, la configuration avancee ou les exports.

Fonctions actuellement couvertes :

- accueil et navigation vers les principaux modules ;
- creation de commande `sur place`, `a emporter` ou `livraison` ;
- ajout d'articles depuis le catalogue Milano Gusto ;
- personnalisation des pizzas : taille, base, ingrédients retirés et suppléments ;
- personnalisation des burgers, focaccias, pâtes et desserts avec les ingrédients de leur catégorie ;
- configuration des offres spéciales Duo et Trio Junior/Senior ;
- modification des quantites avant encaissement ;
- mise en attente d'une commande non payee ;
- encaissement direct ou depuis une commande en attente ;
- paiement simple ou mixte ;
- correction du moyen de paiement avant validation ;
- historique local des tickets encaisses ;
- correction du moyen de paiement depuis l'historique tant que la journee n'est pas cloturee ;
- annulation d'un ticket depuis l'historique via la commande `annuler` ;
- verrouillage des tickets apres cloture Z **automatique** (definitive) de leur journee ;
- filtres d'historique par journee ou periode, type de commande, moyen de paiement et numero de ticket ;
- tri de l'historique par date, type de ticket ou moyen de paiement ;
- regroupement des livraisons en attente par livreur ;
- statistiques de session filtrees par date ;
- modification locale et persistante des prix.

## 2. Fichiers principaux

| Fichier | Role |
| --- | --- |
| `android_app/app/src/main/java/com/amhsolution/caisse/MainActivity.kt` | Point d'entree Android. Applique le theme et lance `CaisseApp`. |
| `android_app/app/src/main/java/com/amhsolution/caisse/ui/app/CaisseApp.kt` | Racine applicative Compose. Coordonne navigation, commandes en attente et historique. |
| `android_app/app/src/main/java/com/amhsolution/caisse/ui/home/HomeScreen.kt` | Accueil tactile et boutons de navigation. |
| `android_app/app/src/main/java/com/amhsolution/caisse/ui/register/RegisterScreen.kt` | Ecran caisse : type de commande, catalogue, panier, livraison, attente ou encaissement. |
| `android_app/app/src/main/java/com/amhsolution/caisse/ui/register/PizzaCustomizationDialog.kt` | Fenêtre de personnalisation d'une pizza avant son ajout au panier. |
| `android_app/app/src/main/java/com/amhsolution/caisse/ui/register/ProductCustomizationDialog.kt` | Retraits, suppléments et option menu des burgers, focaccias, pâtes et desserts. |
| `android_app/app/src/main/java/com/amhsolution/caisse/ui/register/SpecialOfferDialog.kt` | Choix des pizzas incluses dans les offres Duo/Trio Junior/Senior. |
| `android_app/app/src/main/java/com/amhsolution/caisse/ui/payment/PaymentScreen.kt` | Ecran d'encaissement mixte et gestion des paiements. |
| `android_app/app/src/main/java/com/amhsolution/caisse/ui/orders/HistoryScreens.kt` | Historique, filtres, detail ticket et modification des paiements. |
| `android_app/app/src/main/java/com/amhsolution/caisse/ui/orders/HistoryCalendarDialog.kt` | Selection d'une journee ou d'une periode pour l'historique et les statistiques. |
| `android_app/app/src/main/java/com/amhsolution/caisse/ui/orders/OrderScreens.kt` | Listes des commandes en attente et regroupement des livraisons par livreur. |
| `android_app/app/src/main/java/com/amhsolution/caisse/ui/prices/PriceManagementScreen.kt` | Consultation et modification des prix du catalogue. |
| `android_app/app/src/main/java/com/amhsolution/caisse/ui/statistics/StatisticsScreen.kt` | Indicateurs de chiffre d'affaires et de paiements sur une periode. |
| `android_app/app/src/main/java/com/amhsolution/caisse/domain/catalog/SampleCatalog.kt` | Catalogue de reference Milano Gusto et prix par defaut. |
| `android_app/app/src/main/java/com/amhsolution/caisse/domain/catalog/PizzaCatalog.kt` | Pizzas, recettes, tailles et liste fermee des ingredients disponibles. |
| `android_app/app/src/main/java/com/amhsolution/caisse/domain/catalog/ProductOptionCatalog.kt` | Recettes et suppléments autorisés par catégorie hors pizza. |
| `android_app/app/src/main/java/com/amhsolution/caisse/domain/model/` | Modeles metier : produit, categorie, panier, ticket, livraison, paiement. |
| `android_app/app/src/main/java/com/amhsolution/caisse/domain/printing/TicketPrintPayload.kt` | Preparation du JSON d'impression d'un ticket encaisse. |
| `android_app/app/src/main/java/com/amhsolution/caisse/data/local/` | Persistence locale Room pour l'historique et conversions JSON auxiliaires. |
| `android_app/app/src/main/java/com/amhsolution/caisse/data/repository/CatalogRepository.kt` | Applique et persiste les prix modifies avec DataStore. |
| `android_app/app/src/main/java/com/amhsolution/caisse/data/repository/HistoryRepository.kt` | API interne entre l'UI et Room pour l'historique. |
| `android_app/app/src/main/java/com/amhsolution/caisse/data/repository/PendingOrderRepository.kt` | Sauvegarde/recharge les commandes non payees dans un fichier JSON local. |

## 3. Flux global de l'application

Le lancement suit une chaine volontairement simple :

```text
MainActivity
  -> AMHCaisseTheme
  -> CaisseApp
  -> HomeScreen ou ecran selectionne
```

`MainActivity` ne contient pas de logique metier. Cette separation garde l'activite stable et evite
de melanger Android systeme, navigation et interface.

`CaisseApp` contient l'etat de navigation courant :

- `currentDestination` : ecran affiche depuis l'accueil ;
- `pendingOrders` : commandes non payees chargees depuis le fichier local puis affichees en memoire ;
- `pendingPaymentOrder` : commande en attente envoyee directement vers l'encaissement ;
- `editingOrder` : commande en attente que l'on rouvre dans la caisse ;
- `selectedHistoryId` : ticket historique ouvert en detail.

Ce choix est pragmatique pour la V1 : il permet de tester vite les parcours principaux sans ajouter
une couche de navigation complexe. A terme, une navigation plus formelle pourra etre ajoutee si les
parcours deviennent plus nombreux.

## 4. Catalogue produit

Le catalogue de reference est defini dans `SampleCatalog.kt`.

Structure :

- `Product` contient `id`, `name`, `category`, `priceCentimes` ;
- `ProductCategory` regroupe les articles par onglet tactile ;
- les montants sont stockes en centimes pour eviter les erreurs d'arrondi.

Categories V1 :

- `Pizzas` ;
- `Burgers` ;
- `Focaccia` ;
- `Pasta` ;
- `Desserts` ;
- `Bambino` ;
- `Offres spéciales`.

Le catalogue correspond aux flyers définitifs Milano Gusto rangés dans `assets/menu/flyers/current/`.
Le detail des produits, prix et hypotheses est documente dans `docs/menu_milano_gusto_v1.md`.

`CatalogRepository` expose ensuite le catalogue effectif a toute l'application. Les prix modifies
depuis `PriceManagementScreen` sont conserves dans DataStore sous forme de surcharges indexees par
identifiant produit. Le nom et la categorie restent pour l'instant definis dans le code.

Cette separation evite de modifier directement `SampleCatalog` pendant l'utilisation : ce fichier
reste la valeur de repli, tandis que les tarifs de la tablette peuvent evoluer sans recompilation.

### 4.1 Catalogue pizza

`PizzaCatalog` separe trois notions :

- le produit vendu, qui porte le nom et le prix S ;
- la recette, qui porte la base et les ingredients inclus par defaut ;
- la liste globale d'ingredients, seule source autorisee pour les supplements.

Les tailles sont representees par `PizzaSize` et affichees avec les boutons `S`, `M`, `L`.
La Margherita vaut `800`, `1000`, `1300` centimes. Les autres pizzas valent `1100`, `1400`,
`1800` centimes. Les supplements valent `100` centimes par defaut.

Le restaurant peut changer chaque tarif de supplement dans `Prix > Supplements pizza`.
`CatalogRepository` conserve ces valeurs dans DataStore sous la cle
`pizza_supplement_price_overrides_json`. Une modification n'altere donc pas la recette codee dans
`PizzaCatalog` et reste disponible apres redemarrage de l'application.

### 4.2 Autres recettes, option menu et offres pizza

`ProductOptionCatalog` centralise les recettes des burgers, focaccias, pâtes et desserts. Pour une
catégorie donnée, tous les ingrédients utilisés par ses produits forment la liste fermée de ses
suppléments. Leur tarif par défaut est `100` centimes et les surcharges sont enregistrées dans
DataStore sous la clé `product_supplement_price_overrides_json`.

L'option `Le menu` est disponible pour les burgers et les sandwichs focaccia. Elle ajoute `290`
centimes au prix de la ligne et est conservée comme supplément spécial `option-menu`. Ce choix évite
de créer un produit artificiel séparé et garde la ligne lisible dans le panier, l'historique et les
futurs tickets imprimés.

Les offres `Duo Junior`, `Trio Junior`, `Duo Senior` et `Trio Senior` remplacent l'ancien onglet
`Menus`. `SpecialOfferDialog` impose respectivement deux ou trois choix de pizzas et conserve ces
choix dans la ligne de panier et dans l'historique.

## 5. Ecran caisse

`RegisterScreen` gere la prise de commande.

Etats locaux principaux :

- `selectedCategory` : categorie produit affichee ;
- `selectedOrderType` : `sur place`, `a emporter` ou `livraison` ;
- `cartLines` : lignes du panier ;
- `showPayment` : bascule vers l'ecran de paiement ;
- `deliveryCustomerName`, `deliveryAddress`, `deliveryPhoneNumber`, `deliveryDriverNumber` :
  informations de livraison.

Choix important : changer le type de commande ne vide pas le panier.
Cela permet de corriger une erreur de mode, par exemple passer de `livraison` a `a emporter`, sans
perdre la commande du client.

## 6. Mise en page responsive

`RegisterWorkspace` adapte l'ecran selon la largeur disponible :

| Largeur | Mise en page |
| --- | --- |
| `< 720.dp` | Colonnes empilees : categories, produits, panier. |
| `< 950.dp` | Mode portrait tablette : categories en bandeau, produits et panier cote a cote. |
| `>= 950.dp` | Mode paysage tablette : categories, produits, panier en trois zones. |

Cette logique sert a garder la caisse lisible si la tablette passe en vertical.

Les categories utilisent une `LazyVerticalGrid` adaptive. Les produits utilisent aussi une grille
adaptive pour eviter de coder un nombre fixe de colonnes.

## 7. Gestion du panier

Le panier repose sur `CartLine`.

Fonctions utiles :

- `addProductToCart` ajoute un produit ou augmente sa quantite s'il existe deja ;
- `updateQuantity` applique `+1` ou `-1` et supprime la ligne si la quantite tombe a zero ;
- `CartLine.lineTotalCentimes` calcule le total d'une ligne.

Pour une pizza, `CartLine` conserve en plus :

- un `lineId` propre a la personnalisation ;
- le prix unitaire fige au moment de l'ajout ;
- un `PizzaCustomization` avec taille, base, retraits et supplements.

Ce `lineId` evite de fusionner deux pizzas du meme nom si leurs compositions different. Les memes
informations sont copiees dans `OrderLineSnapshot` et dans le JSON de l'historique. La lecture reste
compatible avec les anciens tickets qui ne possedent pas ces nouveaux champs.

Le même principe est appliqué avec `ProductCustomization` pour les autres recettes et
`SpecialOfferCustomization` pour les offres Junior. Les prix unitaires sont figés lors de l'ajout,
afin qu'une modification future du catalogue ne change pas rétroactivement un ticket historique.

Les dialogues de personnalisation affichent la composition et les supplements sous forme de tuiles
compactes. La composition de base tient sur une seule ligne : quatre emplacements sont reserves
pour les recettes de un a quatre ingredients, puis le nombre de colonnes suit le nombre reel
d'ingredients. La police passe de `13.sp` a `11.sp` pour les recettes les plus chargees. Les
supplements utilisent six colonnes et toutes les cibles tactiles gardent une hauteur minimale de
`48.dp`. La couleur et la bordure indiquent l'etat actif ; le texte reste limite a deux lignes.
Les choix de taille pizza `S`, `M`, `L`, les choix de base et les personnalisations hors pizza
reprennent aussi cette logique de tuiles compactes afin d'eviter des boutons trop larges dans les
popups.

Les cartes produit ont ete compactees pour l'usage caisse : grille adaptative a partir de `96.dp`,
hauteur de carte `68.dp`, espaces reduits et suppression du libelle redondant `+ ajouter`.
Les choix de type de commande utilisent une hauteur de `44.dp` et les categories `40.dp` afin de
laisser davantage de place au catalogue et au panier.

Le controle de quantite a ete stabilise le `17-06-2026`.
Le probleme etait que la colonne quantite faisait `100.dp`, alors que ses enfants demandaient plus
d'espace. Le bouton `+` pouvait donc paraitre plus petit que le bouton `-`.

Correction appliquee :

- largeur de controle : `QuantityControlWidth = 116.dp` ;
- taille bouton : `QuantityButtonSize = 36.dp` ;
- largeur de valeur : `QuantityValueWidth = 28.dp` ;
- espacement : `QuantityButtonSpacing = 8.dp`.

L'en-tete `Qt.` utilise la meme largeur que les lignes, ce qui garde l'alignement du tableau.

## 8. Commandes en attente

Deux actions existent dans le panier :

- encaisser immediatement ;
- garder ou mettre en attente.

Libelles selon le type :

| Type | Encaissement direct | Mise en attente |
| --- | --- | --- |
| Sur place | `Encaisser sur place` | `Garder ouverte` |
| A emporter | `Encaisser a emporter` | `Mettre en attente` |
| Livraison | `Encaisser livraison` | `Envoyer en livraison` |

Dans `CaisseApp`, les commandes en attente sont affichees dans `pendingOrders`, puis sauvegardees
localement via `PendingOrderRepository` dans `filesDir/pending_orders.json`.

Les commandes `sur place` et `a emporter` apparaissent ensemble dans `A encaisser`, afin de ne pas
bloquer la caisse si un client part retirer de l'argent ou revient payer plus tard.

Les commandes `livraison` restent dans une liste separee, car elles portent des informations
specifiques : nom client, adresse et livreur.

La liste `A encaisser livraison` peut etre filtree avec `Tous les livreurs` ou `Livreur 1` a
`Livreur 10`. Les commandes sont regroupees par livreur et triees de la plus ancienne a la plus
recente dans chaque groupe. Les livraisons sans attribution restent visibles dans un groupe dedie.

Le type d'une commande en attente n'est pas fige, mais il n'est pas modifie directement dans la
liste. Le bouton `Encaisser` rouvre la commande complete dans `RegisterScreen`; l'utilisateur peut
alors corriger `Sur place`, `A emporter` ou `Livraison` avant de payer ou de remettre en attente.

Lorsqu'une commande en attente est rouverte puis remise en attente, elle remplace sa version
precedente au lieu de creer un second ticket. Cela evite les doublons et les anciennes versions
restant dans la mauvaise liste.

Cette persistance JSON est volontairement simple pour la V1 : elle protege contre une fermeture
accidentelle de l'application ou de l'emulateur, sans melanger les commandes non payees avec
l'historique des tickets deja encaisses. Une migration Room pourra etre envisagee si l'on veut
requeter plus finement les commandes ouvertes.

## 9. Livraison

Quand le type `Livraison` est selectionne, le panier affiche `DeliveryInfoPanel`.

Le panneau s'ouvre automatiquement au premier passage en livraison, avant que le panier soit
valide. Le bouton vert `OK` le replie sans effacer les informations : les produits et quantites
redeviennent alors visibles. Le bandeau `Infos livraison` permet de passer librement de la vue
panier a la vue formulaire. Une saisie incomplete peut donc etre repliee, mais les actions
d'encaissement et de mise en attente restent bloquees tant que les champs requis sont vides.

Le formulaire est verticalement defilable et tient compte du clavier Android. Le champ telephone
utilise le clavier telephonique ; cela garde la valeur saisie et le champ actif visibles en mode
paysage comme en mode portrait.

Champs demandes :

- nom client ;
- adresse ;
- telephone client ;
- livreur de `1` a `10`.

Une livraison ne peut pas etre encaissee ou envoyee si le nom, l'adresse ou le telephone sont vides.
Le numero de livreur est toujours disponible, avec `Livreur 1` comme valeur par defaut.

Le modele correspondant est `DeliveryInfo`.

## 10. Encaissement

`PaymentScreen` gere les paiements.

Moyens de paiement :

- especes ;
- carte bancaire ;
- ticket restaurant ;
- autre.

Fonctions de calcul dans `domain/payment/PaymentKeypad.kt` :

- `appendKeypadDigit` construit le montant saisi ;
- `remainingCentimes` calcule le reste a payer ;
- `totalPaidCentimes` additionne les paiements deja saisis ;
- `cashChangeCentimes` calcule le rendu monnaie pour les especes ;
- `buildPaymentRecord` cree un paiement valide.

La V1 permet le paiement mixte : on peut ajouter plusieurs paiements jusqu'a ce que le reste soit a
zero. Chaque part reste un `PaymentRecord` distinct avec sa méthode et son montant en centimes. Par
exemple, un ticket de 42,00 EUR peut conserver `20,00 EUR en espèces + 22,00 EUR en CB` sans fusionner
les deux règlements.

Correction importante : avant validation, un paiement saisi peut etre retire de la liste. Cela
permet de corriger une erreur de moyen de paiement ou de montant sans repartir de zero.

Apres validation, l'historique permet encore de mettre a jour le moyen de paiement d'un ticket,
mais uniquement tant que la journee du ticket n'a pas de cloture Z **automatique** (definitive).
Une cloture manuelle provisoire n'empeche pas les corrections. Des que la Z auto existe, le
ticket reste consultable et devient non modifiable.

Un ticket peut etre marque annule depuis le detail historique en saisissant exactement `annuler`
dans le champ de commande paiement, puis en enregistrant. Le ticket n'est pas supprime : il reste
visible pour le suivi, apparait dans l'historique et dans le compteur `Commandes annulees` des
statistiques, mais son montant effectif passe a `0` dans les totaux de chiffre d'affaires et les
clotures futures.

Le bouton `Annuler` d'une commande en attente suit la meme regle. Avant de retirer la commande de la
file `A encaisser`, l'application enregistre dans Room un ticket marque `ANNULE` avec son numero, son
type, ses articles, son total d'origine et ses informations de livraison. L'identifiant d'historique
derive de celui de la commande en attente : une double pression ne peut donc pas creer deux tickets
annules. Le ticket reste consultable, mais son montant effectif et ses parts de paiement valent zero.

## 11. Historique

L'historique utilise Room via `HistoryRepository`.

Tables actuelles :

- `history_entries` : tickets encaisses et tickets annules ;
- `ticket_counter` : compteur journalier des numeros de ticket.

Donnees conservees dans `history_entries` :

- identifiant interne ;
- numero de ticket ;
- journee metier ;
- date/heure d'encaissement ;
- type de commande ;
- lignes de commande en JSON ;
- total en centimes ;
- paiements en JSON ;
- informations de livraison en JSON si besoin.

La colonne JSON des paiements conserve toutes les parts d'un règlement mixte. Dans le tableau de
l'historique et dans le détail du ticket, la ventilation affiche désormais le montant associé à
chaque méthode, par exemple `Espèces 20,00 EUR + CB 22,00 EUR`. Si une même méthode a été utilisée
plusieurs fois sur le ticket, ses parts sont regroupées pour l'affichage, sans modifier les données
persistées.

Les nouvelles lignes pizza enregistrent aussi le produit d'origine, le prix unitaire, la taille,
la base, les ingredients retires et les supplements. Ces champs JSON sont optionnels afin que les
anciens tickets, crees avant la personnalisation des pizzas, restent lisibles sans migration ni
suppression de la base Room. L'ecriture Room est maintenant terminee avant qu'un ticket en attente
soit retire de l'interface.

Le choix JSON pour les lignes et paiements simplifie la V1. Il faudra normaliser davantage si l'on
veut faire des statistiques avancees directement en SQL.

Filtres disponibles :

- journee ou periode choisie dans le calendrier ;
- type de commande via menu deroulant : `Tout`, `Sur place`, `A emporter`, `Livraison` ;
- moyen de paiement via menu deroulant : `Tout`, `Especes`, `CB`, `Ticket resto`, `Autre`,
  `Annule` ;
- recherche par numero de ticket.

Ordres de tri disponibles :

- date d'encaissement ;
- type de ticket ;
- moyen de paiement principal.

Le sens croissant ou decroissant peut etre inverse sans modifier les filtres actifs.

Par defaut, l'historique affiche la journee courante.

### 11.1 Cohérence avec les statistiques

Les statistiques sont calculées à partir de la même liste de `HistoryRecord` que l'écran Historique
et appliquent le même filtre de journée ou de période. La ventilation ne cherche plus les tickets
payés exclusivement avec une méthode. Elle additionne chaque `PaymentRecord` selon sa méthode :

- la part en espèces alimente le total `Espèces` ;
- la part par carte alimente le total `CB` ;
- la part en titre restaurant alimente le total `Ticket restaurant` ;
- la part restante alimente le total `Autre`.

Un ticket mixte contribue donc à plusieurs lignes statistiques. Les tickets annulés restent visibles
et alimentent le compteur d'annulations, mais aucune de leurs anciennes parts ne contribue au chiffre
d'affaires ou à la ventilation des encaissements. Pour un ticket valide, la somme des parts par
méthode doit être égale au total du ticket ; cette cohérence est couverte par les tests unitaires.

### 11.2 Persistance et remise à zéro avant livraison

Les tickets, statistiques, clôtures et prix modifiés restent enregistrés après fermeture de
l'application. C'est volontaire : la base locale Room et DataStore conservent les données de caisse
entre deux lancements.

Pour un test, cela explique pourquoi les commandes créées dans l'émulateur réapparaissent après
redémarrage. Pour une exploitation réelle, c'est indispensable : fermer l'application ne doit jamais
effacer les ventes.

Avant de donner la tablette au client, il faut repartir d'un stockage vide si les données présentes
sont uniquement des données de démonstration. La procédure recommandée est de désinstaller
l'application ou de vider son stockage Android, puis de réinstaller la version livrée.

Checklist avant livraison :

1. Confirmer que les tickets et clôtures présents sont des données de test.
2. Vider les données de l'application depuis Android ou désinstaller l'application.
3. Réinstaller l'APK livré.
4. Ouvrir `Historique`, `Statistiques` et `Prix` pour vérifier l'état initial attendu.
5. Ne plus utiliser cette remise à zéro après le démarrage réel sans procédure d'archivage.

Commande utile en développement :

```text
adb uninstall com.amhsolution.caisse
```

Cette commande supprime l'application et ses données locales sur l'émulateur ou la tablette
connectée. Elle ne doit pas être utilisée comme outil de correction métier sur une caisse en
production.

### 11.3 Fermeture de caisse

Il n'y a plus de bouton `Quitter` dans l'accueil. Fermer l'application depuis Android ne correspond
pas à une clôture comptable. Les tickets deja encaisses, les clotures et les commandes encore a
encaisser restent dans le stockage local de l'application.

La distinction entre rapport X, clôture Z, clôtures journalière/mensuelle/annuelle, données à figer,
archivage, attestation individuelle éditeur et certificat d'organisme accrédité est documentée dans
`docs/fermeture_caisse.md`.

La V1 possède maintenant une première base de clôture Z journalière :

- modèle `CashClosure` ;
- table Room `cash_closures` ;
- migration Room `1 -> 2`, puis `2 -> 3`, puis `3 -> 4` (`isFinal` pour distinguer
  clôture manuelle provisoire / automatique définitive) ;
- clôture manuelle illimitée depuis Statistiques (recalcul + écrasement de la Z du jour) ;
- création automatique des clôtures des journées terminées à partir de `23:59`
  (définitive, écrase la manuelle, verrouille les tickets) ;
- création automatique des clôtures mensuelles quand le mois est terminé ;
- création automatique des clôtures annuelles quand l'année est terminée ;
- agrégation mensuelle et annuelle construite depuis les clôtures journalières ;
- génération d'un fichier JSON technique dans `cloture/annee/mois/` ;
- export utilisateur d'un ticket Z en PDF depuis l'écran `Cloture` ;
- écran `Cloture` accessible depuis l'accueil à la place de l'ancien bouton `Paramètres` ;
- accès à l'écran `Cloture` protégé par mot de passe provisoire `1234` ;
- calcul des totaux depuis les tickets encaissés ;
- nombre d'articles ;
- nombre de commandes annulées, repris dans les clôtures mensuelles et annuelles ;
- totaux par type de commande ;
- totaux par moyen de paiement ;
- comptage des paiements par moyen.

Cette base reste volontairement limitée. Il manque encore le rapport X et les corrections tracées.
Le fond de caisse et l'écart de caisse sont volontairement hors du ticket Z actuel, car aucun
comptage physique fiable ne permet encore de les calculer. L'application n'a pas pour objectif actuel
de passer une certification.

Avant une remise au client, les données de démonstration de l'émulateur ou de la tablette doivent
être supprimées. Les historiques, tickets et clôtures sont persistés localement : fermer
l'application ne les efface pas.

Fichiers principaux :

| Fichier | Rôle |
| --- | --- |
| `domain/closure/CashClosure.kt` | Modèle métier et calcul de clôture journalière, mensuelle et annuelle. |
| `data/local/entity/HistoryEntities.kt` | Entité Room `CashClosureEntity`. |
| `data/local/dao/HistoryDao.kt` | DAO `CashClosureDao`. |
| `data/local/CaisseDatabase.kt` | Version 3 de la base et migrations `1 -> 2 -> 3`. |
| `data/local/CashClosureArchiveJson.kt` | Génération du JSON de ticket Z journalier, mensuel et annuel. |
| `data/repository/CashClosureRepository.kt` | Création automatique journalière, mensuelle, annuelle, lecture et écriture des archives JSON. |
| `data/export/ClosurePdfExporter.kt` | Génération du PDF partageable depuis le même rendu que le ticket Z. |
| `ui/closure/ClosureArchiveScreen.kt` | Consultation des clôtures par année, mois et jour. |
| `ui/statistics/StatisticsScreen.kt` | Résumé statistique et état de clôture de la journée filtrée. |

## 12. Impression ticket

La base d'impression reelle est preparee pour l'imprimante thermique Bluetooth recue. Le manuel
indique une imprimante ticket `80mm` compatible `ESC/POS`. L'association Bluetooth reste geree par
Android : l'application envoie ensuite les commandes a un appareil deja associe.

`TicketPrintPayload` prepare un contrat JSON pour trois documents de commande :

- ticket client final ;
- ticket client provisoire ;
- bon cuisine ;

`ClosureReportPrintPayload` prepare le ticket Z depuis une `CashClosure`.

La conversion ticket client final convertit un `HistoryRecord` en document structure :

- type de document ;
- restaurant ;
- numero de ticket ;
- journee metier ;
- type de commande ;
- lignes imprimees ;
- total TTC ;
- TVA simple ;
- numero de TVA ;
- paiements ;
- options d'imprimante.

La fonction principale est :

```kotlin
HistoryRecord.toCustomerTicketPrintPayload()
```

Le ticket client provisoire est prepare depuis une commande en attente. Il sert a sortir un papier
cote caisse avant paiement, mais sans TVA ni numero de TVA :

```kotlin
OrderTicket.toProvisionalCustomerTicketPrintPayload()
```

Le bon cuisine est prepare depuis une commande en attente. Il reste volontairement separe du ticket
caisse et ne contient pas les informations de paiement :

```kotlin
OrderTicket.toKitchenTicketPrintPayload()
```

Chaque ligne de bon cuisine transporte aussi `kitchenDetails`. Ces details sont generes depuis la
recette du produit et les personnalisations de commande. Le rendu papier cuisine garde une forme
tres lisible : un bloc par plat, l'intitule du plat en premiere ligne, puis un ingredient, retrait
ou supplement par ligne.

Le ticket Z est prepare depuis une cloture :

```kotlin
CashClosure.toClosureReportPrintPayload()
```

Ces payloads permettent de produire un JSON stable, une version texte 80mm et une trame ESC/POS :

```kotlin
payload.toJsonString()
payload.toPrintableText()
payload.toEscPosBytes()
```

Le service `BluetoothReceiptPrinter` envoie les bytes ESC/POS via Bluetooth classique. Il demande
les permissions `BLUETOOTH_CONNECT` et `BLUETOOTH_SCAN` sur Android 12+, puis essaie successivement
les UUID proposes par l'imprimante, le profil `SPP` et des canaux RFCOMM de secours. L'imprimante
doit deja etre associee dans Android.

L'ecran `Imprimantes`, accessible depuis l'accueil, enregistre trois reglages locaux :

- l'adresse Bluetooth de l'imprimante caisse ;
- l'adresse Bluetooth de l'imprimante cuisine ;
- la sortie automatique : `Caisse`, `Cuisine` ou `Les deux`.

Le routage relit ces reglages au moment de chaque impression. Une imprimante configuree mais
indisponible provoque une erreur explicite : le logiciel n'envoie pas le document vers l'autre
imprimante par erreur.

Comportement d'impression actuel :

- encaissement immediat d'une nouvelle commande : routage automatique `Caisse`, `Cuisine` ou
  `Les deux`, selon le reglage choisi ;
- mise en attente sur place, a emporter ou livraison : sortie automatique selon le routage choisi ;
  avec `Les deux`, le ticket provisoire caisse et le bon cuisine sortent ensemble ;
- modification puis nouvelle mise en attente d'une commande existante : le ticket provisoire peut
  ressortir côté caisse ; si des articles ont été ajoutés (ou des quantités augmentées), un bon
  `AJOUT CUISINE` part automatiquement avec uniquement ces nouveautés (pas de second bon complet) ;
- encaissement final d'une commande deja en attente : ticket client final uniquement. Le bon
  cuisine est deja sorti lors de la mise en attente et n'est jamais imprime une seconde fois ;
- `Imprimer` dans un ticket ou une commande en attente : choix manuel `Cuisine`, `Caisse` ou `Les deux` ;
- `Imprimer` dans les ecrans a encaisser : choix manuel `Cuisine`, `Caisse` ou `Les deux` ;
- `Imprimer ticket Z` dans le detail d'une cloture.
- `Exporter PDF` dans le detail d'une cloture, via le partage Android standard. Le JSON reste une
  archive technique interne et n'est plus présenté comme l'export destiné à l'utilisateur.

Quand un encaissement final contient une part en especes, le ticket client automatique active
`openCashDrawer`. La trame ESC/POS contient alors l'impulsion `ESC p` envoyee a l'imprimante caisse,
qui commande le tiroir branche sur son port RJ12. Une reimpression manuelle n'ouvre pas le tiroir.

La distinction ticket caisse / ticket cuisine est explicite dans le code avec
`customerTicketPrinter` et `kitchenTicketPrinter`. Le ticket client contient les montants, la TVA,
les paiements et, pour une livraison, le nom, le telephone et l'adresse. Sous chaque article, il
reprend aussi les memes details de composition que le bon cuisine (taille, base, ingredients,
retraits, supplements, menu etudiant, gout smoothie, etc.). Le bon cuisine contient uniquement le
numero, le type de commande, les plats, un ingredient ou une modification par ligne, le nom client
et le numero du livreur. L'adresse et le telephone ne sont ni imprimes ni conserves dans le payload
cuisine : ils ne sont pas utiles a la preparation.

Le bon cuisine utilise une largeur prudente de 24 caracteres, adaptee a l'imprimante testee.
Les supplements sont prefixes par `+` et les retraits par `SANS`, ce qui permet notamment de garder
`+ Pommes de terre` sur une seule ligne. Si une mention depasse malgre tout cette largeur, elle est
coupee entre deux mots plutot qu'au milieu d'un mot.

Apres comparaison des essais papier, les tickets de commande selectionnent la taille ESC/POS
`GS ! 0x10` : largeur normale `x1` et hauteur `x2`. Ce compromis est plus lisible que la police
normale sans retrouver les coupures provoquees par l'ancienne taille `x2` en largeur et en hauteur.
Les rapports Z conservent la taille normale afin de garder leurs tableaux compacts.

La strategie retenue est documentee dans `docs/impression_tickets_json.md`.
L'idee est de garder la caisse independante de l'imprimante : le JSON est genere depuis le ticket
encaisse ou la cloture, puis l'adaptateur Bluetooth transforme ce JSON en commandes papier.

## 13. Statistiques et gestion des prix

`StatisticsScreen` calcule les indicateurs a partir des tickets Room deja charges par `CaisseApp` :

- nombre de commandes ;
- chiffre d'affaires total ;
- montants payes uniquement en especes, carte ou ticket restaurant.

Le filtre de date commun repose sur `HistoryDateFilter`. Les calculs sont centralises dans
`domain/statistics/SessionStatistics.kt` et couverts par des tests unitaires.

`PriceManagementScreen` permet de filtrer les produits par categorie, saisir un nouveau montant et
l'enregistrer. `CatalogRepository` persiste uniquement les prix modifies dans DataStore. Comme les
identifiants produits servent de cle, ils doivent rester stables lors d'une modification de carte.

Des vues séparées listent les suppléments pizza, burger, focaccia, pasta et dessert. Le prix choisi
est intégré au total au moment où le produit est ajouté au panier.

La categorie `Boissons` est geree comme les autres articles. Les canettes ont un prix initial de
`1,50 EUR`. Les bouteilles de `1,5 L` restent affichees avec `Prix a definir` et ne peuvent pas etre
ajoutees au panier tant que le restaurateur n'a pas enregistre un tarif positif dans l'ecran `Prix`.

## 14. Adaptation a un autre restaurant

Pour adapter cette caisse a un autre restaurant, les premiers fichiers a modifier sont :

| Besoin | Fichier actuel |
| --- | --- |
| Categories | `domain/model/ProductCategory.kt` |
| Produits et prix | `domain/catalog/SampleCatalog.kt` |
| Libelles de tickets | `domain/model/OrderType.kt` si les modes changent |
| Historique de demonstration | `domain/catalog/SampleHistoryRecords.kt` et `SampleOrderHistory.kt` |
| Couleurs et theme | `ui/theme/Color.kt`, `Theme.kt`, `Type.kt` |

La methode la plus simple pour un prochain restaurant :

1. Recuperer le menu valide du restaurant.
2. Convertir chaque article en `Product(id, name, category, priceCentimes)`.
3. Adapter les categories dans `ProductCategory`.
4. Garder les prix en centimes.
5. Tester les parcours : commande directe, mise en attente, livraison, paiement mixte, historique.

Pour changer uniquement un tarif sur une caisse installee, la page `Prix` suffit. Pour remplacer
les produits ou categories d'un restaurant, il faut encore modifier le catalogue de reference et
recompiler. Une prochaine etape pourra charger l'ensemble du menu depuis une configuration JSON ou
une base locale, tout en gardant les memes modeles `Product` et `ProductCategory`.

## 15. Validation locale

Dernieres verifications connues :

- compilation `:app:assembleDebug` reussie avec JDK 21 ;
- cible Android : API 35 / Android 15 ;
- lancement valide sur emulateur `Pixel Tablet API 35` d'apres les tests utilisateur ;
- boutons de navigation, commandes et encaissements testes manuellement ;
- correction du controle de quantite compilee avec succes.
- compilation du parcours de personnalisation pizza reussie avec JDK 21.
- personnalisation multi-catégorie, offres Junior et persistance des choix compilées avec succès ;
- `testDebugUnitTest` et `assembleDebug` réussissent ensemble le `26-06-2026`.
- les tests de ventilation du `17-07-2026` vérifient qu'un paiement mixte apparaît avec chaque montant
  dans l'historique et que la somme par méthode correspond au chiffre d'affaires statistique.

Apres l'integration du push `c1eb58b` le 18-06-2026 :

- `compileDebugKotlin` reussit avec JDK 21 ;
- `testDebugUnitTest` reussit, y compris les tests ajoutes pour les montants, les dates et les statistiques ;
- aucun marqueur de conflit ni ancienne categorie produit ne subsiste.

Le rendu et les parcours tactiles doivent encore etre verifies sur l'emulateur depuis Android Studio
avant commit.

Commande utilisee cote terminal :

```bash
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home ./gradlew --no-daemon -Dkotlin.compiler.execution.strategy=in-process :app:assembleDebug
```

Apres une compilation locale, on peut verifier qu'aucun daemon Gradle ne reste actif :

```bash
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home ./gradlew --stop
```

## 16. Limites connues

- Les commandes en attente sont persistantes dans un fichier JSON local, mais pas encore dans Room.
- Les produits et categories de reference restent codes en dur ; seuls les prix sont modifiables.
- L'impression papier est preparee pour Bluetooth/ESC-POS, mais doit etre validee sur l'imprimante reelle.
- L'ancien bouton `Parametres` a ete remplace par `Cloture` dans l'accueil.
- Les corrections post-validation ne sont pas encore tracees comme de vraies operations comptables.
- La cloture Z journaliere existe en base technique, mais elle n'est pas encore complete fiscalement.
- Le rapport X n'est pas encore implemente.
- Il n'y a pas encore de gestion utilisateur, role ou session.
- L'export PDF est disponible ; aucun export CSV n'est encore proposé.

## 17. Prochaines etapes recommandees

1. Tester l'ergonomie avec le maitre de stage sur tablette en paysage et portrait.
2. Valider les prix Milano Gusto avant de figer la V1.
3. Migrer les commandes en attente vers Room si le JSON local devient insuffisant.
4. Remplacer les modifications directes de paiement historique par une correction tracee.
5. Finaliser la cloture : comptage especes, ecart de caisse, rapport X et validation papier du ticket Z.
6. Ajouter l'adaptateur imprimante reel a partir du JSON de ticket.
7. Transformer les produits et categories codes en dur en configuration modifiable.
8. Recréer des paramètres seulement si le besoin fonctionnel est valide.
