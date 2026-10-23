import { NextResponse } from "next/server";
import { z } from "zod";
import { auth } from "@/auth";
import { createOrder, listOrdersForUser } from "@/lib/orders";

export const dynamic = "force-dynamic";

const lineSchema = z.object({
  productId: z.string().min(1),
  name: z.string().min(1),
  unitPriceCents: z.number().int().nonnegative(),
  quantity: z.number().int().positive().max(50),
  options: z
    .array(
      z.object({
        optionId: z.string(),
        name: z.string(),
        priceCents: z.number().int(),
      }),
    )
    .default([]),
});

const createSchema = z.object({
  paymentMode: z.enum(["on_site", "online"]).default("on_site"),
  pickupSlotStart: z.string().datetime().optional().nullable(),
  lines: z.array(lineSchema).min(1).max(40),
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

  const parsed = createSchema.safeParse(body);
  if (!parsed.success) {
    return NextResponse.json({ error: "Panier invalide." }, { status: 400 });
  }

  if (parsed.data.paymentMode === "online") {
    return NextResponse.json(
      { error: "Paiement en ligne bientôt disponible — choisissez le paiement sur place." },
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
    if (e instanceof Error && e.message === "EMPTY_CART") {
      return NextResponse.json({ error: "Panier vide." }, { status: 400 });
    }
    console.error(e);
    return NextResponse.json({ error: "Création impossible." }, { status: 500 });
  }
}
