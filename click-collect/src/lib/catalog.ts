import { asc, eq } from "drizzle-orm";
import { db } from "@/db";
import { categories, options, products } from "@/db/schema";
import { seedCategories, seedOptions, seedProducts } from "@/db/seed-data";

export type CatalogCategory = {
  id: string;
  name: string;
  products: {
    id: string;
    name: string;
    description: string | null;
    priceCents: number;
    hasPain: boolean;
    hasSauces: boolean;
    hasCrudites: boolean;
  }[];
};

export type CatalogOption = {
  id: string;
  groupId: string;
  name: string;
  priceCents: number;
};

/** Fallback mémoire si la BDD n’est pas encore dispo (dev / premier boot). */
function catalogFromSeed(): {
  categories: CatalogCategory[];
  options: CatalogOption[];
} {
  const cats: CatalogCategory[] = seedCategories.map((c) => ({
    id: c.id,
    name: c.name,
    products: seedProducts
      .filter((p) => p.categoryId === c.id)
      .map((p) => ({
        id: p.id,
        name: p.name,
        description: p.description ?? null,
        priceCents: p.priceCents,
        hasPain: p.hasPain ?? false,
        hasSauces: p.hasSauces ?? false,
        hasCrudites: p.hasCrudites ?? false,
      })),
  }));

  return {
    categories: cats,
    options: seedOptions.map((o) => ({
      id: o.id,
      groupId: o.groupId,
      name: o.name,
      priceCents: o.priceCents,
    })),
  };
}

export async function getCatalog(): Promise<{
  categories: CatalogCategory[];
  options: CatalogOption[];
  source: "db" | "seed";
}> {
  try {
    const [dbCategories, dbProducts, dbOptions] = await Promise.all([
      db
        .select()
        .from(categories)
        .where(eq(categories.active, true))
        .orderBy(asc(categories.sortOrder)),
      db
        .select()
        .from(products)
        .where(eq(products.active, true))
        .orderBy(asc(products.sortOrder)),
      db
        .select()
        .from(options)
        .where(eq(options.active, true))
        .orderBy(asc(options.sortOrder)),
    ]);

    if (dbCategories.length === 0) {
      return { ...catalogFromSeed(), source: "seed" };
    }

    return {
      source: "db",
      categories: dbCategories.map((c) => ({
        id: c.id,
        name: c.name,
        products: dbProducts
          .filter((p) => p.categoryId === c.id)
          .map((p) => ({
            id: p.id,
            name: p.name,
            description: p.description,
            priceCents: p.priceCents,
            hasPain: p.hasPain,
            hasSauces: p.hasSauces,
            hasCrudites: p.hasCrudites,
          })),
      })),
      options: dbOptions.map((o) => ({
        id: o.id,
        groupId: o.groupId,
        name: o.name,
        priceCents: o.priceCents,
      })),
    };
  } catch {
    return { ...catalogFromSeed(), source: "seed" };
  }
}
