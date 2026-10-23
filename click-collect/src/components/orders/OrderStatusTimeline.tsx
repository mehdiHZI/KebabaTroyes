import {
  isTerminalStatus,
  orderStatusLabel,
  pipelineIndex,
  STATUS_PIPELINE,
} from "@/lib/order-status";

type Props = {
  status: string;
  refuseReason?: string | null;
};

export function OrderStatusTimeline({ status, refuseReason }: Props) {
  if (status === "refused" || status === "cancelled") {
    return (
      <div className="space-y-2 rounded-xl bg-red-50 p-4 text-sm text-red-900">
        <p className="font-bold">{orderStatusLabel(status)}</p>
        {refuseReason ? <p>{refuseReason}</p> : null}
      </div>
    );
  }

  const current = pipelineIndex(status);
  const done = isTerminalStatus(status);

  return (
    <ol className="space-y-3">
      {STATUS_PIPELINE.map((step, i) => {
        const reached = current >= i || (done && step === "picked_up");
        const active = current === i;
        return (
          <li key={step} className="flex items-start gap-3">
            <span
              className={`mt-0.5 flex h-7 w-7 shrink-0 items-center justify-center rounded-full text-xs font-bold ${
                reached
                  ? "bg-[var(--kebaba-bordeaux)] text-white"
                  : "bg-[var(--kebaba-bordeaux)]/15 text-[var(--kebaba-bordeaux)]/50"
              }`}
            >
              {reached ? "✓" : i + 1}
            </span>
            <div>
              <p
                className={`font-semibold ${
                  active
                    ? "text-[var(--kebaba-bordeaux)]"
                    : reached
                      ? "text-[var(--kebaba-ink)]"
                      : "text-[var(--kebaba-ink-muted)]"
                }`}
              >
                {orderStatusLabel(step)}
              </p>
              {active && !done ? (
                <p className="text-xs text-[var(--kebaba-ink-muted)]">
                  Étape en cours
                </p>
              ) : null}
            </div>
          </li>
        );
      })}
    </ol>
  );
}
