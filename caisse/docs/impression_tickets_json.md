# Impression des tickets via JSON

Date de verification : `29-06-2026`

## 1. Objectif

L'objectif est de preparer l'impression des tickets sans lier toute la caisse a un modele precis
d'imprimante.

La caisse doit d'abord produire un ticket structure en JSON. Ensuite, un adaptateur d'impression
transforme ce JSON en commandes compatibles avec l'imprimante choisie.

```text
HistoryRecord
  -> TicketPrintPayload
  -> JSON d'impression
  -> adaptateur imprimante
  -> commandes imprimante
  -> ticket papier
```

Ce decoupage est volontaire : si le restaurant change d'imprimante, on garde les memes donnees de
ticket et on remplace seulement l'adaptateur materiel.

## 2. Materiel vise

Reference materiel initialement transmise :

- [Imprimante Excelvan sur Amazon](https://www.amazon.fr/Excelvan-Automatique-Imprimante-Imprimantes-Matricielles/dp/B01HO7P8QM?source=ps-sl-shoppingads-lpcontext&ref_=fplfs&smid=AWCWLCQRHZ1UC&utm_source=chatgpt.com&th=1)

Le mode d'emploi transmis le 29/06/2026 indique que l'imprimante recue est une imprimante
thermique ticket `80mm`, compatible avec un jeu de commandes standard `ESC/POS`, et disponible en
connexion Bluetooth selon la variante materielle.

Points retenus pour la V1 :

- largeur papier : `80mm` ;
- largeur imprimable visee : environ `72mm` ;
- profil logique : `POS_80_DMNC_ESC_POS` ;
- connexion visee : Bluetooth classique, appareil deja associe dans Android ;
- langage supporte : commandes `ESC/POS` compatibles ;
- decoupage papier : active dans la trame envoyee a l'imprimante ;
- tiroir-caisse : commande ESC/POS envoyee par l'imprimante caisse vers le tiroir branche en RJ12.

Profil Milano Gusto utilise par defaut dans le code :

```text
profile: POS_80_DMNC_ESC_POS
paperWidth: 80mm
lineColumns: 48
cutPaper: true
openCashDrawer: false # passe a true uniquement au premier encaissement en especes
codePage: PC850
```

Le choix `48` colonnes correspond a l'imprimante caisse POS-80 testee (largeur de 576 points avec
la police A). Le bon cuisine conserve une mise en page compacte limitee a 24 colonnes afin que les
ingredients longs restent lisibles sur l'imprimante cuisine testee.

Les essais papier ont montre que la taille double en largeur et en hauteur etait trop grande, tandis
que la taille normale etait trop petite. Les tickets de commande utilisent donc le compromis ESC/POS
`GS ! 0x10` : largeur `x1`, hauteur `x2`. Les libelles conservent ainsi le meme nombre de caracteres
par ligne tout en gagnant nettement en lisibilite. Les rapports de cloture restent en taille normale,
car ils contiennent davantage de lignes et de colonnes comptables.

Le tiroir n'est pas pilote directement par la tablette. Le cable RJ12 relie le tiroir a
l'imprimante caisse. Lorsqu'un paiement final contient des especes, l'application ajoute
l'impulsion ESC/POS `ESC p` a la trame envoyee a cette imprimante. Une reimpression manuelle ne
renvoie pas l'impulsion, afin de ne pas ouvrir le tiroir depuis l'historique.

Assets de reference ranges dans le depot :

- manuel photo de l'imprimante : `assets/printing/manual/excelvan-thermal-receipt-printer/` ;
- exemples de tickets papier : `assets/printing/examples/` ;
- flyers de carte utilises pour verifier les libelles et prix : `assets/menu/flyers/current/`.

## 3. Pourquoi passer par JSON

Le JSON sert de contrat stable entre la caisse et l'impression.

Avantages :

- le ticket imprime vient directement d'un ticket encaisse ;
- le format est testable sans imprimante branchee ;
- l'impression client, cuisine et cloture peuvent partager la meme base ;
- le driver imprimante reste remplacable ;
- le JSON peut etre archive ou renvoye pour reimprimer un duplicata.

