/** Seed catalogue — source : règle Cursor carte-kebaba (centimes TTC). */

export const seedCategories = [
  { id: "klassik", name: "Kebaba Klassik", sortOrder: 10 },
  { id: "speciaux", name: "Kebaba Spéciaux", sortOrder: 20 },
  { id: "dwich", name: "Kebaba 'Dwich", sortOrder: 30 },
  { id: "enfants", name: "Menus Enfants", sortOrder: 40 },
  { id: "assiettes", name: "Kebaba Assiettes", sortOrder: 50 },
  { id: "texmex", name: "Tex-Mex", sortOrder: 60 },
  { id: "barquettes", name: "Barquettes", sortOrder: 70 },
  { id: "desserts", name: "Desserts", sortOrder: 80 },
  { id: "boissons", name: "Boissons", sortOrder: 90 },
] as const;

type ProductSeed = {
  id: string;
  categoryId: string;
  name: string;
  priceCents: number;
  sortOrder: number;
  hasPain?: boolean;
  hasSauces?: boolean;
  hasCrudites?: boolean;
  description?: string;
};

const sandwichOpts = {
  hasPain: true,
  hasSauces: true,
  hasCrudites: true,
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
  { id: "menu-enfant", categoryId: "enfants", name: "Menu Enfant", priceCents: 700, sortOrder: 1 },
  { id: "assiette-klassik", categoryId: "assiettes", name: "Assiette Klassik", priceCents: 1400, sortOrder: 1, hasSauces: true, hasCrudites: true },
  { id: "assiette-poulet", categoryId: "assiettes", name: "Assiette Poulet", priceCents: 1400, sortOrder: 2, hasSauces: true, hasCrudites: true },
  { id: "assiette-kofte", categoryId: "assiettes", name: "Assiette Kofte", priceCents: 1400, sortOrder: 3, hasSauces: true, hasCrudites: true },
  { id: "assiette-vege", categoryId: "assiettes", name: "Assiette Vege", priceCents: 1200, sortOrder: 4, hasSauces: true, hasCrudites: true },
  { id: "assiette-ricain", categoryId: "assiettes", name: "Assiette Ricain", priceCents: 1400, sortOrder: 5, hasSauces: true, hasCrudites: true },
  { id: "assiette-adana", categoryId: "assiettes", name: "Assiette Adana", priceCents: 1400, sortOrder: 6, hasSauces: true, hasCrudites: true },
  { id: "assiette-sucuk", categoryId: "assiettes", name: "Assiette Sucuk", priceCents: 1400, sortOrder: 7, hasSauces: true, hasCrudites: true },
  { id: "assiette-royal-kebaba", categoryId: "assiettes", name: "Assiette Royal Kebaba", priceCents: 1700, sortOrder: 8, hasSauces: true, hasCrudites: true },
  { id: "chicken-wings", categoryId: "texmex", name: "Chicken Wings", priceCents: 490, sortOrder: 1 },
  { id: "tenders", categoryId: "texmex", name: "Tenders", priceCents: 490, sortOrder: 2 },
  { id: "mozza-stick", categoryId: "texmex", name: "Mozza Stick", priceCents: 490, sortOrder: 3 },
  { id: "nuggets", categoryId: "texmex", name: "Nuggets", priceCents: 490, sortOrder: 4 },
  { id: "tex-mex-mix", categoryId: "texmex", name: "Tex Mex Mix", priceCents: 1090, sortOrder: 5 },
  { id: "barquette-frite-kebab", categoryId: "barquettes", name: "Barquette de Frite + Kebab", priceCents: 500, sortOrder: 1 },
  { id: "barquette-viande-kebab", categoryId: "barquettes", name: "Barquette de Viande Kebab", priceCents: 600, sortOrder: 2 },
  { id: "barquette-frite", categoryId: "barquettes", name: "Barquette de Frite", priceCents: 350, sortOrder: 3 },
  { id: "tiramisu", categoryId: "desserts", name: "Tiramisu", priceCents: 450, sortOrder: 1 },
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

export const seedOptions = [
  // pains
  { id: "durum", groupId: "pains", name: "Durum", priceCents: 0, sortOrder: 1 },
  { id: "pain-frais", groupId: "pains", name: "Pain frais", priceCents: 0, sortOrder: 2 },
  // sauces
  { id: "blanche-maison", groupId: "sauces", name: "Blanche Maison", priceCents: 0, sortOrder: 1 },
  { id: "ketchup", groupId: "sauces", name: "Ketchup", priceCents: 0, sortOrder: 2 },
  { id: "mayo", groupId: "sauces", name: "Mayo", priceCents: 0, sortOrder: 3 },
  { id: "andalouse", groupId: "sauces", name: "Andalouse", priceCents: 0, sortOrder: 4 },
  { id: "samourai", groupId: "sauces", name: "Samouraï", priceCents: 0, sortOrder: 5 },
  { id: "algerienne", groupId: "sauces", name: "Algérienne", priceCents: 0, sortOrder: 6 },
  { id: "bbq", groupId: "sauces", name: "BBQ", priceCents: 0, sortOrder: 7 },
  { id: "biggy", groupId: "sauces", name: "Biggy", priceCents: 0, sortOrder: 8 },
  { id: "moutarde", groupId: "sauces", name: "Moutarde", priceCents: 0, sortOrder: 9 },
  { id: "harissa", groupId: "sauces", name: "Harissa", priceCents: 0, sortOrder: 10 },
  { id: "brazil", groupId: "sauces", name: "Brazil", priceCents: 0, sortOrder: 11 },
  { id: "curry", groupId: "sauces", name: "Curry", priceCents: 0, sortOrder: 12 },
  { id: "poivre-vert", groupId: "sauces", name: "Poivre Vert", priceCents: 0, sortOrder: 13 },
  // crudités
  { id: "salade", groupId: "crudites", name: "Salade", priceCents: 0, sortOrder: 1 },
  { id: "tomates", groupId: "crudites", name: "Tomates", priceCents: 0, sortOrder: 2 },
  { id: "oignon-rouge", groupId: "crudites", name: "Oignon Rouge", priceCents: 0, sortOrder: 3 },
  { id: "choux-rouge", groupId: "crudites", name: "Choux Rouge", priceCents: 0, sortOrder: 4 },
  { id: "sans-crudites", groupId: "crudites", name: "Sans les crudités", priceCents: 0, sortOrder: 5 },
  { id: "complet", groupId: "crudites", name: "Complet", priceCents: 0, sortOrder: 6 },
] as const;
