import {
  EXTRA_SAUCE_CENTS,
  MAX_INCLUDED_SAUCES,
} from "@/db/seed-data";
import {
  getCatalog,
  type CatalogOption,
  type CatalogProduct,
} from "@/lib/catalog";

export class PricingError extends Error {
  constructor(
    public readonly code:
      | "UNKNOWN_PRODUCT"
      | "UNKNOWN_OPTION"
      | "OPTION_NOT_ALLOWED"
      | "EMPTY_CART",
    message: string,
  ) {
    super(message);
    this.name = "PricingError";
  }
}

export type RawOrderLine = {
  productId: string;
  quantity: number;
  /** Ordre conservé (important pour le surcoût sauces). */
  optionIds: string[];
};

export type PricedOption = {
  optionId: string;
  name: string;
  priceCents: number;
  groupId: string;
};

export type PricedOrderLine = {
  productId: string;
  productName: string;
  unitPriceCents: number;
  quantity: number;
  options: PricedOption[];
  lineTotalCents: number;
};

function productMap(
  categories: { products: CatalogProduct[] }[],
): Map<string, CatalogProduct> {
  const map = new Map<string, CatalogProduct>();
  for (const cat of categories) {
    for (const p of cat.products) map.set(p.id, p);
  }
  return map;
}

function optionAllowedForProduct(
  product: CatalogProduct,
  groupId: string,
): boolean {
  switch (groupId) {
    case "pains":
      return product.hasPain;
    case "frites-etape":
      return product.hasFrites;
    case "crudites":
      return product.hasCrudites;
    case "supplements":
      return product.hasSupplements;
    case "sauces":
      return product.hasSauces;
    default:
      return false;
  }
}

/**
 * Recalcule prix + libellés depuis le catalogue serveur.
 * Ne fait jamais confiance aux montants / noms envoyés par le client.
 */
export function priceLinesFromCatalog(
  lines: RawOrderLine[],
  catalog: {
    categories: { products: CatalogProduct[] }[];
    options: CatalogOption[];
  },
): PricedOrderLine[] {
  if (lines.length === 0) {
    throw new PricingError("EMPTY_CART", "Panier vide.");
  }

  const products = productMap(catalog.categories);
  const optionsById = new Map(catalog.options.map((o) => [o.id, o]));

  return lines.map((line) => {
    const product = products.get(line.productId);
    if (!product) {
      throw new PricingError(
        "UNKNOWN_PRODUCT",
        `Produit inconnu ou indisponible : ${line.productId}`,
      );
    }

    let sauceIndex = 0;
    const pricedOptions: PricedOption[] = [];

    for (const optionId of line.optionIds) {
      const opt = optionsById.get(optionId);
      if (!opt) {
        throw new PricingError(
          "UNKNOWN_OPTION",
          `Option inconnue : ${optionId}`,
        );
      }
      if (!optionAllowedForProduct(product, opt.groupId)) {
        throw new PricingError(
          "OPTION_NOT_ALLOWED",
          `Option « ${opt.name} » non autorisée pour ${product.name}.`,
        );
      }

      let priceCents = opt.priceCents;
      let name = opt.name;
      if (opt.groupId === "sauces") {
        if (sauceIndex >= MAX_INCLUDED_SAUCES) {
          priceCents += EXTRA_SAUCE_CENTS;
          name = `${opt.name} (extra)`;
        }
        sauceIndex += 1;
      }

      pricedOptions.push({
        optionId: opt.id,
        name,
        priceCents,
        groupId: opt.groupId,
      });
    }

    const optionsTotal = pricedOptions.reduce((s, o) => s + o.priceCents, 0);
    const unitPriceCents = product.priceCents;
    const lineTotalCents =
      (unitPriceCents + optionsTotal) * line.quantity;

    return {
      productId: product.id,
      productName: product.name,
      unitPriceCents,
      quantity: line.quantity,
      options: pricedOptions,
      lineTotalCents,
    };
  });
}

export async function priceOrderLines(
  lines: RawOrderLine[],
): Promise<PricedOrderLine[]> {
  const catalog = await getCatalog();
  return priceLinesFromCatalog(lines, catalog);
}
