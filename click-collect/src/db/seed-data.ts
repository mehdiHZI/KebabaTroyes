/** Seed catalogue — source : règle Cursor carte-kebaba (centimes TTC). */

export const seedCategories = [
  { id: "klassik", name: "Kebaba Klassik", sortOrder: 10 },
  { id: "speciaux", name: "Kebaba Spéciaux", sortOrder: 20 },
  { id: "dwich", name: "Kebaba 'Dwich", sortOrder: 30 },
  { id: "enfants", name: "Menus Enfants", sortOrder: 40 },
  { id: "bowls", name: "Kebaba Bowls", sortOrder: 45 },
  { id: "assiettes", name: "Kebaba Assiettes", sortOrder: 50 },
  { id: "texmex", name: "Tex-Mex", sortOrder: 60 },
  { id: "barquettes", name: "Barquettes", sortOrder: 70 },
  { id: "desserts", name: "Desserts", sortOrder: 80 },
  { id: "frites", name: "Frites", sortOrder: 85 },
  { id: "boissons", name: "Boissons", sortOrder: 90 },
] as const;

type ProductSeed = {
  id: string;
  categoryId: string;
  name: string;
  priceCents: number;
  sortOrder: number;
  hasPain?: boolean;
  hasFrites?: boolean;
  hasSauces?: boolean;
  hasCrudites?: boolean;
  hasSupplements?: boolean;
  description?: string;
};

const sandwichOpts = {
  hasPain: true,
  hasSauces: true,
  hasCrudites: true,
  hasSupplements: true,
} as const;

const bowlAssietteOpts = {
  hasSauces: true,
  hasCrudites: true,
  hasSupplements: true,
} as const;

