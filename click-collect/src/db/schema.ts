import {
  boolean,
  integer,
  jsonb,
  pgEnum,
  pgTable,
  primaryKey,
  text,
  timestamp,
  uniqueIndex,
} from "drizzle-orm/pg-core";
import type { AdapterAccountType } from "next-auth/adapters";

export const fulfillmentTypeEnum = pgEnum("fulfillment_type", [
  "click_collect",
  "delivery",
]);

export const paymentModeEnum = pgEnum("payment_mode", ["online", "on_site"]);

export const paymentStatusEnum = pgEnum("payment_status", [
  "pending",
  "paid",
  "failed",
  "refunded",
  "to_pay_on_site",
]);

export const orderStatusEnum = pgEnum("order_status", [
  "pending_payment",
  "pending_acceptance",
  "accepted",
  "refused",
  "preparing",
  "ready",
  "picked_up",
  "cancelled",
]);

/** Auth.js (Drizzle adapter) */
export const users = pgTable("users", {
  id: text("id")
    .primaryKey()
    .$defaultFn(() => crypto.randomUUID()),
  name: text("name"),
  email: text("email").notNull().unique(),
  emailVerified: timestamp("email_verified", { mode: "date" }),
  image: text("image"),
  phone: text("phone"),
  passwordHash: text("password_hash"),
  createdAt: timestamp("created_at", { mode: "date" }).defaultNow().notNull(),
});

export const accounts = pgTable(
  "accounts",
  {
    userId: text("user_id")
      .notNull()
      .references(() => users.id, { onDelete: "cascade" }),
    type: text("type").$type<AdapterAccountType>().notNull(),
    provider: text("provider").notNull(),
    providerAccountId: text("provider_account_id").notNull(),
    refresh_token: text("refresh_token"),
    access_token: text("access_token"),
    expires_at: integer("expires_at"),
    token_type: text("token_type"),
    scope: text("scope"),
    id_token: text("id_token"),
    session_state: text("session_state"),
  },
  (t) => [primaryKey({ columns: [t.provider, t.providerAccountId] })],
);

export const sessions = pgTable("sessions", {
  sessionToken: text("session_token").primaryKey(),
  userId: text("user_id")
    .notNull()
    .references(() => users.id, { onDelete: "cascade" }),
  expires: timestamp("expires", { mode: "date" }).notNull(),
});

export const verificationTokens = pgTable(
  "verification_tokens",
  {
    identifier: text("identifier").notNull(),
    token: text("token").notNull(),
    expires: timestamp("expires", { mode: "date" }).notNull(),
  },
  (t) => [primaryKey({ columns: [t.identifier, t.token] })],
);

export const categories = pgTable("categories", {
  id: text("id").primaryKey(),
  name: text("name").notNull(),
  sortOrder: integer("sort_order").notNull().default(0),
  active: boolean("active").notNull().default(true),
});

export const products = pgTable("products", {
  id: text("id").primaryKey(),
  categoryId: text("category_id")
    .notNull()
    .references(() => categories.id),
  name: text("name").notNull(),
  description: text("description"),
  priceCents: integer("price_cents").notNull(),
  /** TVA % × 100 — ex. 10 % → 1000 ; à valider comptable */
  tvaRateBps: integer("tva_rate_bps").notNull().default(1000),
  active: boolean("active").notNull().default(true),
  sortOrder: integer("sort_order").notNull().default(0),
  hasPain: boolean("has_pain").notNull().default(false),
  hasFrites: boolean("has_frites").notNull().default(false),
  hasSauces: boolean("has_sauces").notNull().default(false),
  hasCrudites: boolean("has_crudites").notNull().default(false),
  hasSupplements: boolean("has_supplements").notNull().default(false),
  imageUrl: text("image_url"),
});

export const options = pgTable(
  "options",
  {
    id: text("id").primaryKey(),
    groupId: text("group_id").notNull(),
    name: text("name").notNull(),
    priceCents: integer("price_cents").notNull().default(0),
    sortOrder: integer("sort_order").notNull().default(0),
    active: boolean("active").notNull().default(true),
  },
  (t) => [uniqueIndex("options_group_id_idx").on(t.groupId, t.id)],
);

export const addresses = pgTable("addresses", {
  id: text("id")
    .primaryKey()
    .$defaultFn(() => crypto.randomUUID()),
  userId: text("user_id")
    .notNull()
    .references(() => users.id, { onDelete: "cascade" }),
  label: text("label"),
  line1: text("line1").notNull(),
  line2: text("line2"),
  postalCode: text("postal_code").notNull(),
  city: text("city").notNull(),
  createdAt: timestamp("created_at", { mode: "date" }).defaultNow().notNull(),
});

export const orders = pgTable("orders", {
  id: text("id")
    .primaryKey()
    .$defaultFn(() => crypto.randomUUID()),
  userId: text("user_id")
    .notNull()
    .references(() => users.id),
  publicNumber: text("public_number").notNull(),
  fulfillmentType: fulfillmentTypeEnum("fulfillment_type")
    .notNull()
    .default("click_collect"),
  paymentMode: paymentModeEnum("payment_mode").notNull(),
  paymentStatus: paymentStatusEnum("payment_status").notNull(),
  status: orderStatusEnum("status").notNull(),
  pickupSlotStart: timestamp("pickup_slot_start", { mode: "date" }),
  pickupSlotEnd: timestamp("pickup_slot_end", { mode: "date" }),
  /** Reserved for delivery — unused in V1 */
  addressId: text("address_id").references(() => addresses.id),
  subtotalCents: integer("subtotal_cents").notNull(),
  totalCents: integer("total_cents").notNull(),
  refuseReason: text("refuse_reason"),
  caisseTicketId: text("caisse_ticket_id"),
  createdAt: timestamp("created_at", { mode: "date" }).defaultNow().notNull(),
  updatedAt: timestamp("updated_at", { mode: "date" }).defaultNow().notNull(),
});

export const orderItems = pgTable("order_items", {
  id: text("id")
    .primaryKey()
    .$defaultFn(() => crypto.randomUUID()),
  orderId: text("order_id")
    .notNull()
    .references(() => orders.id, { onDelete: "cascade" }),
  productId: text("product_id").notNull(),
  productName: text("product_name").notNull(),
  unitPriceCents: integer("unit_price_cents").notNull(),
  quantity: integer("quantity").notNull().default(1),
  lineTotalCents: integer("line_total_cents").notNull(),
  optionsJson: jsonb("options_json").$type<
    { optionId: string; name: string; priceCents: number }[]
  >(),
});

export const payments = pgTable("payments", {
  id: text("id")
    .primaryKey()
    .$defaultFn(() => crypto.randomUUID()),
  orderId: text("order_id")
    .notNull()
    .references(() => orders.id, { onDelete: "cascade" }),
  provider: text("provider").notNull().default("stripe"),
  stripePaymentIntentId: text("stripe_payment_intent_id"),
  amountCents: integer("amount_cents").notNull(),
  status: text("status").notNull(),
  createdAt: timestamp("created_at", { mode: "date" }).defaultNow().notNull(),
});

export const orderEvents = pgTable("order_events", {
  id: text("id")
    .primaryKey()
    .$defaultFn(() => crypto.randomUUID()),
  orderId: text("order_id")
    .notNull()
    .references(() => orders.id, { onDelete: "cascade" }),
  type: text("type").notNull(),
  payload: jsonb("payload"),
  actor: text("actor"),
  createdAt: timestamp("created_at", { mode: "date" }).defaultNow().notNull(),
});
