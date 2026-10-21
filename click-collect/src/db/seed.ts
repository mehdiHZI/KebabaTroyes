import "dotenv/config";
import { db } from "./index";
import { categories, options, products } from "./schema";
import { seedCategories, seedOptions, seedProducts } from "./seed-data";

async function main() {
  console.log("Seeding catalogue Kebaba…");

  for (const cat of seedCategories) {
    await db
      .insert(categories)
      .values({ ...cat, active: true })
      .onConflictDoUpdate({
        target: categories.id,
        set: { name: cat.name, sortOrder: cat.sortOrder },
      });
  }

  for (const p of seedProducts) {
    await db
      .insert(products)
      .values({
        id: p.id,
        categoryId: p.categoryId,
        name: p.name,
        description: p.description ?? null,
        priceCents: p.priceCents,
        sortOrder: p.sortOrder,
        hasPain: p.hasPain ?? false,
        hasSauces: p.hasSauces ?? false,
        hasCrudites: p.hasCrudites ?? false,
        active: true,
        tvaRateBps: 1000,
      })
      .onConflictDoUpdate({
        target: products.id,
        set: {
          categoryId: p.categoryId,
          name: p.name,
          priceCents: p.priceCents,
          sortOrder: p.sortOrder,
          hasPain: p.hasPain ?? false,
          hasSauces: p.hasSauces ?? false,
          hasCrudites: p.hasCrudites ?? false,
        },
      });
  }

  for (const o of seedOptions) {
    await db
      .insert(options)
      .values({ ...o, active: true })
      .onConflictDoUpdate({
        target: options.id,
        set: {
          groupId: o.groupId,
          name: o.name,
          priceCents: o.priceCents,
          sortOrder: o.sortOrder,
        },
      });
  }

  console.log(
    `OK — ${seedCategories.length} catégories, ${seedProducts.length} produits, ${seedOptions.length} options.`,
  );
  process.exit(0);
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