export const seedProducts: ProductSeed[] = [
  { id: "kebaba-klassik", categoryId: "klassik", name: "Kebaba Klassik", priceCents: 800, sortOrder: 1, ...sandwichOpts },
  { id: "monster", categoryId: "speciaux", name: "Monster", priceCents: 1050, sortOrder: 1, ...sandwichOpts },
  { id: "chevre-miel", categoryId: "speciaux", name: "Chèvre Miel", priceCents: 1050, sortOrder: 2, ...sandwichOpts },
  { id: "cheezy-jalapanos", categoryId: "speciaux", name: "Cheezy Jalapanos", priceCents: 1050, sortOrder: 3, ...sandwichOpts },
  { id: "boursin-lardon", categoryId: "speciaux", name: "Boursin Lardon", priceCents: 1050, sortOrder: 4, ...sandwichOpts },
  { id: "avocado-fromage-frais", categoryId: "speciaux", name: "Avocado Fromage Frais", priceCents: 1050, sortOrder: 5, ...sandwichOpts },
  { id: "adana", categoryId: "dwich", name: "Adana", priceCents: 800, sortOrder: 1, ...sandwichOpts },
  { id: "kofte", categoryId: "dwich", name: "Kofte", priceCents: 800, sortOrder: 2, ...sandwichOpts },
  { id: "poulet-curry", categoryId: "dwich", name: "Poulet Curry", priceCents: 800, sortOrder: 3, ...sandwichOpts },
  { id: "vege", categoryId: "dwich", name: "Vege", priceCents: 800, sortOrder: 4, ...sandwichOpts },
  { id: "sucuk", categoryId: "dwich", name: "Sucuk", priceCents: 800, sortOrder: 5, ...sandwichOpts },
  { id: "ricain", categoryId: "dwich", name: "Ricain", priceCents: 800, sortOrder: 6, ...sandwichOpts },
  {
    id: "menu-enfant",
    categoryId: "enfants",
    name: "Menu Enfant",
    priceCents: 700,
    sortOrder: 1,
    hasSauces: true,
    hasCrudites: true,
    hasSupplements: true,
  },
  { id: "bowl-klassik", categoryId: "bowls", name: "Bowl Klassik", priceCents: 1200, sortOrder: 1, ...bowlAssietteOpts },
  { id: "bowl-monster", categoryId: "bowls", name: "Bowl Monster", priceCents: 1350, sortOrder: 2, ...bowlAssietteOpts },
  { id: "bowl-chevre-miel", categoryId: "bowls", name: "Bowl Chèvre Miel", priceCents: 1350, sortOrder: 3, ...bowlAssietteOpts },
  { id: "bowl-cheezy-jalapanos", categoryId: "bowls", name: "Bowl Cheezy Jalapanos", priceCents: 1350, sortOrder: 4, ...bowlAssietteOpts },
  { id: "bowl-boursin-lardon", categoryId: "bowls", name: "Bowl Boursin Lardon", priceCents: 1350, sortOrder: 5, ...bowlAssietteOpts },
  { id: "bowl-avocado-fromage-frais", categoryId: "bowls", name: "Bowl Avocado Fromage Frais", priceCents: 1350, sortOrder: 6, ...bowlAssietteOpts },
  { id: "bowl-burrata", categoryId: "bowls", name: "Bowl Burrata", priceCents: 1350, sortOrder: 7, ...bowlAssietteOpts },
  { id: "assiette-klassik", categoryId: "assiettes", name: "Assiette Klassik", priceCents: 1400, sortOrder: 1, ...bowlAssietteOpts },
  { id: "assiette-poulet", categoryId: "assiettes", name: "Assiette Poulet", priceCents: 1400, sortOrder: 2, ...bowlAssietteOpts },
  { id: "assiette-kofte", categoryId: "assiettes", name: "Assiette Kofte", priceCents: 1400, sortOrder: 3, ...bowlAssietteOpts },
  { id: "assiette-vege", categoryId: "assiettes", name: "Assiette Vege", priceCents: 1200, sortOrder: 4, ...bowlAssietteOpts },
  { id: "assiette-ricain", categoryId: "assiettes", name: "Assiette Ricain", priceCents: 1400, sortOrder: 5, ...bowlAssietteOpts },
  { id: "assiette-adana", categoryId: "assiettes", name: "Assiette Adana", priceCents: 1400, sortOrder: 6, ...bowlAssietteOpts },
  { id: "assiette-sucuk", categoryId: "assiettes", name: "Assiette Sucuk", priceCents: 1400, sortOrder: 7, ...bowlAssietteOpts },
  { id: "assiette-royal-kebaba", categoryId: "assiettes", name: "Assiette Royal Kebaba", priceCents: 1700, sortOrder: 8, ...bowlAssietteOpts },
  { id: "chicken-wings", categoryId: "texmex", name: "Chicken Wings", priceCents: 490, sortOrder: 1 },
  { id: "tenders", categoryId: "texmex", name: "Tenders", priceCents: 490, sortOrder: 2 },
  { id: "mozza-stick", categoryId: "texmex", name: "Mozza Stick", priceCents: 490, sortOrder: 3 },
  { id: "nuggets", categoryId: "texmex", name: "Nuggets", priceCents: 490, sortOrder: 4 },
  { id: "tex-mex-mix", categoryId: "texmex", name: "Tex Mex Mix", priceCents: 1090, sortOrder: 5 },
  { id: "barquette-frite-kebab", categoryId: "barquettes", name: "Barquette de Frite + Kebab", priceCents: 500, sortOrder: 1 },
  { id: "barquette-viande-kebab", categoryId: "barquettes", name: "Barquette de Viande Kebab", priceCents: 600, sortOrder: 2 },
  { id: "tiramisu", categoryId: "desserts", name: "Tiramisu", priceCents: 450, sortOrder: 1 },
  // Frites — catégorie juste avant boissons (PDF étapes + prix barquette maison)
  { id: "frites-maison", categoryId: "frites", name: "Frites Maison", priceCents: 350, sortOrder: 1 },
  { id: "frites-cheddar", categoryId: "frites", name: "Frites Cheddar", priceCents: 500, sortOrder: 2 },
  { id: "frites-cheddar-oignon-frit", categoryId: "frites", name: "Frites Cheddar Oignon Frit", priceCents: 550, sortOrder: 3 },
  { id: "frites-cheddar-bacon", categoryId: "frites", name: "Frites Cheddar Bacon", priceCents: 600, sortOrder: 4 },
  { id: "coca-cola-cherry-33", categoryId: "boissons", name: "Coca Cola Cherry 33 cl", priceCents: 200, sortOrder: 1 },
  { id: "coca-cola-33", categoryId: "boissons", name: "Coca Cola 33 cl", priceCents: 200, sortOrder: 2 },
  { id: "oasis-pomme-cassis-framboise-33", categoryId: "boissons", name: "Oasis Pomme Cassis Framboise 33 cl", priceCents: 200, sortOrder: 3 },
  { id: "oasis-tropical-33", categoryId: "boissons", name: "Oasis tropical 33 cl", priceCents: 200, sortOrder: 4 },
  { id: "fanta-orange-33", categoryId: "boissons", name: "Fanta orange 33 cl", priceCents: 200, sortOrder: 5 },
  { id: "tropico-orange-ananas-33", categoryId: "boissons", name: "Tropico orange ananas 33 cl", priceCents: 200, sortOrder: 6 },
  { id: "perrier-33", categoryId: "boissons", name: "Perrier 33 cl", priceCents: 200, sortOrder: 7 },
  { id: "lipton-ice-tea-peche-33", categoryId: "boissons", name: "Lipton ice tea pêche 33 cl", priceCents: 200, sortOrder: 8 },
  { id: "orangina-33", categoryId: "boissons", name: "Orangina 33 cl", priceCents: 200, sortOrder: 9 },
  { id: "ayran-25", categoryId: "boissons", name: "Ayran 25 cl", priceCents: 200, sortOrder: 10 },
  { id: "eau-50", categoryId: "boissons", name: "Eau 50 cl", priceCents: 200, sortOrder: 11 },
];

