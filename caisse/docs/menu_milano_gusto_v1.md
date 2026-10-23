# Menu Milano Gusto - V1 caisse

Ce document trace le catalogue utilisé dans la V1 locale de la caisse.
Les prix viennent des flyers définitifs `flyer-recto.pdf` et `flyer-verso.pdf` rangés dans
`assets/menu/flyers/current/`.

## Hypothèses

- La Margherita utilise les tarifs `S 8,00 EUR`, `M 10,00 EUR` et `L 13,00 EUR`.
- Toutes les autres pizzas utilisent les tarifs `S 11,00 EUR`, `M 14,00 EUR` et `L 18,00 EUR`.
- Chaque supplément pizza vaut `1,00 EUR` par défaut. Ce tarif peut être modifié ingrédient par
  ingrédient dans l'onglet `Prix > Suppléments pizza`.
- Les suppléments burger, focaccia, pasta et dessert valent aussi `1,00 EUR` par défaut et sont
  modifiables par catégorie dans l'onglet `Prix`.
- L'option `Le menu` vaut `2,90 EUR` et s'applique aux burgers et aux sandwichs focaccia.
- Pour chaque catégorie, la liste des suppléments correspond à l'union des ingrédients de tous ses
  produits. Un produit conserve sa recette par défaut, dont chaque ingrédient peut être retiré.
- Les canettes sont saisies a `1,50 EUR` selon l'information transmise le 17/07/2026.
- Les bouteilles `1,5 L` sont creees sans prix de vente initial. Elles restent non vendables tant
  que le restaurateur n'a pas enregistre leur tarif dans l'onglet `Prix`.

## Catalogue saisi

| Catégorie | Produit | Prix |
| --- | --- | ---: |
| Burgers | Il Classico | 8,50 EUR |
| Burgers | Il Capitano | 9,90 EUR |
| Burgers | Il Gigante | 10,50 EUR |
| Burgers | Il Verde | 9,00 EUR |
| Focaccia | Classico | 7,90 EUR |
| Focaccia | Parma | 8,90 EUR |
| Focaccia | Le Truffé | 8,90 EUR |
| Focaccia | Nordico | 10,50 EUR |
| Pasta | Salmone | 12,50 EUR |
| Pasta | Carbonara | 11,00 EUR |
| Pasta | Bolognese | 11,00 EUR |
| Pasta | Alfredo | 11,00 EUR |
| Boissons | Coca - ref. B1247 DPS | 1,50 EUR |
| Boissons | Coca Zéro - ref. B0626 DPS | 1,50 EUR |
| Boissons | Coca Cherry - ref. B0547 DPS | 1,50 EUR |
| Boissons | Fanta - ref. B0868 IBCO | 1,50 EUR |
| Boissons | Sprite - ref. B1130 DPS | 1,50 EUR |
| Boissons | Ice Tea - ref. B1436 DPS | 1,50 EUR |
| Boissons | Oasis - ref. B0926 DPS | 1,50 EUR |
| Boissons | Eau - ref. B0937 DPS | 1,50 EUR |
| Boissons | Perrier - ref. B0938 DPS | 1,50 EUR |
| Boissons | Pepsi 1,5 L - ref. B0692 DPS | À définir |
| Boissons | Fanta 1,5 L - ref. B1037 DPS | À définir |
| Desserts | Tagliacrêpe Nutella | 4,50 EUR |
| Desserts | Tagliacrêpe Nutella Banane | 5,50 EUR |
| Desserts | Tagliacrêpe Fraise Nutella | 5,50 EUR |
| Desserts | Tagliacrêpe Nutella Fraise Banane | 6,90 EUR |
| Desserts | Tagliacrêpe Dubai | 7,90 EUR |
| Desserts | Tiramisù Original | 4,50 EUR |
| Desserts | Panna Cotta Fraise | 4,50 EUR |
| Bambino | Menu Bambino | 7,50 EUR |
| Offres spéciales | Duo Junior : 2 pizzas Junior + 2 canettes | 22,00 EUR |
| Offres spéciales | Trio Junior : 3 pizzas Junior + bouteille 1,5 L | 30,00 EUR |
| Offres spéciales | Duo Senior : 2 pizzas Senior + bouteille 1,5 L | 26,00 EUR |
| Offres spéciales | Trio Senior : 3 pizzas Senior + bouteille 1,5 L | 39,00 EUR |

