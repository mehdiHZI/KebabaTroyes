"use client";

import { useMemo, useState } from "react";
import Image from "next/image";
import { useRouter } from "next/navigation";
import type { CatalogOption } from "@/lib/catalog";
import { formatEur } from "@/lib/money";
import {
  EXTRA_SAUCE_CENTS,
  MAX_INCLUDED_SAUCES,
} from "@/db/seed-data";
import { UPSELL_EXTRAS } from "@/lib/upsell";
import { useCart, type CartOption } from "@/store/cart";
import { UpsellExtrasModal } from "@/components/UpsellExtrasModal";

type Product = {
  id: string;
  name: string;
  priceCents: number;
  hasPain: boolean;
  hasFrites: boolean;
  hasSauces: boolean;
  hasCrudites: boolean;
  hasSupplements: boolean;
  imageUrl?: string | null;
};

type Props = {
  product: Product;
  options: CatalogOption[];
};

const CRUDITE_INGREDIENTS = [
  "salade",
  "tomates",
  "oignon-rouge",
  "choux-rouge",
  "concombre",
  "carotte",
];

function optionButtonClass(active: boolean) {
  return `rounded-xl border px-3 py-2 text-sm font-semibold ${
    active
      ? "border-[var(--kebaba-bordeaux)] bg-[var(--kebaba-bordeaux)] text-white"
      : "border-[var(--kebaba-bordeaux)]/40 text-[var(--kebaba-bordeaux)]"
  }`;
}

