"use client";

import Link from "next/link";
import { formatEur } from "@/lib/money";
import { lineTotal, useCart } from "@/store/cart";

export default function PanierPage() {
  const { lines, setQuantity, removeLine, totalCents, itemCount } = useCart();

  if (itemCount === 0) {
    return (
      <div className="kebaba-card space-y-4 p-8 text-center">
        <h1 className="font-display text-2xl font-bold text-[var(--kebaba-bordeaux)]">
          Panier vide
        </h1>
        <p className="text-[var(--kebaba-ink-muted)]">
          Ajoutez des articles depuis le menu.
        </p>
        <Link href="/menu" className="kebaba-btn inline-flex">
          Voir le menu
        </Link>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-xl space-y-6 pb-24">
      <h1 className="font-display text-3xl font-bold uppercase text-[var(--kebaba-bordeaux)]">
        Panier
      </h1>

      <ul className="space-y-3">
        {lines.map((line) => (
          <li key={line.key} className="kebaba-card space-y-2 p-4">
            <div className="flex items-start justify-between gap-3">
              <div>
                <p className="font-bold text-[var(--kebaba-bordeaux)]">{line.name}</p>
                {line.options.length > 0 ? (
                  <p className="mt-1 text-xs text-[var(--kebaba-ink-muted)]">
                    {line.options.map((o) => o.name).join(" · ")}
                  </p>
                ) : null}
              </div>
              <p className="font-bold">{formatEur(lineTotal(line))}</p>
            </div>
            <div className="flex items-center gap-3">
              <button
                type="button"
                className="kebaba-btn-outline px-3 py-1 text-sm"
                onClick={() => setQuantity(line.key, line.quantity - 1)}
              >
                −
              </button>
              <span className="min-w-6 text-center font-bold">{line.quantity}</span>
              <button
                type="button"
                className="kebaba-btn-outline px-3 py-1 text-sm"
                onClick={() => setQuantity(line.key, line.quantity + 1)}
              >
                +
              </button>
              <button
                type="button"
                className="ml-auto text-sm font-semibold text-red-700"
                onClick={() => removeLine(line.key)}
              >
                Retirer
              </button>
            </div>
          </li>
        ))}
      </ul>

      <div className="kebaba-card flex items-center justify-between p-4">
        <span className="font-bold">Total</span>
        <span className="font-display text-xl font-bold text-[var(--kebaba-bordeaux)]">
          {formatEur(totalCents)}
        </span>
      </div>

      <Link href="/commande" className="kebaba-btn w-full">
        Commander · {formatEur(totalCents)}
      </Link>
      <p className="text-center text-xs text-[var(--kebaba-ink-muted)]">
        Un compte est requis pour finaliser la commande.
      </p>
    </div>
  );
}