## Pizzas

La valeur enregistrée sur chaque produit pizza est le prix S. Les boutons de taille affichés dans
la caisse sont `S`, `M` et `L`. Pour la Margherita, M et L ajoutent `2,00 EUR` et `5,00 EUR` ; pour
les autres pizzas, ils ajoutent `3,00 EUR` et `7,00 EUR`.

| Base | Pizza | Composition par défaut |
| --- | --- | --- |
| Tomate | 4 Formaggi | Mozzarella, bleu, chèvre, emmental |
| Tomate | Tonno | Mozzarella, thon, oignons, olives |
| Tomate | Rustica | Mozzarella, viande hachée, poivrons, œuf |
| Tomate | Frutti del mare | Mozzarella, fruits de mer |
| Tomate | Il Basilico | Mozzarella, pepperoni, basilic |
| Tomate | Verdura | Mozzarella, poivrons, champignons, oignons, olives |
| Tomate | Margherita | Mozzarella |
| Crème | Boursino | Mozzarella, viande hachée, boursin, œuf |
| Crème | Tartiflegna | Mozzarella, jambon, lardons, oignons, pommes de terre, reblochon |
| Crème | Montanara | Mozzarella, jambon, pommes de terre, raclette, oignons |
| Crème | Polo capra | Mozzarella, poulet, chèvre, oignons, persil |
| Crème | Capramiele | Mozzarella, chèvre, miel |
| Crème | Norvegese | Mozzarella, saumon, persil |

Lors de la prise de commande, une pizza peut être personnalisée sans modifier la recette de
référence : changement de base, retrait d'un ingrédient inclus et ajout d'un supplément. La liste
des suppléments est volontairement limitée à l'union des ingrédients présents sur cette carte.

## Personnalisation des autres produits

Les burgers, focaccias, pâtes et desserts suivent la même logique métier :

- la recette du produit est affichée par défaut ;
- chaque ingrédient inclus peut être retiré ;
- les suppléments proposés sont tous les ingrédients de la même catégorie ;
- leur prix est ajouté immédiatement au prix de la ligne et conservé dans le ticket.

Les burgers et sandwichs focaccia ont en plus une option `Le menu` à `2,90 EUR`. Techniquement,
elle est enregistrée comme un supplément spécial de ligne (`option-menu`) pour rester compatible
avec l'historique, les tickets et l'impression sans créer un produit artificiel séparé.

Les recettes et unions d'ingrédients sont centralisées dans `ProductOptionCatalog.kt`. Les offres
pizza disposent d'un configurateur séparé qui demande deux ou trois pizzas selon l'offre choisie.

## Sandwichs focaccia

- Classico : mozza, tomates fraîches, pesto, basilic.
- Parma : jambon, ricotta, pesto, crème de balsamique.
- Le Truffé : bacon de dinde, tomate fraîche, roquette, mayonnaise à la truffe.
- Nordico : saumon fumé, fromage frais aux herbes, concombre, aneth, citron.

## Images

Les flyers définitifs de référence sont rangés dans :

- `assets/menu/flyers/current/flyer-recto.pdf`
- `assets/menu/flyers/current/flyer-verso.pdf`

L'ancienne carte est conservée en archive pour traçabilité :

