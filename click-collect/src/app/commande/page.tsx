import Link from "next/link";
import { auth } from "@/auth";
import { CheckoutForm } from "@/components/checkout/CheckoutForm";

export const dynamic = "force-dynamic";

export const metadata = {
  title: "Finaliser la commande",
};

export default async function CommandePage() {
  const session = await auth();

  return (
    <div className="mx-auto max-w-lg space-y-6 pb-16">
      <h1 className="font-display text-3xl font-bold uppercase text-[var(--kebaba-bordeaux)]">
        Finaliser
      </h1>

      {session?.user ? (
        <CheckoutForm userName={session.user.name} />
      ) : (
        <div className="kebaba-card space-y-4 p-6">
          <p className="text-[var(--kebaba-ink-muted)]">
            Connectez-vous ou créez un compte pour valider votre commande click
            &amp; collect et suivre son état.
          </p>
          <div className="flex flex-col gap-3 sm:flex-row">
            <Link href="/compte" className="kebaba-btn flex-1 text-center">
              Se connecter
            </Link>
            <Link href="/panier" className="kebaba-btn-outline flex-1 text-center">
              Retour panier
            </Link>
          </div>
        </div>
      )}
    </div>
  );
}
