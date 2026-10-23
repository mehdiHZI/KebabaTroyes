import { and, desc, eq } from "drizzle-orm";
import { db } from "@/db";
import { orderEvents, orderItems, orders } from "@/db/schema";

export type OrderListItem = {
  id: string;
  publicNumber: string;
  status: string;
  paymentStatus: string;
  paymentMode: string;
  totalCents: number;
  createdAt: Date;
  pickupSlotStart: Date | null;
};

export type OrderDetail = OrderListItem & {
  refuseReason: string | null;
  subtotalCents: number;
  items: {
    id: string;
    productName: string;
    quantity: number;
    lineTotalCents: number;
    optionsJson: { optionId: string; name: string; priceCents: number }[] | null;
  }[];
  events: {
    id: string;
    type: string;
    createdAt: Date;
  }[];
};

function publicNumber(): string {
  const t = Date.now().toString(36).toUpperCase();
  return `KB-${t.slice(-6)}`;
}

export async function listOrdersForUser(userId: string): Promise<OrderListItem[]> {
  const rows = await db
    .select({
      id: orders.id,
      publicNumber: orders.publicNumber,
      status: orders.status,
      paymentStatus: orders.paymentStatus,
      paymentMode: orders.paymentMode,
      totalCents: orders.totalCents,
      createdAt: orders.createdAt,
      pickupSlotStart: orders.pickupSlotStart,
    })
    .from(orders)
    .where(eq(orders.userId, userId))
    .orderBy(desc(orders.createdAt));
  return rows;
}

export async function getOrderForUser(
  userId: string,
  orderId: string,
): Promise<OrderDetail | null> {
  const [row] = await db
    .select()
    .from(orders)
    .where(and(eq(orders.id, orderId), eq(orders.userId, userId)))
    .limit(1);
  if (!row) return null;

  const items = await db
    .select({
      id: orderItems.id,
      productName: orderItems.productName,
      quantity: orderItems.quantity,
      lineTotalCents: orderItems.lineTotalCents,
      optionsJson: orderItems.optionsJson,
    })
    .from(orderItems)
    .where(eq(orderItems.orderId, orderId));

  const events = await db
    .select({
      id: orderEvents.id,
      type: orderEvents.type,
      createdAt: orderEvents.createdAt,
    })
    .from(orderEvents)
    .where(eq(orderEvents.orderId, orderId))
    .orderBy(desc(orderEvents.createdAt));

  return {
    id: row.id,
    publicNumber: row.publicNumber,
    status: row.status,
    paymentStatus: row.paymentStatus,
    paymentMode: row.paymentMode,
    totalCents: row.totalCents,
    subtotalCents: row.subtotalCents,
    createdAt: row.createdAt,
    pickupSlotStart: row.pickupSlotStart,
    refuseReason: row.refuseReason,
    items,
    events,
  };
}

export type CreateOrderInput = {
  userId: string;
  paymentMode: "on_site" | "online";
  pickupSlotStart?: Date | null;
  lines: {
    productId: string;
    name: string;
    unitPriceCents: number;
    quantity: number;
    options: { optionId: string; name: string; priceCents: number }[];
  }[];
};

export async function createOrder(input: CreateOrderInput): Promise<{ id: string; publicNumber: string }> {
  if (input.lines.length === 0) {
    throw new Error("EMPTY_CART");
  }

  const subtotalCents = input.lines.reduce((sum, line) => {
    const opts = line.options.reduce((s, o) => s + o.priceCents, 0);
    return sum + (line.unitPriceCents + opts) * line.quantity;
  }, 0);

  const paymentMode = input.paymentMode;
  const paymentStatus =
    paymentMode === "on_site" ? "to_pay_on_site" : "pending";
  const status =
    paymentMode === "on_site" ? "pending_acceptance" : "pending_payment";

  const id = crypto.randomUUID();
  const number = publicNumber();

  await db.insert(orders).values({
    id,
    userId: input.userId,
    publicNumber: number,
    fulfillmentType: "click_collect",
    paymentMode,
    paymentStatus,
    status,
    pickupSlotStart: input.pickupSlotStart ?? null,
    subtotalCents,
    totalCents: subtotalCents,
  });

  await db.insert(orderItems).values(
    input.lines.map((line) => {
      const opts = line.options.reduce((s, o) => s + o.priceCents, 0);
      const lineTotal = (line.unitPriceCents + opts) * line.quantity;
      return {
        orderId: id,
        productId: line.productId,
        productName: line.name,
        unitPriceCents: line.unitPriceCents,
        quantity: line.quantity,
        lineTotalCents: lineTotal,
        optionsJson: line.options,
      };
    }),
  );

  await db.insert(orderEvents).values({
    orderId: id,
    type: "order_created",
    actor: "customer",
    payload: { paymentMode, status },
  });

  return { id, publicNumber: number };
}
