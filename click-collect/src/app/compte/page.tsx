import Link from "next/link";
import { auth, googleEnabled } from "@/auth";
import { CompteAuth } from "@/components/auth/CompteAuth";

export const dynamic = "force-dynamic";

export const metadata = {
  title: "Compte",
};

type Props = {
  searchParams: Promise<{ error?: string }>;
};

export default async function ComptePage({ searchParams }: Props) {
  const session = await auth();
  const { error } = await searchParams;

  return (
    <div className="mx-auto max-w-md space-y-6 pb-16">
      <header className="space-y-2">
        <h1 className="font-display text-3xl font-bold uppercase text-[var(--kebaba-bordeaux)]">
          Mon compte
        </h1>
        <p className="text-[var(--kebaba-ink-muted)]">
          Créez un compte ou connectez-vous pour passer commande.
        </p>
      </header>

      <CompteAuth
        user={
          session?.user
            ? {
                id: session.user.id,
                name: session.user.name,
                email: session.user.email,
                image: session.user.image,
              }
            : null
        }
        googleEnabled={googleEnabled}
        errorCode={error}
      />

      <Link href="/menu" className="kebaba-btn-outline inline-flex">
        Continuer vers le menu
      </Link>
    </div>
  );
}