Ce JSON n'est pas l'archive fiscale definitive. Il est un format technique d'impression. La source
de verite reste le ticket encaisse en base locale, puis plus tard les clotures immuables.

## 4. Tickets couverts par la documentation

La logique d'impression doit couvrir trois documents differents.

| Document | Moment de generation | Role |
| --- | --- | --- |
| Ticket client final | Apres encaissement | Justificatif remis au client, avec TVA. |
| Ticket client provisoire | Quand une commande part en attente | Rappel client non fiscal, sans TVA ni numero de TVA. |
| Bon cuisine | A l'envoi en preparation | Aide de production pour la cuisine. |
| Rapport de cloture | Lors d'un rapport X ou d'une cloture Z | Recapitulatif de caisse. |

Ces trois documents peuvent partager un meme format technique, mais ils n'ont pas le meme sens.

Le ticket client final est lie a une vente encaissee. Il doit afficher le numero de ticket, le type
de commande, les produits, le total TTC, la TVA simple, le numero de TVA et les paiements.
Le numero de TVA configure pour la V1 Milano Gusto est `FR37106121080`.

Le ticket client provisoire sert uniquement quand une commande est gardee en attente, par exemple
une livraison ou une commande sur place a encaisser plus tard. Il affiche le numero de ticket, le
type de commande, les produits et le total a regler, mais il ne doit pas afficher la TVA ni le
numero de TVA. Quand la commande est finalement encaissee, un ticket client final doit sortir.

Le bon cuisine n'est pas un justificatif fiscal. Il sert uniquement a preparer la commande. Il doit
afficher ce que la cuisine doit produire : produits, quantites, composition, retraits,
supplements, mode de commande et reference utile de livraison. Il ne doit afficher ni prix, ni TVA,
ni paiement. Pour une livraison, seuls le nom client et le numero du livreur sont utiles en cuisine.
L'adresse et le telephone restent sur le ticket caisse ou provisoire afin de limiter la diffusion
des donnees personnelles.

Le format cuisine doit rester volontairement simple : un bloc par plat, l'intitule du plat en
premiere ligne, puis un detail par ligne. Cela permet de lire vite le bon papier en cuisine et de
bien distinguer chaque nouveau plat.

Le rapport de cloture sert a imprimer ou exporter les totaux d'une periode. Le rapport X est
provisoire. Le rapport Z fige une periode. Le detail fonctionnel de cette partie est documente dans
`docs/fermeture_caisse.md`.

## 5. Format JSON prevu

Le code ajoute un premier generateur dans :

```text
android_app/app/src/main/java/com/amhsolution/caisse/domain/printing/TicketPrintPayload.kt
```

Exemple simplifie :

```json
{
  "schemaVersion": 1,
  "documentType": "CUSTOMER_RECEIPT",
  "restaurant": {
    "name": "Milano Gusto"
  },
  "ticket": {
    "number": 42,
    "label": "#42",
    "businessDay": "2026-06-23",
    "paidAtEpochMillis": 1782200000000,
    "orderType": "A_EMPORTER",
    "orderTypeLabel": "A emporter"
  },
  "delivery": null,
  "lines": [
    {
      "productName": "Margherita",
      "displayLabel": "2x Margherita",
      "quantity": 2,
      "unitTotalCentimes": 800,
      "lineTotalCentimes": 1600,
      "kitchenDetails": [
        "Taille : S",
        "Base tomate",
        "Mozzarella"
      ]
    }
  ],
  "totals": {
    "totalCentimes": 1600
  },
  "payments": [
    {
      "method": "CARTE_BANCAIRE",
      "label": "Carte bancaire",
      "amountCentimes": 1600
    }
  ],
  "printer": {
    "profile": "POS_80_DMNC_ESC_POS",
    "paperWidth": "80mm",
    "lineColumns": 48,
    "cutPaper": true,
    "openCashDrawer": false,
    "codePage": "PC850"
  }
}
```

Les montants restent en centimes. L'adaptateur d'impression convertira les montants au format
lisible, par exemple `16,00 EUR`.

