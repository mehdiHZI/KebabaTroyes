import Link from "next/link";
import { auth } from "@/auth";

export const dynamic = "force-dynamic";

export const metadata = {
  title: "Commande",
};

export default async function CommandePage() {
  const session = await auth();

  return (
    <div className="mx-auto max-w-lg space-y-6 pb-16">
      <h1 className="font-display text-3xl font-bold uppercase text-[var(--kebaba-bordeaux)]">
        Finaliser
      </h1>
      <div className="kebaba-card space-y-4 p-6">
        {session?.user ? (
          <>
            <p className="text-[var(--kebaba-ink-muted)]">
              Bonjour {session.user.name?.split(" ")[0] ?? ""} ! Votre compte est
              prêt — la validation de commande (créneau et paiement) sera
              disponible très bientôt.
            </p>
            <Link href="/panier" className="kebaba-btn-outline inline-flex">
              Retour au panier
            </Link>
          </>
        ) : (
          <>
            <p className="text-[var(--kebaba-ink-muted)]">
              Connectez-vous ou créez un compte pour valider votre commande click
              &amp; collect.
            </p>
            <div className="flex flex-col gap-3 sm:flex-row">
              <Link href="/compte" className="kebaba-btn flex-1 text-center">
                Se connecter
              </Link>
              <Link href="/panier" className="kebaba-btn-outline flex-1 text-center">
                Retour panier
              </Link>
            </div>
          </>
        )}
      </div>
    </div>
  );
}
