# Implementation Android - ecran d'accueil

Date : `11-06-2026`
Mise a jour : `18-06-2026`
Statut : documentation technique a versionner
Perimetre : ecran d'accueil Compose, navigation principale et lien avec les premiers modules metier

## 1. Objectif

Cette implementation remplace l'ecran Android Studio par defaut par une page d'accueil de caisse.
L'objectif initial etait de poser :

- une page d'accueil tactile ;
- une action principale pour acceder a la caisse ;
- deux acces vers les commandes a encaisser ;
- des boutons secondaires pour l'historique, les statistiques, les prix et les parametres ;
- un bouton de sortie ;
- une navigation simple vers une page dediee pour chaque bouton.

L'accueil reprend l'idee du croquis `01_accueil_v2.png`. La version poussee par le maitre de stage
garde une forme proche wireframe, puis notre ajout rend les boutons cliquables pour valider le
parcours utilisateur.

Depuis cette premiere base, l'application contient maintenant une vraie premiere version de caisse :
catalogue Milano Gusto, panier, encaissement, commandes en attente et historique local. Le detail de
cette V1 est documente dans `docs/implementation_caisse_android.md`.

## 2. Fichiers principaux

| Fichier | Role |
| --- | --- |
| `android_app/app/src/main/java/com/amhsolution/caisse/MainActivity.kt` | Point d'entree Android. Lance le theme puis `CaisseApp`. |
| `android_app/app/src/main/java/com/amhsolution/caisse/ui/app/CaisseApp.kt` | Racine Compose de l'application. Garde l'ecran courant et affiche la bonne page. |
| `android_app/app/src/main/java/com/amhsolution/caisse/ui/common/PlaceholderScreen.kt` | Ecran temporaire reutilisable pour representer les pages pas encore implementees. |
| `android_app/app/src/main/java/com/amhsolution/caisse/ui/home/HomeScreen.kt` | Interface d'accueil et boutons de navigation. |
| `android_app/app/src/main/java/com/amhsolution/caisse/ui/register/RegisterScreen.kt` | Ecran caisse V1 : catalogue, panier, livraison, attente et encaissement. |
| `android_app/app/src/main/java/com/amhsolution/caisse/ui/payment/PaymentScreen.kt` | Ecran d'encaissement simple ou mixte. |
| `android_app/app/src/main/java/com/amhsolution/caisse/ui/orders/HistoryScreens.kt` | Historique local des tickets encaisses. |
| `android_app/app/src/main/java/com/amhsolution/caisse/ui/theme/Color.kt` | Palette de couleurs de l'application, dont les couleurs wireframe. |
| `android_app/app/src/main/java/com/amhsolution/caisse/ui/theme/Theme.kt` | Theme Material 3 de l'application. |
| `android_app/app/src/main/java/com/amhsolution/caisse/ui/theme/Type.kt` | Typographies utilisees par l'interface. |
| `android_app/app/build.gradle.kts` | Configuration Android, dont `minSdk`, `targetSdk` et Compose. |

## 3. Configuration Android

La tablette cible etant sous Android 15, la configuration actuelle est :

| Element | Valeur | Raison |
| --- | --- | --- |
| `minSdk` | `26` | Permet de garder une compatibilite large avec les appareils Android recents. |
| `targetSdk` | `35` | Cible officiellement Android 15, version de la tablette prevue. |
| `compileSdk` | `35` | Compile avec le SDK Android 15. |
| Interface | Jetpack Compose | Plus adapte pour construire rapidement une interface tactile moderne. |

## 4. Point d'entree

`MainActivity.kt` reste volontairement simple :

```kotlin
AMHCaisseTheme {
    CaisseApp()
}
```

Choix effectue :

- l'activite ne contient pas la mise en page ;
- l'interface est deleguee a `CaisseApp` ;
- le theme est applique au niveau le plus haut ;
- `MainActivity` reste stable meme quand on ajoute de nouveaux ecrans.

