/** Libellés FR + étapes de suivi commande. */

export const ORDER_STATUSES = [
  "pending_payment",
  "pending_acceptance",
  "accepted",
  "preparing",
  "ready",
  "picked_up",
  "refused",
  "cancelled",
] as const;

export type OrderStatus = (typeof ORDER_STATUSES)[number];

const LABELS: Record<OrderStatus, string> = {
  pending_payment: "Paiement en attente",
  pending_acceptance: "En attente du restaurant",
  accepted: "Acceptée",
  preparing: "En préparation",
  ready: "Prête à retirer",
  picked_up: "Récupérée",
  refused: "Refusée",
  cancelled: "Annulée",
};

/** Étapes « heureuses » pour la timeline client (hors refus / annulation). */
export const STATUS_PIPELINE: OrderStatus[] = [
  "pending_acceptance",
  "accepted",
  "preparing",
  "ready",
  "picked_up",
];

export function orderStatusLabel(status: string): string {
  return LABELS[status as OrderStatus] ?? status;
}

export function isTerminalStatus(status: string): boolean {
  return status === "picked_up" || status === "refused" || status === "cancelled";
}

export function pipelineIndex(status: string): number {
  if (status === "pending_payment") return -1;
  const i = STATUS_PIPELINE.indexOf(status as OrderStatus);
  return i;
}
