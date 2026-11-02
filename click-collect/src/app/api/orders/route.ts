import { NextResponse } from "next/server";
import { z } from "zod";
import { auth } from "@/auth";
import { createOrder, listOrdersForUser } from "@/lib/orders";
import { PricingError } from "@/lib/pricing";

export const dynamic = "force-dynamic";

const createSchema = z.object({
  paymentMode: z.enum(["on_site", "online"]).default("on_site"),
  pickupSlotStart: z.string().datetime().optional().nullable(),
  lines: z
    .array(
      z.object({
        productId: z.string().min(1),
        quantity: z.number().int().positive().max(50),
        /** Identifiants d’options uniquement — les prix client sont ignorés. */
        optionIds: z.array(z.string().min(1)).max(40).default([]),
      }),
    )
    .min(1)
    .max(40),
});

export async function GET() {
  const session = await auth();
  if (!session?.user?.id) {
    return NextResponse.json({ error: "Non connecté." }, { status: 401 });
  }
  const rows = await listOrdersForUser(session.user.id);
  return NextResponse.json({
    orders: rows.map((o) => ({
      ...o,
      createdAt: o.createdAt.toISOString(),
      pickupSlotStart: o.pickupSlotStart?.toISOString() ?? null,
    })),
  });
}

export async function POST(req: Request) {
  const session = await auth();
  if (!session?.user?.id) {
    return NextResponse.json({ error: "Non connecté." }, { status: 401 });
  }

  let body: unknown;
  try {
    body = await req.json();
  } catch {
    return NextResponse.json({ error: "JSON invalide." }, { status: 400 });
  }

  const parsed = createSchema.safeParse(normalizeLegacyBody(body));
  if (!parsed.success) {
    return NextResponse.json({ error: "Panier invalide." }, { status: 400 });
  }

  if (parsed.data.paymentMode === "online") {
    return NextResponse.json(
      {
        error:
          "Paiement en ligne bientôt disponible — choisissez le paiement sur place.",
      },
      { status: 400 },
    );
  }

  try {
    const order = await createOrder({
      userId: session.user.id,
      paymentMode: parsed.data.paymentMode,
      pickupSlotStart: parsed.data.pickupSlotStart
        ? new Date(parsed.data.pickupSlotStart)
        : null,
      lines: parsed.data.lines,
    });
    return NextResponse.json(order, { status: 201 });
  } catch (e) {
    if (e instanceof PricingError) {
      return NextResponse.json({ error: e.message, code: e.code }, { status: 400 });
    }
    console.error(e);
    return NextResponse.json({ error: "Création impossible." }, { status: 500 });
  }
}

/**
 * Accepte encore l’ancien format `{ options: [{ optionId, name?, priceCents? }] }`
 * et le normalise en `optionIds` (prix / noms client jetés).
 */
function normalizeLegacyBody(body: unknown): unknown {
  if (!body || typeof body !== "object") return body;
  const b = body as Record<string, unknown>;
  if (!Array.isArray(b.lines)) return body;

  return {
    ...b,
    lines: b.lines.map((line) => {
      if (!line || typeof line !== "object") return line;
      const l = line as Record<string, unknown>;
      if (Array.isArray(l.optionIds)) {
        return {
          productId: l.productId,
          quantity: l.quantity,
          optionIds: l.optionIds,
        };
      }
      const options = Array.isArray(l.options) ? l.options : [];
      const optionIds = options
        .map((o) =>
          o && typeof o === "object"
            ? (o as { optionId?: unknown }).optionId
            : undefined,
        )
        .filter((id): id is string => typeof id === "string" && id.length > 0);
      return {
        productId: l.productId,
        quantity: l.quantity,
        optionIds,
      };
    }),
  };
}