Cette separation evite que `MainActivity` devienne un fichier trop gros.

## 5. Navigation avec `CaisseApp.kt`

`CaisseApp` est la racine Compose actuelle de l'application.

Son role :

- afficher `HomeScreen` au demarrage ;
- memoriser la destination selectionnee avec `remember` et `mutableStateOf` ;
- afficher la caisse, les commandes a encaisser, l'historique ou les pages de preparation ;
- permettre de revenir a l'accueil avec le bouton `Retour`.

Extrait simplifie :

```kotlin
var currentDestination by remember { mutableStateOf<HomeDestination?>(null) }

when (currentDestination) {
    null -> HomeScreen(onActionSelected = { currentDestination = it })
    HomeDestination.Register -> RegisterScreen(...)
    HomeDestination.History -> HistoryScreen(...)
    HomeDestination.Statistics -> PlaceholderScreen(...)
}
```

Choix effectue :

- utiliser une navigation simple tant que le nombre d'ecrans reste raisonnable ;
- eviter d'ajouter `navigation-compose` trop tot ;
- garder un code facile a lire pendant la phase d'apprentissage ;
- permettre au maitre de stage de tester rapidement que chaque bouton ouvre une page.

Cette solution n'est pas forcement la navigation finale. Elle sera remplacee ou structuree plus
proprement si les parcours deviennent plus complexes.

## 6. Structure de `HomeScreen.kt`

### `HomeDestination`

Enum qui liste les destinations possibles depuis l'accueil :

- `Register` ;
- `OnSiteToCollect` ;
- `DeliveryToCollect` ;
- `History` ;
- `Statistics` ;
- `PriceManagement` ;
- `ClosureArchive` ;
- `Quit`.

Ces destinations servent a choisir la page temporaire affichee par `CaisseApp`.

### `HomeScreen`

Composable public appele par `CaisseApp`.

Parametres :

| Parametre | Role |
| --- | --- |
| `modifier` | Permet d'adapter l'affichage depuis l'exterieur si besoin. |
| `onActionSelected` | Callback appele quand l'utilisateur appuie sur un bouton. |

Choix effectue :

- l'ecran est stateless : il ne stocke pas encore d'etat metier ;
- les clics remontent par callback ;
- cela garde l'accueil reutilisable, meme si la navigation change plus tard.

### `BoxWithConstraints`

`HomeScreen` choisit la mise en page selon la largeur disponible :

- `CompactHomeLayout` si l'ecran est etroit ;
- `WireframeHomeLayout` pour une tablette.

Ce choix evite les chevauchements et permet de garder des previews utiles pour plusieurs tailles
d'ecran.

### `WireframeHomeLayout`

Version principale de l'accueil pour tablette.

Organisation :

- un en-tete avec le titre `01 - Accueil` ;
- une rangee de boutons secondaires : historique, statistiques, prix, cloture ;
- une zone centrale avec `CAISSE` au centre ;
- deux boutons lateraux pour les encaissements sur place et livraison ;
- un pied de page court qui rappelle l'intention de l'ecran.

Cette organisation garde la logique du croquis de depart et donne deja des zones cliquables.

### `CompactHomeLayout`

Version plus compacte prevue pour les ecrans et previews etroits.

Elle garde les memes actions, mais les empile verticalement pour eviter les chevauchements.

### Boutons de l'accueil

Les boutons sont separes en petits composants :

| Composant | Role |
| --- | --- |
| `NavButton` | Boutons secondaires : historique, statistiques, prix, cloture. |
| `CaisseButton` | Bouton principal de prise de commande / encaissement. |
| `PendingButton` | Boutons pour les commandes a encaisser. |
| `WireframeCard` | Composant commun qui gere la forme, la bordure et le clic. |

Chaque bouton appelle `onActionSelected` avec la destination correspondante.

