"use client";

import { useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { formatEur } from "@/lib/money";
import { lineTotal, useCart } from "@/store/cart";

type Props = {
  userName?: string | null;
};

function nextSlots(count = 8): { label: string; value: string }[] {
  const out: { label: string; value: string }[] = [];
  const now = new Date();
  const start = new Date(now.getTime() + 20 * 60 * 1000);
  start.setMinutes(Math.ceil(start.getMinutes() / 15) * 15, 0, 0);
  for (let i = 0; i < count; i++) {
    const d = new Date(start.getTime() + i * 15 * 60 * 1000);
    out.push({
      label: d.toLocaleTimeString("fr-FR", {
        hour: "2-digit",
        minute: "2-digit",
      }),
      value: d.toISOString(),
    });
  }
  return out;
}

export function CheckoutForm({ userName }: Props) {
  const router = useRouter();
  const { lines, totalCents, itemCount, clear } = useCart();
  const slots = useMemo(() => nextSlots(), []);
  const [slot, setSlot] = useState(slots[0]?.value ?? "");
  const [pending, setPending] = useState(false);
  const [error, setError] = useState<string | null>(null);

  if (itemCount === 0) {
    return (
      <div className="kebaba-card space-y-4 p-6 text-center">
        <p className="text-[var(--kebaba-ink-muted)]">Votre panier est vide.</p>
        <Link href="/menu" className="kebaba-btn inline-flex">
          Voir le menu
        </Link>
      </div>
    );
  }

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    setPending(true);
    setError(null);
    try {
      const res = await fetch("/api/orders", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          paymentMode: "on_site",
          pickupSlotStart: slot || null,
          lines: lines.map((l) => ({
            productId: l.productId,
            name: l.name,
            unitPriceCents: l.unitPriceCents,
            quantity: l.quantity,
            options: l.options.map((o) => ({
              optionId: o.optionId,
              name: o.name,
              priceCents: o.priceCents,
            })),
          })),
        }),
      });
      const data = (await res.json()) as {
        id?: string;
        error?: string;
      };
      if (!res.ok || !data.id) {
        setError(data.error ?? "Commande impossible.");
        setPending(false);
        return;
      }
      clear();
      router.push(`/commandes/${data.id}`);
    } catch {
      setError("Erreur réseau. Réessayez.");
      setPending(false);
    }
  }

  return (
    <form className="space-y-6" onSubmit={onSubmit}>
      <div className="kebaba-card space-y-3 p-5">
        <p className="text-sm text-[var(--kebaba-ink-muted)]">
          Bonjour {userName?.split(" ")[0] ?? ""} — click &amp; collect, paiement
          au comptoir.
        </p>
        <ul className="space-y-2 text-sm">
          {lines.map((line) => (
            <li key={line.key} className="flex justify-between gap-3">
              <span>
                {line.quantity}× {line.name}
                {line.options.length > 0 ? (
                  <span className="block text-xs text-[var(--kebaba-ink-muted)]">
                    {line.options.map((o) => o.name).join(" · ")}
                  </span>
                ) : null}
              </span>
              <span className="font-semibold">{formatEur(lineTotal(line))}</span>
            </li>
          ))}
        </ul>
        <div className="flex justify-between border-t border-[var(--kebaba-bordeaux)]/15 pt-3 font-bold">
          <span>Total</span>
          <span className="text-[var(--kebaba-bordeaux)]">
            {formatEur(totalCents)}
          </span>
        </div>
      </div>

      <fieldset className="kebaba-card space-y-3 p-5">
        <legend className="font-display text-lg font-bold text-[var(--kebaba-bordeaux)]">
          Créneau de retrait
        </legend>
        <div className="flex flex-wrap gap-2">
          {slots.map((s) => (
            <button
              key={s.value}
              type="button"
              onClick={() => setSlot(s.value)}
              className={`rounded-xl border px-3 py-2 text-sm font-semibold ${
                slot === s.value
                  ? "border-[var(--kebaba-bordeaux)] bg-[var(--kebaba-bordeaux)] text-white"
                  : "border-[var(--kebaba-bordeaux)]/40 text-[var(--kebaba-bordeaux)]"
              }`}
            >
              {s.label}
            </button>
          ))}
        </div>
      </fieldset>

      <fieldset className="kebaba-card space-y-2 p-5">
        <legend className="font-display text-lg font-bold text-[var(--kebaba-bordeaux)]">
          Paiement
        </legend>
        <p className="text-sm text-[var(--kebaba-ink-muted)]">
          Sur place au comptoir (CB / espèces). Le paiement en ligne arrive
          bientôt.
        </p>
      </fieldset>

      {error ? (
        <p className="text-sm font-semibold text-red-700" role="alert">
          {error}
        </p>
      ) : null}

      <button type="submit" className="kebaba-btn w-full" disabled={pending}>
        {pending
          ? "Envoi…"
          : `Confirmer · ${formatEur(totalCents)}`}
      </button>
      <Link
        href="/panier"
        className="block text-center text-sm font-semibold text-[var(--kebaba-bordeaux)] underline-offset-2 hover:underline"
      >
        Modifier le panier
      </Link>
    </form>
  );
}
