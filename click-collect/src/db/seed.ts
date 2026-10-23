import "dotenv/config";
import { notInArray } from "drizzle-orm";
import { db } from "./index";
import { categories, options, products } from "./schema";
import { seedCategories, seedOptions, seedProducts } from "./seed-data";
import { productImageSrc } from "../lib/product-image";

const seedProductIds = seedProducts.map((p) => p.id);
const seedOptionIds = seedOptions.map((o) => o.id);

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
    const imageUrl = productImageSrc(p.id);
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
        hasFrites: p.hasFrites ?? false,
        hasSauces: p.hasSauces ?? false,
        hasCrudites: p.hasCrudites ?? false,
        hasSupplements: p.hasSupplements ?? false,
        imageUrl,
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
          hasFrites: p.hasFrites ?? false,
          hasSauces: p.hasSauces ?? false,
          hasCrudites: p.hasCrudites ?? false,
          hasSupplements: p.hasSupplements ?? false,
          imageUrl,
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
          active: true,
        },
      });
  }

  await db
    .update(options)
    .set({ active: false })
    .where(notInArray(options.id, seedOptionIds));

  await db
    .update(products)
    .set({ active: false })
    .where(notInArray(products.id, seedProductIds));

  console.log(
    `OK — ${seedCategories.length} catégories, ${seedProducts.length} produits, ${seedOptions.length} options.`,
  );
  process.exit(0);
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
