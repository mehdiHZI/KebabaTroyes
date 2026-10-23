"use client";

import { formatEur } from "@/lib/money";
import { UPSELL_EXTRAS, type UpsellExtra } from "@/lib/upsell";

type Props = {
  open: boolean;
  selectedIds: string[];
  onToggle: (id: string) => void;
  onSkip: () => void;
  onConfirm: () => void;
};

export function UpsellExtrasModal({
  open,
  selectedIds,
  onToggle,
  onSkip,
  onConfirm,
}: Props) {
  if (!open) return null;

  const extrasTotal = UPSELL_EXTRAS.filter((e) =>
    selectedIds.includes(e.id),
  ).reduce((s, e) => s + e.priceCents, 0);

  return (
    <div
      className="fixed inset-0 z-50 flex items-end justify-center bg-black/50 p-4 sm:items-center"
      role="dialog"
      aria-modal="true"
      aria-labelledby="upsell-title"
    >
      <div className="kebaba-card w-full max-w-md overflow-hidden shadow-xl">
        <div className="border-b border-[var(--kebaba-bordeaux)]/15 bg-[var(--kebaba-bordeaux)] px-5 py-4 text-white">
          <p className="text-xs font-semibold uppercase tracking-wider text-white/80">
            Un dernier geste
          </p>
          <h2 id="upsell-title" className="font-display text-xl font-bold">
            Les petits + qui font la diff
          </h2>
        </div>

        <div className="grid grid-cols-2 gap-3 p-4">
          {UPSELL_EXTRAS.map((extra) => (
            <UpsellTile
              key={extra.id}
              extra={extra}
              active={selectedIds.includes(extra.id)}
              onToggle={() => onToggle(extra.id)}
            />
          ))}
        </div>

        <div className="flex flex-col gap-2 border-t border-[var(--kebaba-bordeaux)]/15 p-4">
          <button type="button" className="kebaba-btn w-full" onClick={onConfirm}>
            {selectedIds.length > 0
              ? `Ajouter · +${formatEur(extrasTotal)}`
              : "Continuer sans +"}
          </button>
          {selectedIds.length > 0 ? (
            <button
              type="button"
              onClick={onSkip}
              className="text-center text-sm font-semibold text-[var(--kebaba-ink-muted)] underline-offset-2 hover:underline"
            >
              Non merci
            </button>
          ) : null}
        </div>
      </div>
    </div>
  );
}

function UpsellTile({
  extra,
  active,
  onToggle,
}: {
  extra: UpsellExtra;
  active: boolean;
  onToggle: () => void;
}) {
  return (
    <button
      type="button"
      onClick={onToggle}
      className={`relative flex flex-col items-start gap-1 rounded-2xl border-2 p-3 text-left transition ${
        active
          ? "border-[var(--kebaba-bordeaux)] bg-[var(--kebaba-bordeaux)]/10"
          : "border-[var(--kebaba-bordeaux)]/25 bg-[var(--kebaba-cream)]"
      }`}
    >
      <span
        className={`absolute right-2 top-2 flex h-8 w-8 items-center justify-center rounded-full text-lg font-bold ${
          active
            ? "bg-[var(--kebaba-bordeaux)] text-white"
            : "bg-[var(--kebaba-bordeaux)]/15 text-[var(--kebaba-bordeaux)]"
        }`}
        aria-hidden
      >
        {active ? "✓" : "+"}
      </span>
      <span className="pr-8 font-display text-sm font-bold uppercase leading-tight text-[var(--kebaba-bordeaux)]">
        {extra.name}
      </span>
      <span className="text-xs text-[var(--kebaba-ink-muted)]">{extra.blurb}</span>
      <span className="mt-1 text-sm font-bold text-[var(--kebaba-bordeaux)]">
        +{formatEur(extra.priceCents)}
      </span>
    </button>
  );
}
