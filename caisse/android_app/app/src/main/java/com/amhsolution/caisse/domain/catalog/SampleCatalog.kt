package com.amhsolution.caisse.domain.catalog

import com.amhsolution.caisse.domain.model.Product
import com.amhsolution.caisse.domain.model.ProductCategory

/**
 * Catalogue caisse Kebaba — source : règle `carte-kebaba`
 * (PDF `1M_50_JEAN_JAURÈS_MENU_KEBABA.pdf`).
 * Prix = TTC en centimes, **seul** ; menu + flags via [ProductCatalogDefaults].
 */
object SampleCatalog {
    val products: List<Product> = listOf(
        // Klassik
        ProductCatalogDefaults.product("kebaba-klassik", "Kebaba Klassik", ProductCategory.KLASSIK, 800),

        // 'Dwich
        ProductCatalogDefaults.product("kofte", "Kofte", ProductCategory.DWICH, 950),
        ProductCatalogDefaults.product("poulet", "Poulet", ProductCategory.DWICH, 950),
        ProductCatalogDefaults.product("vege", "Végé", ProductCategory.DWICH, 950),
        ProductCatalogDefaults.product("ricain", "Ricain", ProductCategory.DWICH, 950),
        ProductCatalogDefaults.product("sucuk", "Sucuk", ProductCategory.DWICH, 950),
        ProductCatalogDefaults.product("adana", "Adana", ProductCategory.DWICH, 1050),

        // Kréations
        ProductCatalogDefaults.product("krousty", "Krousty", ProductCategory.KREATIONS, 1100),
        ProductCatalogDefaults.product("avocado-fromage-frais", "Avocado Fromage Frais", ProductCategory.KREATIONS, 1100),
        ProductCatalogDefaults.product("cheezy-jalapanos", "Cheezy Jalapanos", ProductCategory.KREATIONS, 1100),
        ProductCatalogDefaults.product("boursin-lardons", "Boursin Lardons", ProductCategory.KREATIONS, 1100),
        ProductCatalogDefaults.product("monster", "Monster", ProductCategory.KREATIONS, 1100),
        ProductCatalogDefaults.product("chevre-miel", "Chèvre-Miel", ProductCategory.KREATIONS, 1100),

        // Assiettes
        ProductCatalogDefaults.product("assiette-klassik", "Assiette Klassik", ProductCategory.ASSIETTES, 1500),
        ProductCatalogDefaults.product("assiette-poulet", "Assiette Poulet", ProductCategory.ASSIETTES, 1500),
        ProductCatalogDefaults.product("assiette-kofte", "Assiette Kofte", ProductCategory.ASSIETTES, 1500),
        ProductCatalogDefaults.product("assiette-vege", "Assiette Végé", ProductCategory.ASSIETTES, 1400),
        ProductCatalogDefaults.product("assiette-ricain", "Assiette Ricain", ProductCategory.ASSIETTES, 1500),
        ProductCatalogDefaults.product("assiette-adana", "Assiette Adana", ProductCategory.ASSIETTES, 1700),
        ProductCatalogDefaults.product("assiette-sucuk", "Assiette Sucuk", ProductCategory.ASSIETTES, 1500),
        ProductCatalogDefaults.product("assiette-royal", "Assiette Royal", ProductCategory.ASSIETTES, 1900),

        // Bowls
        ProductCatalogDefaults.product("bowl-klassik", "Bowl Klassik", ProductCategory.BOWLS, 1200),
        ProductCatalogDefaults.product("bowl-chevre-miel", "Bowl Chèvre-Miel", ProductCategory.BOWLS, 1350),
        ProductCatalogDefaults.product("bowl-cheezy-jalapanos", "Bowl Cheezy Jalapanos", ProductCategory.BOWLS, 1350),
        ProductCatalogDefaults.product("bowl-boursin-lardons", "Bowl Boursin Lardons", ProductCategory.BOWLS, 1350),
        ProductCatalogDefaults.product("bowl-avocado-fromage-frais", "Bowl Avocado Fromage Frais", ProductCategory.BOWLS, 1350),

        // Takos
        ProductCatalogDefaults.product("takos-1-viande", "Takos 1 viande", ProductCategory.TAKOS, 800),
        ProductCatalogDefaults.product("takos-2-viandes", "Takos 2 viandes", ProductCategory.TAKOS, 1000),
        ProductCatalogDefaults.product("takos-3-viandes", "Takos 3 viandes", ProductCategory.TAKOS, 1200),

        // Menus
        ProductCatalogDefaults.product("menu-etudiant", "Menu Étudiant", ProductCategory.MENUS, 800),
        ProductCatalogDefaults.product("menu-enfant", "Menu Enfant", ProductCategory.MENUS, 700),

        // P'tit +
        ProductCatalogDefaults.product("chicken-wings-x4", "Chicken Wings ×4", ProductCategory.PTIT_PLUS, 690),
        ProductCatalogDefaults.product("chicken-wings-x8", "Chicken Wings ×8", ProductCategory.PTIT_PLUS, 990),
        ProductCatalogDefaults.product("tenders-x4", "Tenders ×4", ProductCategory.PTIT_PLUS, 690),
        ProductCatalogDefaults.product("tenders-x8", "Tenders ×8", ProductCategory.PTIT_PLUS, 990),
        ProductCatalogDefaults.product("camembert-x4", "Camembert ×4", ProductCategory.PTIT_PLUS, 190),
        ProductCatalogDefaults.product("camembert-x8", "Camembert ×8", ProductCategory.PTIT_PLUS, 290),
        ProductCatalogDefaults.product("oignon-rings-x4", "Oignon Rings ×4", ProductCategory.PTIT_PLUS, 190),
        ProductCatalogDefaults.product("oignon-rings-x8", "Oignon Rings ×8", ProductCategory.PTIT_PLUS, 290),
        ProductCatalogDefaults.product("mozza-sticks-x4", "Mozza Sticks ×4", ProductCategory.PTIT_PLUS, 390),
        ProductCatalogDefaults.product("mozza-sticks-x8", "Mozza Sticks ×8", ProductCategory.PTIT_PLUS, 690),
        ProductCatalogDefaults.product("nuggets-x4", "Nuggets ×4", ProductCategory.PTIT_PLUS, 390),
        ProductCatalogDefaults.product("nuggets-x8", "Nuggets ×8", ProductCategory.PTIT_PLUS, 690),
        ProductCatalogDefaults.product("jalapenos-cheezy-x4", "Jalapenos Cheezy ×4", ProductCategory.PTIT_PLUS, 390),
        ProductCatalogDefaults.product("jalapenos-cheezy-x8", "Jalapenos Cheezy ×8", ProductCategory.PTIT_PLUS, 690),

        // Barquettes
        ProductCatalogDefaults.product("barquette-kebab-frites-petite", "Barquette Kebab + Frites petite", ProductCategory.BARQUETTES, 500),
        ProductCatalogDefaults.product("barquette-kebab-frites-grande", "Barquette Kebab + Frites grande", ProductCategory.BARQUETTES, 700),
        ProductCatalogDefaults.product("barquette-viande-kebab-petite", "Barquette Viande de Kebab petite", ProductCategory.BARQUETTES, 600),
        ProductCatalogDefaults.product("barquette-viande-kebab-grande", "Barquette Viande de Kebab grande", ProductCategory.BARQUETTES, 900),

        // Frites / boulgour
        ProductCatalogDefaults.product("frites-petite", "Frites petite", ProductCategory.FRITES, 350),
        ProductCatalogDefaults.product("frites-grande", "Frites grande", ProductCategory.FRITES, 500),
        ProductCatalogDefaults.product("boulgour-petite", "Boulgour petite", ProductCategory.FRITES, 350),
        ProductCatalogDefaults.product("boulgour-grande", "Boulgour grande", ProductCategory.FRITES, 500),
        ProductCatalogDefaults.product("frites-cheddar", "Frites Cheddar", ProductCategory.FRITES, 500),
        ProductCatalogDefaults.product("frites-cheddar-oignon-frit", "Frites Cheddar Oignon Frit", ProductCategory.FRITES, 550),
        ProductCatalogDefaults.product("frites-cheddar-bacon", "Frites Cheddar Bacon", ProductCategory.FRITES, 600),

        // Desserts
        ProductCatalogDefaults.product("tarte-daim", "Tarte Daim", ProductCategory.DESSERTS, 350),
        ProductCatalogDefaults.product("tiramisu", "Tiramisu", ProductCategory.DESSERTS, 400),

        // Boissons
        ProductCatalogDefaults.product("coca-cola-33", "Coca Cola 33 cl", ProductCategory.BOISSONS, 200),
        ProductCatalogDefaults.product("coca-cola-cherry-33", "Coca Cola Cherry 33 cl", ProductCategory.BOISSONS, 200),
        ProductCatalogDefaults.product("oasis-pomme-cassis-framboise-33", "Oasis Pomme Cassis Framboise 33 cl", ProductCategory.BOISSONS, 200),
        ProductCatalogDefaults.product("oasis-tropical-33", "Oasis tropical 33 cl", ProductCategory.BOISSONS, 200),
        ProductCatalogDefaults.product("fanta-orange-33", "Fanta orange 33 cl", ProductCategory.BOISSONS, 200),
        ProductCatalogDefaults.product("tropico-orange-ananas-33", "Tropico orange ananas 33 cl", ProductCategory.BOISSONS, 200),
        ProductCatalogDefaults.product("perrier-33", "Perrier 33 cl", ProductCategory.BOISSONS, 200),
        ProductCatalogDefaults.product("lipton-ice-tea-peche-33", "Lipton ice tea pêche 33 cl", ProductCategory.BOISSONS, 200),
        ProductCatalogDefaults.product("orangina-33", "Orangina 33 cl", ProductCategory.BOISSONS, 200),
        ProductCatalogDefaults.product("ayran-25", "Ayran 25 cl", ProductCategory.BOISSONS, 200),
        ProductCatalogDefaults.product("eau-50", "Eau 50 cl", ProductCategory.BOISSONS, 200),
        ProductCatalogDefaults.product("cafe", "Café", ProductCategory.BOISSONS, 150)
    )

    fun productsFor(category: ProductCategory): List<Product> =
        products.filter { it.category == category && it.enabled }
}