## 6. Types de documents a prevoir

Le champ `documentType` prepare plusieurs usages :

| Type | Role |
| --- | --- |
| `CUSTOMER_RECEIPT` | Ticket client remis apres encaissement. |
| `CUSTOMER_PROVISIONAL_RECEIPT` | Ticket client provisoire pour une commande non encaissee. |
| `KITCHEN_TICKET` | Bon cuisine sans details de paiement. |
| `CLOSURE_REPORT` | Rapport de cloture X ou Z. |

Pour une reimpression, il faudra ajouter un marquage visible :

```text
DUPLICATA
Imprime le 23-06-2026 18:42
Ticket original #42
```

Cela evite de confondre un duplicata avec un nouveau ticket.

## 7. Contenu attendu par type de ticket

### Ticket client

Contenu minimal :

```text
Nom du restaurant
Numero de ticket
Date et heure
Type de commande
Lignes de produits
Total
Moyens de paiement
Message de fin
```

Exemple papier :

```text
MILANO GUSTO
Ticket #42 - A emporter
23/06/2026 18:42
-------------------------------
2x Margherita          16,00 EUR
-------------------------------
TOTAL                  16,00 EUR
CB                     16,00 EUR
Merci et a bientot
```

Le ticket client final doit etre genere depuis un ticket encaisse. Une commande non payee ne doit
pas etre imprimee comme ticket client final. Si elle doit sortir sur papier cote client avant
paiement, on utilise `CUSTOMER_PROVISIONAL_RECEIPT`.

Dans l'application :

- l'encaissement immediat d'une nouvelle commande respecte la sortie automatique configuree ;
- une mise en attente respecte aussi cette sortie et constitue l'unique impression cuisine
  automatique de la commande ;
- une commande en attente modifiee puis renvoyee ne ressort pas une seconde fois en cuisine ;
- l'encaissement ulterieur d'une commande en attente imprime uniquement le ticket client final :
  aucun second bon cuisine n'est genere ;
- les boutons manuels `Imprimer` proposent `Cuisine`, `Caisse` ou `Les deux` ;
- `Cuisine` imprime un bon cuisine ;
- `Caisse` imprime un ticket client final depuis l'historique, ou provisoire depuis une commande en attente.

### Bon cuisine

Contenu minimal :

```text
BON CUISINE
Commande #42
Type : Livraison
Heure d'envoi
-------------------------------
1x Frutti del mare
  - Taille : S
  - Base tomate
  - Mozzarella
  - Fruits de mer
  + Saumon
-------------------------------
2x Focaccia mortadelle
  - Pain focaccia
  - Mortadelle
  - Creme burrata
  - Tomates seches
-------------------------------
Client : Martin
Livreur : Livreur 3
```

Regles :

- ne pas afficher le detail des paiements ;
- afficher un plat par bloc, avec un detail par ligne ;
- afficher la composition utile, les retraits et les supplements ;
- afficher le type de commande ;
- afficher uniquement le nom client et le numero du livreur pour une livraison ;
- ne pas inclure le telephone ni l'adresse dans le payload ou le papier cuisine ;
- pouvoir sortir avant l'encaissement si le restaurant veut lancer la preparation en avance ;
- garder une trace de la generation ou de la reimpression du bon cuisine.

Le profil cuisine utilise 24 colonnes pour correspondre a l'imprimante testee. Les supplements sont
notes avec `+` et les retraits avec `SANS`. Cette notation garde par exemple `+ Pommes de terre` sur
une seule ligne et evite les fins de mots isolees sur la ligne suivante. Les autres retours a la
ligne sont places entre deux mots.

Le bon cuisine devra probablement etre imprime sur une imprimante differente du ticket client. La
modelisation de base prevoit deja cette separation avec une imprimante de type `client` et une
imprimante de type `cuisine` dans `docs/modelisation_bdd_looping.md`.

### Rapport de cloture

Le rapport de cloture utilise le meme principe d'impression, mais son contenu vient des totaux de
caisse et non d'une commande.

Pour le rapport Z journalier, le contenu minimal est documente dans
`docs/fermeture_caisse.md`.