- `assets/menu/archive/ancienne-carte-2026-06/menu-pizzas-base-tomate.jpg`
- `assets/menu/archive/ancienne-carte-2026-06/menu-pizzas-base-creme.jpg`
- `assets/menu/archive/ancienne-carte-2026-06/menu-formules-pizza.jpg`
- `assets/menu/archive/ancienne-carte-2026-06/menu-focaccia.jpg`
- `assets/menu/archive/ancienne-carte-2026-06/menu-pasta.jpg`
- `assets/menu/archive/ancienne-carte-2026-06/menu-desserts.jpg`

Versions site :

- `assets/menu/site/menu-focaccia-site.jpg`
- `assets/menu/site/menu-pasta-site.jpg`
- `assets/menu/site/menu-desserts-site.jpg`

Versions carrées de travail pour plateformes :

- `assets/menu/delivery/menu-focaccia-square.jpg`
- `assets/menu/delivery/menu-pasta-square.jpg`
- `assets/menu/delivery/menu-desserts-square.jpg`

Ces versions carrées sont des assets temporaires basés sur les affiches.
Pour Uber Eats / Deliveroo, il faudra idéalement produire une vraie photo produit par article, sans texte, sans prix et sans montage graphique.

## Implémentation dans l'application

Le catalogue de reference est saisi dans :

- `android_app/app/src/main/java/com/amhsolution/caisse/domain/catalog/SampleCatalog.kt`
- `android_app/app/src/main/java/com/amhsolution/caisse/domain/catalog/PizzaCatalog.kt`
- `android_app/app/src/main/java/com/amhsolution/caisse/domain/catalog/ProductOptionCatalog.kt`
- `android_app/app/src/main/java/com/amhsolution/caisse/domain/model/ProductCategory.kt`

Les prix sont stockés en centimes :

- `8,50 EUR` devient `850`
- `11,00 EUR` devient `1100`
- `3,50 EUR` devient `350`

Ce choix évite les erreurs d'arrondi et simplifie les calculs de panier, de paiement et d'historique.

Depuis le push du 18/06/2026, la page `Prix` permet de modifier un tarif sans recompiler
l'application. `CatalogRepository` enregistre ces surcharges dans DataStore et les applique au
catalogue de reference. Un identifiant produit doit donc rester stable pour conserver son prix
personnalise.

Cette logique s'applique aussi aux boissons. Une bouteille dont le prix initial vaut `0` apparait
comme `Prix a definir` dans la caisse et son bouton est desactive. L'enregistrement d'un prix
positif dans `Prix > Boissons` la rend immediatement vendable.

Pour modifier la carte V1 :

1. ajouter ou modifier la catégorie dans `ProductCategory.kt` si besoin ;
2. ajouter ou modifier les `Product(...)` dans `SampleCatalog.kt` ;
3. garder un `id` stable, en minuscules, sans espace ;
4. garder le prix en centimes ;
5. mettre à jour ce document pour conserver la trace de la source et des hypothèses.

Pour une pizza, ajouter aussi sa recette dans `PizzaCatalog.recipes`. Tout nouvel ingrédient doit
être ajouté une seule fois dans `PizzaCatalog.ingredients`; il devient alors disponible comme
supplément et son prix apparaît automatiquement dans l'onglet `Prix`.

Pour un burger, une focaccia, une pasta ou un dessert, ajouter sa recette et ses ingrédients dans
`ProductOptionCatalog`. L'identifiant de recette doit être strictement identique à l'identifiant du
produit dans `SampleCatalog`.

À terme, les noms, catégories et produits devront aussi sortir du code source pour adapter la caisse
à un autre restaurant sans recompiler l'application.

## Points à valider

- Valider officiellement le prix des 4 nouveaux sandwichs focaccia.
- Confirmer que la reference B1037 correspond bien a `Fanta 1,5 L` et non a `Orangina 1,5 L`.
- Confirmer si les offres Senior visibles sur le support doivent être intégrées après la V1 Junior.
- Valider l'orthographe commerciale `Tartiflegna`.
- Remplacer les visuels temporaires Uber Eats / Deliveroo par des photos produit propres.