## 7. Structure de `PlaceholderScreen.kt`

`PlaceholderScreen` sert encore a creer rapidement une page cible pour les modules non termines.
Les ecrans caisse, commandes a encaisser et historique ne sont plus des placeholders.

Parametres :

| Parametre | Role |
| --- | --- |
| `title` | Titre affiche en haut de la page temporaire. |
| `subtitle` | Texte court qui explique ce que contiendra la future page. |
| `onBack` | Callback appele par le bouton `Retour`. |
| `modifier` | Permet de reutiliser le composant dans un autre conteneur si besoin. |

Choix effectue :

- donner une page visible a chaque action sans inventer trop tot la logique metier ;
- valider le parcours utilisateur : accueil, clic, page, retour ;
- garder le composant commun tant que les vraies pages ne sont pas encore separees.

## 8. Choix de design

Le style actuel cherche surtout a rendre le parcours testable :

- interface tactile ;
- zones de clic grandes ;
- disposition proche du croquis initial ;
- boutons separes selon leur role ;
- version compacte pour verifier le comportement sur ecran etroit.

Palette actuelle :

| Couleur | Usage |
| --- | --- |
| Vert clair | Action principale `CAISSE`. |
| Jaune clair | Actions d'attente / encaissement. |
| Rouge clair | Action de sortie. |
| Gris bleu | Navigation secondaire. |

Les couleurs sont definies dans `Color.kt` pour pouvoir les changer sans modifier toute l'interface.

## 9. Validation locale

Etat verifie initialement le `15-06-2026`, puis complete le `18-06-2026` :

| Element | Resultat |
| --- | --- |
| Emulateur | `Pixel Tablet API 35`. |
| Version Android testee | Android 15 / API 35. |
| Lancement application | Reussi. |
| Navigation accueil vers pages temporaires | Reussie pour les boutons testes. |
| Retour vers accueil | Reussi via le bouton `Retour`. |
| Caisse V1 | Disponible via le bouton `CAISSE`. |
| Historique local | Disponible via le bouton `Historique`. |
| Statistiques | Calculs par journee ou periode disponibles. |
| Prix | Modification locale persistante disponible. |
| Cloture | Clotures journalieres, mensuelles et annuelles consultables, imprimables et exportables en PDF. |

Cette validation confirme que le socle actuel fonctionne sur une configuration proche de la
tablette cible.

## 10. Etat actuel

Ce qui est implemente depuis l'accueil :

- bouton `CAISSE` vers `RegisterScreen` ;
- bouton `A encaisser sur place / a emporter` vers les commandes correspondantes en attente ;
- bouton `A encaisser livraison` vers les livraisons en attente ;
- bouton `Historique` vers l'historique local Room ;
- bouton `Statistiques` vers les indicateurs filtres par date ;
- bouton `Prix` vers la gestion persistante des tarifs ;
- bouton `Cloture` vers les tickets Z journaliers, mensuels et annuels, avec export PDF.

La caisse V1 et son fonctionnement sont documentes dans `docs/implementation_caisse_android.md`.
Le catalogue Milano Gusto est documente dans `docs/menu_milano_gusto_v1.md`.

## 11. Limites actuelles

Ce qui n'est pas encore implemente :

- navigation finale avec `navigation-compose` ;
- parametres finalises si le besoin revient dans un ecran dedie ;
- tests automatises d'interface.

L'accueil est donc maintenant une base de navigation fonctionnelle. Le bouton `Quitter` a ete retire
pour ne pas confondre fermeture Android et cloture de caisse.

## 12. Prochaines etapes recommandees

1. Garder `CaisseApp` comme point de coordination tant que la navigation reste simple.
2. Remplacer par `navigation-compose` seulement si les parcours deviennent difficiles a maintenir.
3. Valider les pages `Statistiques`, `Prix` et `Cloture`.
4. Tester sur la tablette Android 15 reelle.