Depuis le 24/06/2026, l'application sait enregistrer des `CashClosure` journalieres, mensuelles et
annuelles dans Room et generer les JSON de cloture dans le dossier interne :

```text
cloture/annee/mois/jour-ticket-z.json
cloture/annee/mois/annee-mois-ticket-z-mensuel.json
cloture/annee/annee-ticket-z-annuel.json
```

Ce JSON reprend la structure generale du ticket Z : informations restaurant, general,
encaissements, chiffre d'affaires et types de commande. Le nombre de commandes annulees remplace le
panier moyen dans le rendu. Les tickets mensuels et annuels additionnent ce compteur depuis les
clotures journalieres. L'utilisateur exporte un PDF ; le JSON reste interne.

Depuis le 29/06/2026, un second payload dedie a l'impression du ticket Z existe cote domaine :

```kotlin
CashClosure.toClosureReportPrintPayload()
```

Il produit a la fois :

- un JSON technique d'impression ;
- une version texte 80mm ;
- une trame ESC/POS prete a envoyer a l'imprimante.

## 8. Etat actuel dans le code

L'etat actuel est volontairement progressif :

- `TicketPrintPayload` existe comme contrat JSON des tickets client et cuisine ;
- `CUSTOMER_RECEIPT`, `CUSTOMER_PROVISIONAL_RECEIPT`, `KITCHEN_TICKET` et `CLOSURE_REPORT` sont deja prevus dans l'enum ;
- la conversion concrete implementee concerne le ticket client final depuis `HistoryRecord` ;
- la conversion concrete implementee concerne le ticket client provisoire depuis `OrderTicket` ;
- la conversion du bon cuisine depuis `OrderTicket` est disponible ;
- `ClosureReportPrintPayload` gere le ticket Z depuis `CashClosure` ;
- les JSON de cloture journaliere, mensuelle et annuelle sont generes depuis `CashClosure` dans
  `CashClosureArchiveJson.kt` ;
- les tickets sont transformables en texte imprimable et en commandes ESC/POS ;
- un service Bluetooth classique est prepare pour envoyer les bytes vers une imprimante deja
  associee dans Android ;
- l'ecran `Imprimantes` permet de choisir l'imprimante caisse, l'imprimante cuisine et la sortie
  automatique : `Caisse`, `Cuisine` ou `Les deux` ;
- ces choix sont conserves localement apres redemarrage de l'application ;
- les caracteres mal geres par l'imprimante sont normalises avant envoi, notamment `Oeuf` a la
  place de la ligature `Œuf` ;
- les informations completes de livraison restent sur le ticket caisse ; le bon cuisine ne recoit
  que le nom client et le numero du livreur ;
- une commande deja envoyee en cuisine n'est pas renvoyee a l'imprimante cuisine au moment de son
  encaissement final.

Fonctions disponibles :

```kotlin
HistoryRecord.toCustomerTicketPrintPayload()
OrderTicket.toProvisionalCustomerTicketPrintPayload()
OrderTicket.toKitchenTicketPrintPayload()
CashClosure.toClosureReportPrintPayload()
```

Rendus disponibles :

```kotlin
TicketPrintPayload.toJsonString()
TicketPrintPayload.toPrintableText()
TicketPrintPayload.toEscPosBytes()

ClosureReportPrintPayload.toJsonString()
ClosureReportPrintPayload.toPrintableText()
ClosureReportPrintPayload.toEscPosBytes()
```

## 9. Adaptateur imprimante a developper

Le module technique suit ce principe :

```kotlin
interface ReceiptPrinter {
    suspend fun print(bytes: ByteArray): PrintResult
}
```

Implementation ajoutee :

```text
EscPosBluetoothPrinter
```

Dans le code, elle correspond a :

```text
android_app/app/src/main/java/com/amhsolution/caisse/data/printing/BluetoothReceiptPrinter.kt
```

Fonctionnement :

