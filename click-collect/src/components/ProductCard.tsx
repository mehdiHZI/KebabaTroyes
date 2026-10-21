"use client";

import Link from "next/link";
import { formatEur } from "@/lib/money";

type Props = {
  id: string;
  name: string;
  priceCents: number;
  description?: string | null;
};

export function ProductCard({ id, name, priceCents, description }: Props) {
  return (
    <Link
      href={`/menu/${id}`}
      className="kebaba-card group flex flex-col gap-2 p-4 transition hover:-translate-y-0.5 hover:shadow-md"
    >
      <div className="flex items-start justify-between gap-3">
        <h3 className="font-display text-base font-bold uppercase tracking-wide text-[var(--kebaba-bordeaux)]">
          {name}
        </h3>
        <span className="shrink-0 rounded-lg bg-[var(--kebaba-bordeaux)] px-2.5 py-1 text-sm font-bold text-white">
          {formatEur(priceCents)}
        </span>
      </div>
      {description ? (
        <p className="text-sm text-[var(--kebaba-ink-muted)]">{description}</p>
      ) : null}
      <span className="mt-auto text-xs font-semibold text-[var(--kebaba-bordeaux)] opacity-0 transition group-hover:opacity-100">
        Personnaliser →
      </span>
    </Link>
  );
}
