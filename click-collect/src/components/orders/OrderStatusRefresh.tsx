"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { isTerminalStatus } from "@/lib/order-status";

/** Rafraîchit la page tant que la commande n’est pas terminée. */
export function OrderStatusRefresh({ status }: { status: string }) {
  const router = useRouter();

  useEffect(() => {
    if (isTerminalStatus(status)) return;
    const id = window.setInterval(() => {
      router.refresh();
    }, 12_000);
    return () => window.clearInterval(id);
  }, [status, router]);

  return null;
}
