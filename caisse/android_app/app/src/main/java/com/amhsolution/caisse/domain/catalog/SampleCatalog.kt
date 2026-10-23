package com.amhsolution.caisse.domain.catalog

import com.amhsolution.caisse.domain.model.Product
import com.amhsolution.caisse.domain.model.ProductCategory

/**
 * Catalogue caisse Kebaba — source : règle `carte-kebaba`
 * (PDF `1M_50_JEAN_JAURÈS_MENU_KEBABA.pdf`).
 * Prix = TTC en centimes, **seul** (menu via [KebabaOptionCatalog.menuPriceCentimes]).
 */
object SampleCatalog {
    val products: List<Product> = listOf(
        // Klassik
        Product("kebaba-klassik", "Kebaba Klassik", ProductCategory.KLASSIK, 800),

        // 'Dwich
        Product("kofte", "Kofte", ProductCategory.DWICH, 950),
        Product("poulet", "Poulet", ProductCategory.DWICH, 950),
        Product("vege", "Végé", ProductCategory.DWICH, 950),
        Product("ricain", "Ricain", ProductCategory.DWICH, 950),
        Product("sucuk", "Sucuk", ProductCategory.DWICH, 950),
        Product("adana", "Adana", ProductCategory.DWICH, 1050),

        // Kréations
        Product("krousty", "Krousty", ProductCategory.KREATIONS, 1100),
        Product("avocado-fromage-frais", "Avocado Fromage Frais", ProductCategory.KREATIONS, 1100),
        Product("cheezy-jalapanos", "Cheezy Jalapanos", ProductCategory.KREATIONS, 1100),
        Product("boursin-lardons", "Boursin Lardons", ProductCategory.KREATIONS, 1100),
        Product("monster", "Monster", ProductCategory.KREATIONS, 1100),
        Product("chevre-miel", "Chèvre-Miel", ProductCategory.KREATIONS, 1100),

        // Assiettes
        Product("assiette-klassik", "Assiette Klassik", ProductCategory.ASSIETTES, 1500),
        Product("assiette-poulet", "Assiette Poulet", ProductCategory.ASSIETTES, 1500),
        Product("assiette-kofte", "Assiette Kofte", ProductCategory.ASSIETTES, 1500),
        Product("assiette-vege", "Assiette Végé", ProductCategory.ASSIETTES, 1400),
        Product("assiette-ricain", "Assiette Ricain", ProductCategory.ASSIETTES, 1500),
        Product("assiette-adana", "Assiette Adana", ProductCategory.ASSIETTES, 1700),
        Product("assiette-sucuk", "Assiette Sucuk", ProductCategory.ASSIETTES, 1500),
        Product("assiette-royal", "Assiette Royal", ProductCategory.ASSIETTES, 1900),

        // Bowls
        Product("bowl-klassik", "Bowl Klassik", ProductCategory.BOWLS, 1200),
        Product("bowl-chevre-miel", "Bowl Chèvre-Miel", ProductCategory.BOWLS, 1350),
        Product("bowl-cheezy-jalapanos", "Bowl Cheezy Jalapanos", ProductCategory.BOWLS, 1350),
        Product("bowl-boursin-lardons", "Bowl Boursin Lardons", ProductCategory.BOWLS, 1350),
        Product("bowl-avocado-fromage-frais", "Bowl Avocado Fromage Frais", ProductCategory.BOWLS, 1350),

        // Takos
        Product("takos-1-viande", "Takos 1 viande", ProductCategory.TAKOS, 800),
        Product("takos-2-viandes", "Takos 2 viandes", ProductCategory.TAKOS, 1000),
        Product("takos-3-viandes", "Takos 3 viandes", ProductCategory.TAKOS, 1200),

        // Menus
        Product("menu-etudiant", "Menu Étudiant", ProductCategory.MENUS, 800),
        Product("menu-enfant", "Menu Enfant", ProductCategory.MENUS, 700),

        // P'tit +
        Product("chicken-wings-x4", "Chicken Wings ×4", ProductCategory.PTIT_PLUS, 690),
        Product("chicken-wings-x8", "Chicken Wings ×8", ProductCategory.PTIT_PLUS, 990),
        Product("tenders-x4", "Tenders ×4", ProductCategory.PTIT_PLUS, 690),
        Product("tenders-x8", "Tenders ×8", ProductCategory.PTIT_PLUS, 990),
        Product("camembert-x4", "Camembert ×4", ProductCategory.PTIT_PLUS, 190),
        Product("camembert-x8", "Camembert ×8", ProductCategory.PTIT_PLUS, 290),
        Product("oignon-rings-x4", "Oignon Rings ×4", ProductCategory.PTIT_PLUS, 190),
        Product("oignon-rings-x8", "Oignon Rings ×8", ProductCategory.PTIT_PLUS, 290),
        Product("mozza-sticks-x4", "Mozza Sticks ×4", ProductCategory.PTIT_PLUS, 390),
        Product("mozza-sticks-x8", "Mozza Sticks ×8", ProductCategory.PTIT_PLUS, 690),
        Product("nuggets-x4", "Nuggets ×4", ProductCategory.PTIT_PLUS, 390),
        Product("nuggets-x8", "Nuggets ×8", ProductCategory.PTIT_PLUS, 690),
        Product("jalapenos-cheezy-x4", "Jalapenos Cheezy ×4", ProductCategory.PTIT_PLUS, 390),
        Product("jalapenos-cheezy-x8", "Jalapenos Cheezy ×8", ProductCategory.PTIT_PLUS, 690),

        // Barquettes
        Product("barquette-kebab-frites-petite", "Barquette Kebab + Frites petite", ProductCategory.BARQUETTES, 500),
        Product("barquette-kebab-frites-grande", "Barquette Kebab + Frites grande", ProductCategory.BARQUETTES, 700),
        Product("barquette-viande-kebab-petite", "Barquette Viande de Kebab petite", ProductCategory.BARQUETTES, 600),
        Product("barquette-viande-kebab-grande", "Barquette Viande de Kebab grande", ProductCategory.BARQUETTES, 900),

        // Frites / boulgour
        Product("frites-petite", "Frites petite", ProductCategory.FRITES, 350),
        Product("frites-grande", "Frites grande", ProductCategory.FRITES, 500),
        Product("boulgour-petite", "Boulgour petite", ProductCategory.FRITES, 350),
        Product("boulgour-grande", "Boulgour grande", ProductCategory.FRITES, 500),
        Product("frites-cheddar", "Frites Cheddar", ProductCategory.FRITES, 500),
        Product("frites-cheddar-oignon-frit", "Frites Cheddar Oignon Frit", ProductCategory.FRITES, 550),
        Product("frites-cheddar-bacon", "Frites Cheddar Bacon", ProductCategory.FRITES, 600),

        // Desserts
        Product("tarte-daim", "Tarte Daim", ProductCategory.DESSERTS, 350),
        Product("tiramisu", "Tiramisu", ProductCategory.DESSERTS, 400),

        // Boissons
        Product("coca-cola-33", "Coca Cola 33 cl", ProductCategory.BOISSONS, 200),
        Product("coca-cola-cherry-33", "Coca Cola Cherry 33 cl", ProductCategory.BOISSONS, 200),
        Product("oasis-pomme-cassis-framboise-33", "Oasis Pomme Cassis Framboise 33 cl", ProductCategory.BOISSONS, 200),
        Product("oasis-tropical-33", "Oasis tropical 33 cl", ProductCategory.BOISSONS, 200),
        Product("fanta-orange-33", "Fanta orange 33 cl", ProductCategory.BOISSONS, 200),
        Product("tropico-orange-ananas-33", "Tropico orange ananas 33 cl", ProductCategory.BOISSONS, 200),
        Product("perrier-33", "Perrier 33 cl", ProductCategory.BOISSONS, 200),
        Product("lipton-ice-tea-peche-33", "Lipton ice tea pêche 33 cl", ProductCategory.BOISSONS, 200),
        Product("orangina-33", "Orangina 33 cl", ProductCategory.BOISSONS, 200),
        Product("ayran-25", "Ayran 25 cl", ProductCategory.BOISSONS, 200),
        Product("eau-50", "Eau 50 cl", ProductCategory.BOISSONS, 200),
        Product("cafe", "Café", ProductCategory.BOISSONS, 150)
    )

    fun productsFor(category: ProductCategory): List<Product> =
        products.filter { it.category == category }
}
