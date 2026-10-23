/**
 * Contenu présentation homepage — à modifier plus tard sans toucher au layout.
 * Chemins d’images = fichiers dans /public
 */

export const homePresentation = {
  about: {
    eyebrow: "Chez Kebaba",
    title: "Le vrai goût du kebab, fait avec soin",
    text: "Pain frais, viandes marinées, sauces maison et crudités croquantes. On mise sur le fait maison et des produits de qualité — pour un kebab comme on les aime.",
    image: "/products/kebaba-klassik.jpeg",
    imageAlt: "Kebaba Klassik",
  },
  highlights: [
    {
      title: "Fait maison",
      text: "Frites dorées, sauces préparées sur place, recettes travaillées chaque jour.",
      image: "/products/assiette-klassik.jpeg",
      imageAlt: "Assiette Klassik",
    },
    {
      title: "Des classiques & des signatures",
      text: "Du Klassik aux spéciaux Chèvre Miel, Monster, Avocado… il y en a pour tous les goûts.",
      image: "/products/chevre-miel.jpeg",
      imageAlt: "Chèvre Miel",
    },
    {
      title: "Assiettes généreuses",
      text: "Envie de manger à l’assiette ? Royal, Adana, Sucuk, Vege — généreux et savoureux.",
      image: "/products/assiette-royal-kebaba.jpeg",
      imageAlt: "Assiette Royal Kebaba",
    },
    {
      title: "Bowls",
      text: "Klassik, Monster, Chèvre Miel, Burrata… le kebab en bol, sauces et crudités au choix.",
      image: "/products/bowl-klassik.jpeg",
      imageAlt: "Bowl Klassik",
    },
  ],
  story: {
    title: "Click & Collect, sans la file d’attente",
    text: "Compose ta commande en ligne, choisis ton créneau, et récupère au comptoir. Moins d’attente, autant de plaisir.",
    image: "/products/assiette-poulet.jpeg",
    imageAlt: "Assiette poulet",
    ctaLabel: "Commander maintenant",
    ctaHref: "/menu",
  },
} as const;
