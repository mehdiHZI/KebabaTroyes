"use client";

import Link from "next/link";
import { useSession } from "next-auth/react";
import { useCart } from "@/store/cart";
import { formatEur } from "@/lib/money";

export function SiteHeader() {
  const { itemCount, totalCents } = useCart();
  const { data: session } = useSession();

  return (
    <header className="sticky top-0 z-40 border-b border-[var(--kebaba-bordeaux)]/20 bg-[var(--kebaba-cream)]/95 backdrop-blur">
      <div className="mx-auto flex h-14 max-w-5xl items-center justify-between gap-4 px-4">
        <Link
          href="/"
          className="font-display text-xl font-extrabold tracking-wide text-[var(--kebaba-bordeaux)]"
        >
          KEBABA
        </Link>
        <nav className="flex items-center gap-4 text-sm font-semibold text-[var(--kebaba-bordeaux)]">
          <Link href="/menu" className="hover:opacity-80">
            Menu
          </Link>
          {session?.user ? (
            <Link href="/commandes" className="hover:opacity-80">
              Commandes
            </Link>
          ) : null}
          <Link href="/compte" className="hover:opacity-80">
            {session?.user ? session.user.name?.split(" ")[0] || "Compte" : "Compte"}
          </Link>
          <Link
            href="/panier"
            className="inline-flex items-center gap-2 rounded-xl bg-[var(--kebaba-bordeaux)] px-3 py-1.5 text-white"
          >
            Panier
            {itemCount > 0 && (
              <span className="rounded-lg bg-white/20 px-1.5 text-xs">
                {itemCount} · {formatEur(totalCents)}
              </span>
            )}
          </Link>
        </nav>
      </div>
    </header>
  );
}