/** Surcoût par sauce au-delà des 2 incluses (PDF étapes). */
export const EXTRA_SAUCE_CENTS = 50;
export const MAX_INCLUDED_SAUCES = 2;

export const seedOptions = [
  // pains
  { id: "pain-frais", groupId: "pains", name: "Pain", priceCents: 0, sortOrder: 1 },
  { id: "durum", groupId: "pains", name: "Dürum", priceCents: 0, sortOrder: 2 },
  // garnitures
  { id: "salade", groupId: "crudites", name: "Salade", priceCents: 0, sortOrder: 1 },
  { id: "tomates", groupId: "crudites", name: "Tomate", priceCents: 0, sortOrder: 2 },
  { id: "oignon-rouge", groupId: "crudites", name: "Oignon Rouge", priceCents: 0, sortOrder: 3 },
  { id: "choux-rouge", groupId: "crudites", name: "Choux Rouge", priceCents: 0, sortOrder: 4 },
  { id: "concombre", groupId: "crudites", name: "Concombre", priceCents: 0, sortOrder: 5 },
  { id: "carotte", groupId: "crudites", name: "Carotte", priceCents: 0, sortOrder: 6 },
  { id: "sans-crudites", groupId: "crudites", name: "Sans les crudités", priceCents: 0, sortOrder: 7 },
  { id: "complet", groupId: "crudites", name: "Complet", priceCents: 0, sortOrder: 8 },
  // suppléments
  { id: "piment-jalapeno", groupId: "supplements", name: "Piment Jalapeno", priceCents: 100, sortOrder: 1 },
  { id: "cheddar-maison", groupId: "supplements", name: "Cheddar Maison", priceCents: 150, sortOrder: 2 },
  { id: "cornichon", groupId: "supplements", name: "Cornichon", priceCents: 100, sortOrder: 3 },
  { id: "sup-boursin", groupId: "supplements", name: "Boursin", priceCents: 150, sortOrder: 4 },
  { id: "feta", groupId: "supplements", name: "Feta", priceCents: 150, sortOrder: 5 },
  { id: "avocat", groupId: "supplements", name: "Avocat", priceCents: 200, sortOrder: 6 },
  { id: "sup-chevre", groupId: "supplements", name: "Chèvre", priceCents: 150, sortOrder: 7 },
  { id: "oeuf", groupId: "supplements", name: "Œuf", priceCents: 150, sortOrder: 8 },
  { id: "sup-viande", groupId: "supplements", name: "Viande", priceCents: 300, sortOrder: 9 },
  // sauces (ordre PDF)
  { id: "harissa", groupId: "sauces", name: "Harissa", priceCents: 0, sortOrder: 1 },
  { id: "blanche-maison", groupId: "sauces", name: "Blanche Maison", priceCents: 0, sortOrder: 2 },
  { id: "ketchup", groupId: "sauces", name: "Ketchup", priceCents: 0, sortOrder: 3 },
  { id: "samourai", groupId: "sauces", name: "Samouraï", priceCents: 0, sortOrder: 4 },
  { id: "mayo", groupId: "sauces", name: "Mayonnaise", priceCents: 0, sortOrder: 5 },
  { id: "bbq", groupId: "sauces", name: "BBQ", priceCents: 0, sortOrder: 6 },
  { id: "brazil", groupId: "sauces", name: "Brazil", priceCents: 0, sortOrder: 7 },
  { id: "andalouse", groupId: "sauces", name: "Andalouse", priceCents: 0, sortOrder: 8 },
  { id: "algerienne", groupId: "sauces", name: "Algérienne", priceCents: 0, sortOrder: 9 },
  { id: "curry", groupId: "sauces", name: "Curry", priceCents: 0, sortOrder: 10 },
  { id: "biggy", groupId: "sauces", name: "Biggy", priceCents: 0, sortOrder: 11 },
] as const;