export function ProductCustomizer({ product, options }: Props) {
  const router = useRouter();
  const { addLine } = useCart();

  const pains = options.filter((o) => o.groupId === "pains");
  const sauces = options.filter((o) => o.groupId === "sauces");
  const crudites = options.filter((o) => o.groupId === "crudites");
  const catalogSupplements = options.filter((o) => o.groupId === "supplements");

  const needsCustom =
    product.hasPain || product.hasSauces || product.hasCrudites;

  const showUpsell = product.hasSupplements;

  const [painId, setPainId] = useState(pains[0]?.id ?? "");
  const [sauceIds, setSauceIds] = useState<string[]>([]);
  const [cruditeIds, setCruditeIds] = useState<string[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [upsellOpen, setUpsellOpen] = useState(false);
  const [upsellIds, setUpsellIds] = useState<string[]>([]);

  const baseOptions: CartOption[] = useMemo(() => {
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
    sauceIds.forEach((id, index) => {
      const s = sauces.find((o) => o.id === id);
      if (!s) return;
      const extra = index >= MAX_INCLUDED_SAUCES ? EXTRA_SAUCE_CENTS : 0;
      list.push({
        optionId: s.id,
        name: extra > 0 ? `${s.name} (+${formatEur(extra)})` : s.name,
        priceCents: s.priceCents + extra,
        groupId: "sauces",
      });
    });
    return list;
  }, [
    product.hasPain,
    painId,
    sauceIds,
    cruditeIds,
    pains,
    sauces,
    crudites,
  ]);

  const extras = baseOptions.reduce((s, o) => s + o.priceCents, 0);
  const unit = product.priceCents + extras;

  function toggleSauce(id: string) {
    setError(null);
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
      if (ingredients.length === CRUDITE_INGREDIENTS.length) {
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
    if (product.hasCrudites && cruditeIds.length === 0) {
      setError("Sélectionnez vos garnitures (ou sans crudités).");
      return false;
    }
    if (product.hasSauces && sauceIds.length === 0) {
      setError("Choisissez au moins une sauce.");
      return false;
    }
    setError(null);
    return true;
  }

  function commitToCart(selectedUpsellIds: string[]) {
    const optionExtras: CartOption[] = [];
    for (const upsell of UPSELL_EXTRAS) {
      if (!selectedUpsellIds.includes(upsell.id)) continue;
      if (upsell.kind !== "option") continue;
      const fromCatalog = catalogSupplements.find((o) => o.id === upsell.id);
      optionExtras.push({
        optionId: upsell.id,
        name: fromCatalog?.name ?? upsell.name,
        priceCents: fromCatalog?.priceCents ?? upsell.priceCents,
        groupId: "supplements",
      });
    }

    addLine({
      productId: product.id,
      name: product.name,
      unitPriceCents: product.priceCents,
      options: [...baseOptions, ...optionExtras],
    });

    for (const upsell of UPSELL_EXTRAS) {
      if (!selectedUpsellIds.includes(upsell.id)) continue;
      if (upsell.kind !== "product") continue;
      addLine({
        productId: upsell.id,
        name: upsell.name,
        unitPriceCents: upsell.priceCents,
        options: [],
      });
    }

    router.push("/panier");
  }

  function onAddClick() {
    if (needsCustom && !validate()) return;
    if (showUpsell) {
      setUpsellIds([]);
      setUpsellOpen(true);
      return;
    }
    commitToCart([]);
  }

  function toggleUpsell(id: string) {
    setUpsellIds((prev) =>
      prev.includes(id) ? prev.filter((x) => x !== id) : [...prev, id],
    );
  }

  const extraSauceCount = Math.max(0, sauceIds.length - MAX_INCLUDED_SAUCES);

  return (
    <div className="space-y-6">
      <header className="kebaba-card overflow-hidden">
        {product.imageUrl ? (
          <div className="relative aspect-[4/3] w-full bg-[var(--kebaba-cream-deep)]">
            <Image
              src={product.imageUrl}
              alt={product.name}
              fill
              sizes="(max-width: 640px) 100vw, 560px"
              className="object-cover"
              priority
            />
          </div>
        ) : null}
        <div className="space-y-2 p-5">
          <h1 className="font-display text-2xl font-bold uppercase text-[var(--kebaba-bordeaux)]">
            {product.name}
          </h1>
          <p className="text-lg font-bold text-[var(--kebaba-bordeaux)]">
            {formatEur(unit)}
          </p>
        </div>
      </header>

      {product.hasPain ? (
        <fieldset className="kebaba-card space-y-3 p-5">
          <legend className="font-display text-lg font-bold text-[var(--kebaba-bordeaux)]">
            Choisi ton pain
          </legend>
          <div className="flex flex-wrap gap-2">
            {pains.map((p) => (
              <button
                key={p.id}
                type="button"
                onClick={() => setPainId(p.id)}
                className={optionButtonClass(painId === p.id)}
              >
                {p.name}
              </button>
            ))}
          </div>
        </fieldset>
      ) : null}

      {product.hasCrudites ? (
        <fieldset className="kebaba-card space-y-3 p-5">
          <legend className="font-display text-lg font-bold text-[var(--kebaba-bordeaux)]">
            Choisi tes garnitures
          </legend>
          <p className="text-xs text-[var(--kebaba-ink-muted)]">
            Multi-choix · « Sans les crudités » exclusif · « Complet » = les 6
          </p>
          <div className="flex flex-wrap gap-2">
            {crudites.map((c) => (
              <button
                key={c.id}
                type="button"
                onClick={() => toggleCrudite(c.id)}
                className={optionButtonClass(cruditeIds.includes(c.id))}
              >
                {c.name}
              </button>
            ))}
          </div>
        </fieldset>
      ) : null}

      {product.hasSauces ? (
        <fieldset className="kebaba-card space-y-3 p-5">
          <legend className="font-display text-lg font-bold text-[var(--kebaba-bordeaux)]">
            Choisi 2 sauces maximum
          </legend>
          <p className="text-xs text-[var(--kebaba-ink-muted)]">
            {MAX_INCLUDED_SAUCES} sauces incluses · supplémentaire : +
            {formatEur(EXTRA_SAUCE_CENTS)}
            {extraSauceCount > 0 ? ` · ${extraSauceCount} en plus` : null}
          </p>
          <div className="flex flex-wrap gap-2">
            {sauces.map((s) => (
              <button
                key={s.id}
                type="button"
                onClick={() => toggleSauce(s.id)}
                className={optionButtonClass(sauceIds.includes(s.id))}
              >
                {s.name}
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

      <button type="button" className="kebaba-btn w-full" onClick={onAddClick}>
        Ajouter au panier · {formatEur(unit)}
      </button>

      <UpsellExtrasModal
        open={upsellOpen}
        selectedIds={upsellIds}
        onToggle={toggleUpsell}
        onSkip={() => {
          setUpsellOpen(false);
          commitToCart([]);
        }}
        onConfirm={() => {
          setUpsellOpen(false);
          commitToCart(upsellIds);
        }}
      />
    </div>
  );
}
