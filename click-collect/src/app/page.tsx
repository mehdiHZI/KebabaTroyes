import Link from "next/link";

export default function HomePage() {
  return (
    <div className="flex flex-col gap-10 py-4">
      <section className="relative overflow-hidden rounded-3xl border border-[var(--kebaba-bordeaux)]/30 bg-[var(--kebaba-card)] px-6 py-14 text-center shadow-sm sm:px-12 sm:py-20">
        <div
          aria-hidden
          className="pointer-events-none absolute inset-0 opacity-40"
          style={{
            backgroundImage:
              "radial-gradient(circle, color-mix(in srgb, var(--kebaba-bordeaux) 35%, transparent) 1.2px, transparent 1.3px)",
            backgroundSize: "10px 10px",
            maskImage:
              "radial-gradient(ellipse 80% 70% at 50% 0%, black 20%, transparent 70%)",
          }}
        />
        <p className="relative font-display text-5xl font-bold tracking-wide text-[var(--kebaba-bordeaux)] sm:text-6xl">
          KEBABA
        </p>
        <h1 className="relative mt-4 text-xl font-bold text-[var(--kebaba-ink)] sm:text-2xl">
          Click &amp; Collect
        </h1>
        <p className="relative mx-auto mt-3 max-w-md text-[var(--kebaba-ink-muted)]">
          Commandez en ligne, retirez au restaurant. Compte obligatoire pour
          valider — paiement en ligne ou au retrait.
        </p>
        <div className="relative mt-8 flex flex-wrap items-center justify-center gap-3">
          <Link href="/menu" className="kebaba-btn">
            Voir le menu
          </Link>
          <Link href="/compte" className="kebaba-btn-outline">
            Mon compte
          </Link>
        </div>
      </section>

      <section className="grid gap-4 sm:grid-cols-3">
        {[
          {
            title: "1. Composez",
            text: "Choisissez pain, sauces et crudités comme sur place.",
          },
          {
            title: "2. Réservez",
            text: "Créneau de retrait + compte client.",
          },
          {
            title: "3. Récupérez",
            text: "Payez en ligne ou au comptoir, à votre convenance.",
          },
        ].map((step) => (
          <article key={step.title} className="kebaba-card p-5">
            <h2 className="font-display text-lg font-bold text-[var(--kebaba-bordeaux)]">
              {step.title}
            </h2>
            <p className="mt-2 text-sm text-[var(--kebaba-ink-muted)]">{step.text}</p>
          </article>
        ))}
      </section>
    </div>
  );
}
