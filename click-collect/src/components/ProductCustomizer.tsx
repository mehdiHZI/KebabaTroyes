"use client";

import { useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import type { CatalogOption } from "@/lib/catalog";
import { formatEur } from "@/lib/money";
import { useCart, type CartOption } from "@/store/cart";

type Product = {
  id: string;
  name: string;
  priceCents: number;
  hasPain: boolean;
  hasSauces: boolean;
  hasCrudites: boolean;
};

type Props = {
  product: Product;
  options: CatalogOption[];
};

const CRUDITE_INGREDIENTS = ["salade", "tomates", "oignon-rouge", "choux-rouge"];

export function ProductCustomizer({ product, options }: Props) {
  const router = useRouter();
  const { addLine } = useCart();

  const pains = options.filter((o) => o.groupId === "pains");
  const sauces = options.filter((o) => o.groupId === "sauces");
  const crudites = options.filter((o) => o.groupId === "crudites");

  const [painId, setPainId] = useState(pains[0]?.id ?? "");
  const [sauceIds, setSauceIds] = useState<string[]>([]);
  const [cruditeIds, setCruditeIds] = useState<string[]>([]);
  const [error, setError] = useState<string | null>(null);

  const selectedOptions: CartOption[] = useMemo(() => {
    const list: CartOption[] = [];
    if (product.hasPain && painId) {
      const p = pains.find((o) => o.id === painId);
      if (p)
        list.push({
          optionId: p.id,
          name: p.name,
          priceCents: p.priceCents,
          groupId: "pains",
        });
    }
    for (const id of sauceIds) {
      const s = sauces.find((o) => o.id === id);
      if (s)
        list.push({
          optionId: s.id,
          name: s.name,
          priceCents: s.priceCents,
          groupId: "sauces",
        });
    }
    for (const id of cruditeIds) {
      const c = crudites.find((o) => o.id === id);
      if (c)
        list.push({
          optionId: c.id,
          name: c.name,
          priceCents: c.priceCents,
          groupId: "crudites",
        });
    }
    return list;
  }, [product.hasPain, painId, sauceIds, cruditeIds, pains, sauces, crudites]);

  const extras = selectedOptions.reduce((s, o) => s + o.priceCents, 0);
  const unit = product.priceCents + extras;

  function toggleSauce(id: string) {
    setSauceIds((prev) =>
      prev.includes(id) ? prev.filter((x) => x !== id) : [...prev, id],
    );
  }

  function toggleCrudite(id: string) {
    setError(null);
    if (id === "sans-crudites") {
      setCruditeIds((prev) => (prev.includes(id) ? [] : ["sans-crudites"]));
      return;
    }
    if (id === "complet") {
      setCruditeIds((prev) =>
        prev.includes("complet") ? [] : [...CRUDITE_INGREDIENTS, "complet"],
      );
      return;
    }
    setCruditeIds((prev) => {
      let next = prev.filter((x) => x !== "sans-crudites" && x !== "complet");
      if (next.includes(id)) next = next.filter((x) => x !== id);
      else next = [...next, id];
      const ingredients = next.filter((x) => CRUDITE_INGREDIENTS.includes(x));
      if (ingredients.length === 4) {
        return [...ingredients, "complet"];
      }
      return ingredients;
    });
  }

  function validate(): boolean {
    if (product.hasPain && !painId) {
      setError("Choisissez un pain.");
      return false;
    }
    if (product.hasCrudites) {
      const count = cruditeIds.filter(
        (id) => id !== "complet" && id !== "sans-crudites",
      ).length;
      const sans = cruditeIds.includes("sans-crudites");
      if (!sans && (count < 1 || count > 4)) {
        setError("Crudités : entre 1 et 4 choix, ou « Sans les crudités ».");
        return false;
      }
      if (!sans && count === 0 && !cruditeIds.includes("complet")) {
        setError("Sélectionnez vos crudités.");
        return false;
      }
      if (cruditeIds.length === 0) {
        setError("Sélectionnez vos crudités (ou sans crudités).");
        return false;
      }
    }
    setError(null);
    return true;
  }

  function onAdd() {
    if (!validate()) return;
    addLine({
      productId: product.id,
      name: product.name,
      unitPriceCents: product.priceCents,
      options: selectedOptions,
    });
    router.push("/panier");
  }

  return (
    <div className="space-y-6">
      <header className="kebaba-card space-y-2 p-5">
        <h1 className="font-display text-2xl font-bold uppercase text-[var(--kebaba-bordeaux)]">
          {product.name}
        </h1>
        <p className="text-lg font-bold text-[var(--kebaba-bordeaux)]">
          {formatEur(unit)}
        </p>
      </header>

      {product.hasPain ? (
        <fieldset className="kebaba-card space-y-3 p-5">
          <legend className="font-display text-lg font-bold text-[var(--kebaba-bordeaux)]">
            Pain
          </legend>
          <div className="flex flex-wrap gap-2">
            {pains.map((p) => (
              <button
                key={p.id}
                type="button"
                onClick={() => setPainId(p.id)}
                className={`rounded-xl border px-3 py-2 text-sm font-semibold ${
                  painId === p.id
                    ? "border-[var(--kebaba-bordeaux)] bg-[var(--kebaba-bordeaux)] text-white"
                    : "border-[var(--kebaba-bordeaux)]/40 text-[var(--kebaba-bordeaux)]"
                }`}
              >
                {p.name}
              </button>
            ))}
          </div>
        </fieldset>
      ) : null}

      {product.hasSauces ? (
        <fieldset className="kebaba-card space-y-3 p-5">
          <legend className="font-display text-lg font-bold text-[var(--kebaba-bordeaux)]">
            Sauces
          </legend>
          <div className="flex flex-wrap gap-2">
            {sauces.map((s) => (
              <button
                key={s.id}
                type="button"
                onClick={() => toggleSauce(s.id)}
                className={`rounded-xl border px-3 py-2 text-sm font-semibold ${
                  sauceIds.includes(s.id)
                    ? "border-[var(--kebaba-bordeaux)] bg-[var(--kebaba-bordeaux)] text-white"
                    : "border-[var(--kebaba-bordeaux)]/40 text-[var(--kebaba-bordeaux)]"
                }`}
              >
                {s.name}
              </button>
            ))}
          </div>
        </fieldset>
      ) : null}

      {product.hasCrudites ? (
        <fieldset className="kebaba-card space-y-3 p-5">
          <legend className="font-display text-lg font-bold text-[var(--kebaba-bordeaux)]">
            Crudité au choix
          </legend>
          <p className="text-xs text-[var(--kebaba-ink-muted)]">
            1 à 4 choix · « Sans les crudités » exclusif · « Complet » = les 4
          </p>
          <div className="flex flex-wrap gap-2">
            {crudites.map((c) => (
              <button
                key={c.id}
                type="button"
                onClick={() => toggleCrudite(c.id)}
                className={`rounded-xl border px-3 py-2 text-sm font-semibold ${
                  cruditeIds.includes(c.id)
                    ? "border-[var(--kebaba-bordeaux)] bg-[var(--kebaba-bordeaux)] text-white"
                    : "border-[var(--kebaba-bordeaux)]/40 text-[var(--kebaba-bordeaux)]"
                }`}
              >
                {c.name}
              </button>
            ))}
          </div>
        </fieldset>
      ) : null}

      {error ? (
        <p className="text-sm font-semibold text-red-700" role="alert">
          {error}
        </p>
      ) : null}

      <button type="button" className="kebaba-btn w-full" onClick={onAdd}>
        Ajouter au panier · {formatEur(unit)}
      </button>
    </div>
  );
}
