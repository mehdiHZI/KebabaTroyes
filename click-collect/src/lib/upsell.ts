/** Petits + proposés juste avant validation panier. */

export type UpsellExtra = {
  id: string;
  name: string;
  priceCents: number;
  /** option = sur le produit ; product = ligne panier séparée */
  kind: "option" | "product";
  blurb: string;
};

export const UPSELL_EXTRAS: UpsellExtra[] = [
  {
    id: "feta",
    name: "Feta",
    priceCents: 150,
    kind: "option",
    blurb: "Le croquant salé",
  },
  {
    id: "sup-boursin",
    name: "Boursin",
    priceCents: 150,
    kind: "option",
    blurb: "Crémeux & herbes",
  },
  {
    id: "cheddar-maison",
    name: "Cheddar Maison",
    priceCents: 150,
    kind: "option",
    blurb: "Fondant maison",
  },
  {
    id: "frites-cheddar-bacon",
    name: "Frites Cheddar Bacon",
    priceCents: 600,
    kind: "product",
    blurb: "Le duo ultra gourmand",
  },
];
