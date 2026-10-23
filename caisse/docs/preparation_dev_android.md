# Préparation environnement Android / Kotlin

Date : `04-06-2026`
Mise à jour : `18-06-2026`

Objectif : vérifier que l'environnement de travail est prêt avant de commencer le développement
Kotlin / Jetpack Compose du logiciel de caisse.

## 1. État de l'environnement

| Élément | État |
| --- | --- |
| Mac | Apple Silicon `arm64` |
| macOS | `26.5.1` |
| Android Studio | Installé : `Android Studio 2026.1` |
| Projet Android | Créé dans `android_app/` |
| SDK Android | Installé dans `/Users/enzo/Library/Android/sdk` |
| Platform Tools / `adb` | Installé : `Android Debug Bridge 37.0.0` |
| Appareil de test | Émulateur `Pixel Tablet API 35` créé et testé |
| Git | Installé : `git version 2.50.1` |
| Langage | Kotlin |
| Interface | Jetpack Compose |
| Stockage local | Room pour l'historique, DataStore pour les prix modifies |
| Minimum SDK | API 26 |
| Compile SDK | Android 15 / API 35 |
| Target SDK | Android 15 / API 35 |
| Gradle JVM | JDK 21 recommandé |
| Package | `com.amhsolution.caisse` |
| Dépôt Git distant | Connecté au repo privé GitHub |

## 2. Ce qui est déjà prêt

- projet Android généré par Android Studio ;
- structure Gradle Kotlin DSL ;
- application Compose ;
- historique local Room ;
- surcharges de prix locales avec DataStore ;
- configuration Android SDK ;
- `adb` fonctionne ;
- émulateur tablette Android 15 disponible : `Pixel Tablet API 35` ;
- lancement de l'application validé sur l'émulateur ;
- documents de cadrage dans `docs/` ;
- croquis V2 dans `docs/croquis/wireframes_v2/`.

## 3. Configuration Android actuelle

| Élément | Valeur |
| --- | --- |
| Nom projet | `AMH Caisse` |
| Module | `app` |
| `applicationId` | `com.amhsolution.caisse` |
| `namespace` | `com.amhsolution.caisse` |
| `minSdk` | `26` |
| `targetSdk` | `35` |
| `compileSdk` | `35` |
| Gradle wrapper | `8.11.1` |
| Android Gradle Plugin | `8.9.1` |
| Kotlin | `2.0.21` |
| Compose BOM | `2024.10.01` |

## 4. Dépendances déjà présentes

| Dépendance | Rôle |
| --- | --- |
| `androidx.core:core-ktx` | Extensions Kotlin Android |
| `androidx.activity:activity-compose` | Lancer Compose depuis l'activité Android |
| `androidx.compose.ui:ui` | Base interface Compose |
| `androidx.compose.material3:material3` | Composants visuels Material 3 |
| `androidx.lifecycle:lifecycle-runtime-ktx` | Cycle de vie Android |
| `androidx.lifecycle:lifecycle-runtime-compose` | Observation d'etat compatible Compose |
| `androidx.room:room-runtime` | Base locale SQLite via Room |
| `androidx.room:room-ktx` | API Kotlin/coroutines pour Room |
| `androidx.room:room-compiler` | Generation du code Room via KSP |
| `com.google.devtools.ksp` | Traitement d'annotations pour Room |
| `androidx.datastore:datastore-preferences` | Persistence legere des prix modifies |
| `junit` | Tests unitaires |
| `androidx.test` / `espresso` | Tests Android instrumentés |

Ces dépendances suffisent pour la V1 actuelle : interface Compose, calculs metier simples et
historique local persistant.

## 5. Dépendances à ajouter plus tard

À ajouter seulement quand on en a besoin :

- `navigation-compose` quand il y aura plusieurs écrans reliés ;
- `lifecycle-viewmodel-compose` si on sépare l'état d'écran dans des ViewModels ;
- tests unitaires supplementaires sur les calculs métier ;
- éventuellement une couche d'impression après choix du matériel.

## 6. Contraintes projet à respecter

| Contrainte | Conséquence technique |
| --- | --- |
| Application locale Android | Pas de dépendance à un serveur pour encaisser |
| Livraison APK | Projet Android natif installable sur terminal ou tablette |
| Logiciel non certifié pour l'instant | Ne pas promettre d'usage légal en production sans validation |
| Paiement mixte | Une commande peut avoir plusieurs paiements |
| Espèces avec rendu monnaie | Stocker aussi montant reçu et rendu |
| Montants en centimes | Utiliser des entiers, pas des nombres décimaux flottants |
| Tickets client et cuisine | Prévoir un type de ticket ou deux sorties d'impression |
| Impression matérielle inconnue | Ne pas coder l'impression avant validation du matériel |
| TPE autonome | Le paiement CB est confirmé manuellement dans l'application |
| Documentation continue | Chaque phase de code doit être documentée dans `docs/` quand elle concerne le projet |

## 7. Points à compléter avant une vraie livraison

- tester l'installation APK sur la tablette Android 15 réelle ;
- choisir l'imprimante ticket client ;
- choisir l'imprimante cuisine ou confirmer qu'elle n'est pas nécessaire ;
- vérifier le mode de connexion imprimante : USB, Bluetooth, réseau ou intégrée ;
- installer les Android Command-line Tools si l'on veut utiliser `sdkmanager` en terminal ;
- definir les prochains parametres a conserver dans DataStore ;
- persister aussi les commandes en attente, pas seulement l'historique ;
- structurer les corrections/annulations avec une trace comptable.

## 8. Priorités de codage

1. Stabiliser la caisse V1 avec le vrai catalogue Milano Gusto.
2. Tester paysage et portrait sur tablette Android 15.
3. Persister les commandes en attente dans Room.
4. Valider les pages statistiques et prix, puis finaliser les paramètres.
5. Ajouter les tests unitaires sur paiement, historique et tickets.
6. Préparer l'impression quand le matériel est validé.
7. Sortir le catalogue du code si plusieurs restaurants doivent être gérés.

## 9. Règles de qualité

- stocker les montants en centimes sous forme d'entiers ;
- éviter de mélanger l'interface et les calculs métier ;
- documenter chaque phase de code ;
- faire des commits courts et compréhensibles ;
- ne pas ajouter de dépendance sans raison claire ;
- tester les calculs critiques avant de brancher l'impression.

## 10. État Git

Le dépôt distant est connecté et `android_app/` est maintenant versionné.

État actuel :

- le socle Android Compose est déjà poussé sur `main` ;
- l'écran d'accueil est présent ;
- la caisse V1 comprend catalogue, panier, encaissement, historique, statistiques et prix modifiables ;
- l'émulateur `Pixel Tablet API 35` a permis de valider le lancement ;
- les fichiers personnels de rapport et journaux restent ignorés par Git.

## 11. Documentation d'implémentation

Les documentations techniques de code sont disponibles ici :

- [implementation_accueil_android.md](implementation_accueil_android.md)
- [implementation_caisse_android.md](implementation_caisse_android.md)
- [menu_milano_gusto_v1.md](menu_milano_gusto_v1.md)

Elles décrivent l'accueil Compose, la caisse V1, le catalogue Milano Gusto, les choix de design,
la configuration Android et les limites actuelles de l'implémentation.
