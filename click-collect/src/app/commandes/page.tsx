import Link from "next/link";
import { redirect } from "next/navigation";
import { auth } from "@/auth";
import { OrderStatusBadge } from "@/components/orders/OrderStatusBadge";
import { formatEur } from "@/lib/money";
import { listOrdersForUser } from "@/lib/orders";

export const dynamic = "force-dynamic";

export const metadata = {
  title: "Mes commandes",
};

export default async function CommandesPage() {
  const session = await auth();
  if (!session?.user?.id) {
    redirect("/compte?callbackUrl=/commandes");
  }

  const rows = await listOrdersForUser(session.user.id);

  return (
    <div className="mx-auto max-w-xl space-y-6 pb-16">
      <div className="flex items-end justify-between gap-3">
        <h1 className="font-display text-3xl font-bold uppercase text-[var(--kebaba-bordeaux)]">
          Mes commandes
        </h1>
        <Link
          href="/compte"
          className="text-sm font-semibold text-[var(--kebaba-bordeaux)] underline-offset-2 hover:underline"
        >
          Compte
        </Link>
      </div>

      {rows.length === 0 ? (
        <div className="kebaba-card space-y-4 p-8 text-center">
          <p className="text-[var(--kebaba-ink-muted)]">
            Aucune commande pour l’instant.
          </p>
          <Link href="/menu" className="kebaba-btn inline-flex">
            Voir le menu
          </Link>
        </div>
      ) : (
        <ul className="space-y-3">
          {rows.map((order) => (
            <li key={order.id}>
              <Link
                href={`/commandes/${order.id}`}
                className="kebaba-card flex items-center justify-between gap-3 p-4 transition hover:border-[var(--kebaba-bordeaux)]/50"
              >
                <div className="space-y-1">
                  <p className="font-bold text-[var(--kebaba-bordeaux)]">
                    {order.publicNumber}
                  </p>
                  <p className="text-xs text-[var(--kebaba-ink-muted)]">
                    {order.createdAt.toLocaleString("fr-FR", {
                      dateStyle: "medium",
                      timeStyle: "short",
                    })}
                  </p>
                  <OrderStatusBadge status={order.status} />
                </div>
                <p className="font-display text-lg font-bold">
                  {formatEur(order.totalCents)}
                </p>
              </Link>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
