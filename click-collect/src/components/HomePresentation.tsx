import Image from "next/image";
import Link from "next/link";
import { homePresentation as c } from "@/content/home-presentation";

export function HomePresentation() {
  return (
    <div className="flex flex-col gap-14 pb-8 sm:gap-20">
      {/* À propos */}
      <section className="grid items-center gap-8 lg:grid-cols-2 lg:gap-12">
        <div className="relative aspect-[4/3] overflow-hidden rounded-3xl border border-[var(--kebaba-bordeaux)]/25 shadow-sm">
          <Image
            src={c.about.image}
            alt={c.about.imageAlt}
            fill
            sizes="(max-width: 1024px) 100vw, 50vw"
            className="object-cover"
          />
        </div>
        <div className="space-y-4">
          <p className="text-sm font-bold uppercase tracking-widest text-[var(--kebaba-bordeaux)]">
            {c.about.eyebrow}
          </p>
          <h2 className="font-display text-3xl font-bold leading-tight text-[var(--kebaba-bordeaux)] sm:text-4xl">
            {c.about.title}
          </h2>
          <p className="text-base leading-relaxed text-[var(--kebaba-ink-muted)] sm:text-lg">
            {c.about.text}
          </p>
        </div>
      </section>

      {/* Points forts */}
      <section className="space-y-6">
        <h2 className="text-center font-display text-2xl font-bold uppercase tracking-wide text-[var(--kebaba-bordeaux)] sm:text-3xl">
          Pourquoi Kebaba
        </h2>
        <div className="grid gap-5 sm:grid-cols-3">
          {c.highlights.map((item) => (
            <article
              key={item.title}
              className="kebaba-card overflow-hidden transition hover:-translate-y-0.5 hover:shadow-md"
            >
              <div className="relative aspect-[4/3] bg-[var(--kebaba-cream-deep)]">
                <Image
                  src={item.image}
                  alt={item.imageAlt}
                  fill
                  sizes="(max-width: 640px) 100vw, 33vw"
                  className="object-cover"
                />
              </div>
              <div className="space-y-2 p-5">
                <h3 className="font-display text-lg font-bold text-[var(--kebaba-bordeaux)]">
                  {item.title}
                </h3>
                <p className="text-sm leading-relaxed text-[var(--kebaba-ink-muted)]">
                  {item.text}
                </p>
              </div>
            </article>
          ))}
        </div>
      </section>

      {/* Story + CTA */}
      <section className="overflow-hidden rounded-3xl border border-[var(--kebaba-bordeaux)]/25 bg-[var(--kebaba-card)] shadow-sm lg:grid lg:grid-cols-2">
        <div className="relative order-2 aspect-[4/3] lg:order-1 lg:aspect-auto lg:min-h-[320px]">
          <Image
            src={c.story.image}
            alt={c.story.imageAlt}
            fill
            sizes="(max-width: 1024px) 100vw, 50vw"
            className="object-cover"
          />
        </div>
        <div className="order-1 flex flex-col justify-center gap-4 p-8 sm:p-10 lg:order-2">
          <h2 className="font-display text-2xl font-bold text-[var(--kebaba-bordeaux)] sm:text-3xl">
            {c.story.title}
          </h2>
          <p className="text-base leading-relaxed text-[var(--kebaba-ink-muted)]">
            {c.story.text}
          </p>
          <Link href={c.story.ctaHref} className="kebaba-btn mt-2 w-fit">
            {c.story.ctaLabel}
          </Link>
        </div>
      </section>
    </div>
  );
}
