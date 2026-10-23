import { orderStatusLabel } from "@/lib/order-status";

const TONE: Record<string, string> = {
  pending_payment: "bg-amber-100 text-amber-900",
  pending_acceptance: "bg-amber-100 text-amber-900",
  accepted: "bg-sky-100 text-sky-900",
  preparing: "bg-orange-100 text-orange-900",
  ready: "bg-emerald-100 text-emerald-900",
  picked_up: "bg-stone-200 text-stone-700",
  refused: "bg-red-100 text-red-800",
  cancelled: "bg-stone-200 text-stone-600",
};

export function OrderStatusBadge({ status }: { status: string }) {
  const tone = TONE[status] ?? "bg-stone-100 text-stone-700";
  return (
    <span
      className={`inline-flex rounded-full px-2.5 py-1 text-xs font-bold ${tone}`}
    >
      {orderStatusLabel(status)}
    </span>
  );
}
