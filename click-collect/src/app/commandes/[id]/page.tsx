import Link from "next/link";
import { notFound, redirect } from "next/navigation";
import { auth } from "@/auth";
import { OrderStatusBadge } from "@/components/orders/OrderStatusBadge";
import { OrderStatusRefresh } from "@/components/orders/OrderStatusRefresh";
import { OrderStatusTimeline } from "@/components/orders/OrderStatusTimeline";
import { formatEur } from "@/lib/money";
import { getOrderForUser } from "@/lib/orders";

export const dynamic = "force-dynamic";

type Props = {
  params: Promise<{ id: string }>;
};

export async function generateMetadata({ params }: Props) {
  const { id } = await params;
  return { title: `Commande ${id.slice(0, 8)}` };
}

export default async function CommandeDetailPage({ params }: Props) {
  const session = await auth();
  if (!session?.user?.id) {
    redirect("/compte");
  }

  const { id } = await params;
  const order = await getOrderForUser(session.user.id, id);
  if (!order) notFound();

  return (
    <div className="mx-auto max-w-xl space-y-6 pb-16">
      <OrderStatusRefresh status={order.status} />

      <Link
        href="/commandes"
        className="text-sm font-semibold text-[var(--kebaba-bordeaux)] hover:underline"
      >
        ← Mes commandes
      </Link>

      <header className="kebaba-card space-y-3 p-5">
        <div className="flex flex-wrap items-start justify-between gap-3">
          <div>
            <p className="text-xs font-semibold uppercase tracking-wide text-[var(--kebaba-ink-muted)]">
              Commande
            </p>
            <h1 className="font-display text-2xl font-bold text-[var(--kebaba-bordeaux)]">
              {order.publicNumber}
            </h1>
            <p className="mt-1 text-sm text-[var(--kebaba-ink-muted)]">
              {order.createdAt.toLocaleString("fr-FR", {
                dateStyle: "full",
                timeStyle: "short",
              })}
            </p>
          </div>
          <OrderStatusBadge status={order.status} />
        </div>
        {order.pickupSlotStart ? (
          <p className="text-sm">
            Retrait prévu vers{" "}
            <strong>
              {order.pickupSlotStart.toLocaleTimeString("fr-FR", {
                hour: "2-digit",
                minute: "2-digit",
              })}
            </strong>
          </p>
        ) : null}
        <p className="text-sm text-[var(--kebaba-ink-muted)]">
          {order.paymentMode === "on_site"
            ? "Paiement au comptoir"
            : "Paiement en ligne"}{" "}
          · {formatEur(order.totalCents)}
        </p>
      </header>

      <section className="kebaba-card space-y-4 p-5">
        <h2 className="font-display text-lg font-bold text-[var(--kebaba-bordeaux)]">
          Suivi
        </h2>
        <OrderStatusTimeline
          status={order.status}
          refuseReason={order.refuseReason}
        />
        <p className="text-xs text-[var(--kebaba-ink-muted)]">
          L’état se met à jour automatiquement quand le restaurant avance la
          commande.
        </p>
      </section>

      <section className="kebaba-card space-y-3 p-5">
        <h2 className="font-display text-lg font-bold text-[var(--kebaba-bordeaux)]">
          Détail
        </h2>
        <ul className="space-y-3 text-sm">
          {order.items.map((item) => (
            <li key={item.id} className="flex justify-between gap-3">
              <div>
                <p className="font-semibold">
                  {item.quantity}× {item.productName}
                </p>
                {item.optionsJson && item.optionsJson.length > 0 ? (
                  <p className="text-xs text-[var(--kebaba-ink-muted)]">
                    {item.optionsJson.map((o) => o.name).join(" · ")}
                  </p>
                ) : null}
              </div>
              <p className="font-semibold">{formatEur(item.lineTotalCents)}</p>
            </li>
          ))}
        </ul>
        <div className="flex justify-between border-t border-[var(--kebaba-bordeaux)]/15 pt-3 font-bold">
          <span>Total</span>
          <span className="text-[var(--kebaba-bordeaux)]">
            {formatEur(order.totalCents)}
          </span>
        </div>
      </section>
    </div>
  );
}
