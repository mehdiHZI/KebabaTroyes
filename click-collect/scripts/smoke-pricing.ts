/**
 * Smoke test pricing serveur (sans BDD) — npx tsx scripts/smoke-pricing.ts
 */
import assert from "node:assert/strict";
import { seedCategories, seedOptions, seedProducts } from "../src/db/seed-data";
import {
  priceLinesFromCatalog as price,
  PricingError as PE,
} from "../src/lib/pricing";

const catalog = {
  categories: seedCategories.map((c) => ({
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
        hasFrites: p.hasFrites ?? false,
        hasSauces: p.hasSauces ?? false,
        hasCrudites: p.hasCrudites ?? false,
        hasSupplements: p.hasSupplements ?? false,
        imageUrl: null,
      })),
  })),
  options: seedOptions.map((o) => ({
    id: o.id,
    groupId: o.groupId,
    name: o.name,
    priceCents: o.priceCents,
  })),
};

// 1) Prix produit seul
{
  const [line] = price(
    [{ productId: "kebaba-klassik", quantity: 2, optionIds: [] }],
    catalog,
  );
  assert.equal(line.unitPriceCents, 800);
  assert.equal(line.lineTotalCents, 1600);
}

// 2) Ignore toute tentative de prix client (pas dans l’API) + 3e sauce = +50
{
  const [line] = price(
    [
      {
        productId: "kebaba-klassik",
        quantity: 1,
        optionIds: [
          "pain-frais",
          "salade",
          "harissa",
          "blanche-maison",
          "samourai",
        ],
      },
    ],
    catalog,
  );
  // 800 + 0 options base + 50 sauce extra
  assert.equal(line.lineTotalCents, 850);
  const extra = line.options.find((o) => o.optionId === "samourai");
  assert.equal(extra?.priceCents, 50);
}

// 3) Supplément catalogue
{
  const [line] = price(
    [
      {
        productId: "kebaba-klassik",
        quantity: 1,
        optionIds: ["pain-frais", "feta"],
      },
    ],
    catalog,
  );
  assert.equal(line.lineTotalCents, 950); // 800 + 150
}

// 4) Produit inconnu
{
  let caught = false;
  try {
    price([{ productId: "pizza-fake", quantity: 1, optionIds: [] }], catalog);
  } catch (e) {
    assert.ok(e instanceof PE);
    assert.equal(e.code, "UNKNOWN_PRODUCT");
    caught = true;
  }
  assert.equal(caught, true);
}

// 5) Option non autorisée (boisson + sauce)
{
  let caught = false;
  try {
    price(
      [{ productId: "coca-cola-33", quantity: 1, optionIds: ["harissa"] }],
      catalog,
    );
  } catch (e) {
    assert.ok(e instanceof PE);
    assert.equal(e.code, "OPTION_NOT_ALLOWED");
    caught = true;
  }
  assert.equal(caught, true);
}

console.log("smoke-pricing: OK");