1. Android doit deja connaitre l'imprimante dans les appareils Bluetooth associes.
2. L'application demande `BLUETOOTH_CONNECT` et `BLUETOOTH_SCAN` sur Android 12+.
3. L'ecran `Imprimantes` liste les appareils deja associes dans Android et memorise l'adresse de
   l'imprimante caisse et de l'imprimante cuisine.
4. La connexion essaie le profil serie Bluetooth classique `SPP`, les UUID exposes par
   l'imprimante puis plusieurs canaux RFCOMM de secours.
5. Le texte est converti en bytes ESC/POS, puis envoye au flux de sortie Bluetooth.

Si aucune imprimante n'a encore ete choisie, le service conserve un mode de secours : il cherche
un appareil associe par mots-cles (`printer`, `pos`, `receipt`, `thermal`, `80`, `dmn`). Des qu'une adresse
est configuree, il ne bascule pas silencieusement vers une autre imprimante si celle-ci est absente.

Cette V1 ne fait pas encore de scan Bluetooth dans l'application. C'est volontaire : l'association
reste geree par Android, ce qui reduit le risque et suffit pour tester l'imprimante recue.

L'export du ticket Z utilise le partage Android standard via `FileProvider`. Le fichier JSON reste
une archive technique interne dans le dossier `cloture/`. Le bouton `Exporter PDF` genere le ticket
Z lisible puis permet a l'utilisateur de l'envoyer ou de le sauvegarder.

## 10. Regles d'impression conseillees

Sur papier `80mm`, le ticket doit rester court et lisible :

```text
MILANO GUSTO
Ticket #42 - A emporter
23/06/2026 18:42
-------------------------------
2x Margherita          16,00 EUR
-------------------------------
TOTAL                  16,00 EUR
CB                     16,00 EUR
Merci et a bientot
```

Regles pragmatiques :

- couper les lignes longues proprement ;
- convertir les accents et le symbole euro pour limiter les problemes d'encodage ;
- imprimer le type de commande ;
- imprimer le numero de ticket ;
- imprimer le total et les paiements ;
- ne jamais imprimer une commande non encaissee comme ticket client final ;
- distinguer strictement ticket caisse et ticket cuisine, car ils peuvent partir sur deux imprimantes differentes ;
- imprimer les livraisons avec nom, adresse et livreur seulement si necessaire ;
- envoyer l'ouverture RJ12 uniquement a l'imprimante caisse et uniquement apres un encaissement en especes.

## 11. Ordre d'implementation recommande

1. Generer le JSON depuis `HistoryRecord`. Fait.
2. Ajouter une preview texte du ticket. Fait.
3. Tester le format avec plusieurs tickets : pizza, supplement, paiement mixte, livraison. En cours.
4. Confirmer le modele exact de l'imprimante. Fait via manuel papier 80mm.
5. Brancher le transport reel Bluetooth. Base technique faite.
6. Ajouter l'impression automatique du ticket final apres encaissement. Fait.
7. Ajouter l'impression automatique du ticket provisoire lors de la mise en attente. Fait.
8. Ajouter le choix manuel `Cuisine`, `Caisse`, `Les deux`. Fait.
9. Ajouter `Imprimer ticket Z` dans l'ecran cloture. Fait.
10. Ajouter `Exporter PDF` dans le detail d'une cloture. Fait.
11. Tester avec l'imprimante physique associee a Android. A faire.
12. Ajouter une configuration explicite des deux imprimantes Bluetooth. Fait.
13. Tester le routage caisse/cuisine et l'ouverture RJ12 avec les deux imprimantes physiques. A faire.

## 12. Risques a surveiller

- Certaines imprimantes bas cout ont une compatibilite ESC/POS partielle.
- Le Bluetooth Android demande des permissions et peut etre instable selon les modeles.
- Les accents, le symbole euro et les caracteres speciaux doivent etre testes sur papier reel.
- Meme en `80mm`, les lignes longues doivent rester courtes pour eviter les retours automatiques.
- Le ticket papier ne remplace pas l'archive fiscale ni la cloture de caisse.
- Le signal RJ12 `ESC p` est standard, mais le tiroir doit etre branche sur le port tiroir de
  l'imprimante caisse et la compatibilite doit etre confirmee sur le materiel reel.
