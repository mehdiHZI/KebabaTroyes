import Image from "next/image";
import Link from "next/link";
import { HomePresentation } from "@/components/HomePresentation";
import { BRAND_BANNER } from "@/lib/product-image";

export default function HomePage() {
  return (
    <div className="flex flex-col gap-10 py-4">
      {/* Hero — ne pas modifier sans demande explicite */}
      <section className="overflow-hidden rounded-3xl border border-[var(--kebaba-bordeaux)]/25 bg-[var(--kebaba-bordeaux-deep)] shadow-sm">
        <div className="relative aspect-[16/10] w-full sm:aspect-[21/9]">
          <Image
            src={BRAND_BANNER}
            alt="Kebaba Döner & Co"
            fill
            priority
            sizes="100vw"
            className="object-cover object-center"
          />
        </div>

        <div className="flex flex-col items-center gap-4 bg-[var(--kebaba-bordeaux)] px-6 py-8 text-center text-white sm:px-10 sm:py-10">
          <h1 className="font-display text-2xl font-bold tracking-wide sm:text-3xl">
            Click &amp; Collect
          </h1>
          <p className="max-w-lg text-sm text-white/95 sm:text-base">
            Tes kebabs préférés, prêts pile à l’heure. Tu commandes en 2
            minutes — on s’occupe du reste.
          </p>
          <div className="mt-1 flex w-full max-w-md flex-col gap-3 sm:flex-row sm:justify-center">
            <Link
              href="/menu"
              className="inline-flex flex-1 items-center justify-center rounded-xl bg-white px-5 py-3 text-center text-base font-bold text-[var(--kebaba-bordeaux)] transition hover:bg-[var(--kebaba-cream)]"
            >
              Voir le menu
            </Link>
            <Link
              href="/compte"
              className="inline-flex flex-1 items-center justify-center rounded-xl border-2 border-white bg-transparent px-5 py-3 text-center text-base font-bold text-white transition hover:bg-white/15"
            >
              Mon compte
            </Link>
          </div>
        </div>
      </section>

      <HomePresentation />
    </div>
  );
}
