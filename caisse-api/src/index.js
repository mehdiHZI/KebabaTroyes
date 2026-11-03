import http from "node:http";
import { mkdir, readFile, writeFile, rename } from "node:fs/promises";
import path from "node:path";
import { fileURLToPath } from "node:url";
import { timingSafeEqual, randomUUID } from "node:crypto";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const PORT = Number(process.env.PORT || 8080);
const DATA_DIR = process.env.DATA_DIR || path.join(process.cwd(), "data");
const TICKETS_FILE = path.join(DATA_DIR, "tickets.json");
const ORDERS_FILE = path.join(DATA_DIR, "orders.json");
const CATALOG_FILE = path.join(DATA_DIR, "catalog.json");
const CATALOG_SEED_FILE = path.join(__dirname, "catalog-seed.json");
const API_TOKEN = process.env.CAISSE_API_TOKEN || "";

const PRODUCT_CATEGORIES = new Set([
  "KLASSIK",
  "DWICH",
  "KREATIONS",
  "ASSIETTES",
  "BOWLS",
  "TAKOS",
  "MENUS",
  "PTIT_PLUS",
  "BARQUETTES",
  "FRITES",
  "DESSERTS",
  "BOISSONS",
]);

/** Canaux supportés (agrégateur → VPS → caisse). */
const CHANNELS = new Set([
  "uber_eats",
  "deliveroo",
  "click_collect",
  "phone_ai",
  "borne",
  "manual",
]);

const OPEN_STATUSES = new Set(["open", "accepted", "preparing"]);

if (!API_TOKEN) {
  console.error("CAISSE_API_TOKEN manquant — refuse de démarrer.");
  process.exit(1);
}

async function ensureFile(file, fallback = "{}") {
  await mkdir(DATA_DIR, { recursive: true });
  try {
    await readFile(file, "utf8");
  } catch {
    await writeFile(file, fallback, "utf8");
  }
}

async function loadCatalogSeed() {
  const raw = await readFile(CATALOG_SEED_FILE, "utf8");
  return JSON.parse(raw);
}

async function ensureCatalogStore() {
  await mkdir(DATA_DIR, { recursive: true });
  try {
    const raw = await readFile(CATALOG_FILE, "utf8");
    const parsed = JSON.parse(raw || "{}");
    const count = parsed?.products ? Object.keys(parsed.products).length : 0;
    if (count > 0) return;
  } catch {
    // fichier absent
  }
  const seed = await loadCatalogSeed();
  await saveJson(CATALOG_FILE, seed);
}

async function ensureStore() {
  await ensureFile(TICKETS_FILE);
  await ensureFile(ORDERS_FILE);
  await ensureCatalogStore();
}

function slugifyProductId(name) {
  return String(name || "")
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/^-+|-+$/g, "")
    .slice(0, 64);
}

function validateProduct(body, { requireId = false } = {}) {
  if (!body || typeof body !== "object") return "JSON objet requis";
  if (requireId && (!body.id || typeof body.id !== "string")) return "id requis";
  if (!body.name || typeof body.name !== "string" || !body.name.trim()) {
    return "name requis";
  }
  if (!body.category || !PRODUCT_CATEGORIES.has(body.category)) {
    return `category invalide (attendu: ${[...PRODUCT_CATEGORIES].join(", ")})`;
  }
  if (typeof body.priceCentimes !== "number" || body.priceCentimes < 0) {
    return "priceCentimes invalide";
  }
  if (
    body.menuPriceCentimes != null &&
    (typeof body.menuPriceCentimes !== "number" || body.menuPriceCentimes < 0)
  ) {
    return "menuPriceCentimes invalide";
  }
  return null;
}

function normalizeProduct(body, existingId) {
  const id =
    (typeof body.id === "string" && body.id.trim()) ||
    existingId ||
    slugifyProductId(body.name) ||
    `produit-${randomUUID().slice(0, 8)}`;
  return {
    id,
    name: String(body.name).trim(),
    category: body.category,
    priceCentimes: Math.round(body.priceCentimes),
    menuPriceCentimes:
      body.menuPriceCentimes == null ? null : Math.round(body.menuPriceCentimes),
    hasPain: Boolean(body.hasPain),
    hasFrites: Boolean(body.hasFrites),
    hasCrudites: Boolean(body.hasCrudites),
    hasSupplements: Boolean(body.hasSupplements),
    hasSauces: Boolean(body.hasSauces),
    hasTakosMeats: Boolean(body.hasTakosMeats),
    supportsDrinkPlus: Boolean(body.supportsDrinkPlus),
    takosMeatCount:
      typeof body.takosMeatCount === "number"
        ? Math.max(0, Math.round(body.takosMeatCount))
        : 0,
    enabled: body.enabled !== false,
    updatedAtEpochMillis: Date.now(),
  };
}

async function loadJson(file) {
  const raw = await readFile(file, "utf8");
  return JSON.parse(raw || "{}");
}

async function saveJson(file, data) {
  const tmp = `${file}.${process.pid}.tmp`;
  await writeFile(tmp, JSON.stringify(data, null, 2), "utf8");
  await rename(tmp, file);
}

function safeEqual(a, b) {
  const ba = Buffer.from(a);
  const bb = Buffer.from(b);
  if (ba.length !== bb.length) return false;
  return timingSafeEqual(ba, bb);
}

function authorize(req) {
  const header = req.headers.authorization || "";
  const match = /^Bearer\s+(.+)$/i.exec(header);
  if (!match) return false;
  return safeEqual(match[1].trim(), API_TOKEN);
}

function readJson(req) {
  return new Promise((resolve, reject) => {
    const chunks = [];
    req.on("data", (c) => chunks.push(c));
    req.on("end", () => {
      try {
        const raw = Buffer.concat(chunks).toString("utf8");
        resolve(raw ? JSON.parse(raw) : {});
      } catch (e) {
        reject(e);
      }
    });
    req.on("error", reject);
  });
}

function send(res, status, body) {
  const payload = JSON.stringify(body);
  res.writeHead(status, {
    "Content-Type": "application/json; charset=utf-8",
    "Content-Length": Buffer.byteLength(payload),
  });
  res.end(payload);
}

function validateTicket(body) {
  if (!body || typeof body !== "object") return "JSON objet requis";
  if (!body.ticketId || typeof body.ticketId !== "string") return "ticketId requis";
  if (typeof body.ticketNumber !== "number") return "ticketNumber requis";
  if (typeof body.totalCentimes !== "number") return "totalCentimes requis";
  if (!body.orderType || typeof body.orderType !== "string") return "orderType requis";
  if (!Array.isArray(body.lines) || body.lines.length === 0) return "lines requises";
  if (!Array.isArray(body.payments)) return "payments requis";
  return null;
}

function channelLabel(channel) {
  switch (channel) {
    case "uber_eats":
      return "Uber Eats";
    case "deliveroo":
      return "Deliveroo";
    case "click_collect":
      return "Click & collect";
    case "phone_ai":
      return "Téléphone IA";
    case "borne":
      return "Borne";
    default:
      return channel || "Externe";
  }
}

function validateOrder(body) {
  if (!body || typeof body !== "object") return "JSON objet requis";
  if (!body.orderId || typeof body.orderId !== "string") return "orderId requis";
  if (!body.channel || !CHANNELS.has(body.channel)) {
    return `channel invalide (attendu: ${[...CHANNELS].join(", ")})`;
  }
  if (typeof body.totalCentimes !== "number") return "totalCentimes requis";
  if (!Array.isArray(body.lines) || body.lines.length === 0) return "lines requises";
  for (const line of body.lines) {
    if (!line || typeof line.name !== "string" || !line.name.trim()) {
      return "chaque ligne doit avoir name";
    }
    if (typeof line.quantity !== "number" || line.quantity < 1) {
      return "quantity invalide";
    }
  }
  return null;
}

function normalizeOrder(body) {
  const paidOnline = Boolean(body.paidOnline ?? true);
  const label = channelLabel(body.channel);
  return {
    orderId: body.orderId,
    externalRef: body.externalRef || null,
    channel: body.channel,
    channelLabel: label,
    status: "open",
    paidOnline,
    totalCentimes: body.totalCentimes,
    currency: body.currency || "EUR",
    customerName: body.customerName || label,
    phone: body.phone || "",
    address: body.address || "",
    notes: body.notes || "",
    lines: body.lines.map((l, i) => ({
      lineId: l.lineId || `l-${i + 1}`,
      productId: l.productId || null,
      name: l.name,
      quantity: l.quantity,
      unitPriceCentimes:
        typeof l.unitPriceCentimes === "number"
          ? l.unitPriceCentimes
          : Math.round((l.totalCentimes || 0) / Math.max(1, l.quantity)),
      totalCentimes:
        typeof l.totalCentimes === "number"
          ? l.totalCentimes
          : (l.unitPriceCentimes || 0) * l.quantity,
      options: Array.isArray(l.options) ? l.options : [],
    })),
    createdAt: body.createdAt || new Date().toISOString(),
    updatedAt: new Date().toISOString(),
    receivedAt: new Date().toISOString(),
  };
}

const server = http.createServer(async (req, res) => {
  const url = new URL(req.url || "/", `http://${req.headers.host || "localhost"}`);

  if (req.method === "GET" && url.pathname === "/health") {
    return send(res, 200, { ok: true, service: "kebaba-caisse-api" });
  }

  if (!authorize(req)) {
    return send(res, 401, { error: "Non autorisé" });
  }

  try {
    // —— Tickets caisse (existant) ——
    if (req.method === "POST" && url.pathname === "/v1/tickets") {
      const body = await readJson(req);
      const err = validateTicket(body);
      if (err) return send(res, 400, { error: err });

      const tickets = await loadJson(TICKETS_FILE);
      const existing = tickets[body.ticketId];
      if (existing) {
        return send(res, 200, { status: "duplicate", ticket: existing });
      }

      const stored = {
        ...body,
        receivedAt: new Date().toISOString(),
        source: "caisse",
      };
      tickets[body.ticketId] = stored;
      await saveJson(TICKETS_FILE, tickets);
      return send(res, 201, { status: "created", ticket: stored });
    }

    if (req.method === "GET" && url.pathname.startsWith("/v1/tickets/")) {
      const id = decodeURIComponent(url.pathname.slice("/v1/tickets/".length));
      if (!id || id.includes("/")) return send(res, 404, { error: "Route inconnue" });
      const tickets = await loadJson(TICKETS_FILE);
      const ticket = tickets[id];
      if (!ticket) return send(res, 404, { error: "Introuvable" });
      return send(res, 200, { ticket });
    }

    if (req.method === "GET" && url.pathname === "/v1/tickets") {
      const tickets = await loadJson(TICKETS_FILE);
      const list = Object.values(tickets).sort((a, b) =>
        String(b.receivedAt).localeCompare(String(a.receivedAt)),
      );
      return send(res, 200, { tickets: list, count: list.length });
    }

    // —— Commandes multi-canal (Uber / Deliveroo / …) ——
    if (req.method === "POST" && url.pathname === "/v1/orders") {
      const body = await readJson(req);
      const err = validateOrder(body);
      if (err) return send(res, 400, { error: err });

      const orders = await loadJson(ORDERS_FILE);
      const existing = orders[body.orderId];
      if (existing) {
        return send(res, 200, { status: "duplicate", order: existing });
      }

      const stored = normalizeOrder(body);
      orders[stored.orderId] = stored;
      await saveJson(ORDERS_FILE, orders);
      return send(res, 201, { status: "created", order: stored });
    }

    if (req.method === "GET" && url.pathname === "/v1/orders") {
      const statusFilter = url.searchParams.get("status"); // open | all | completed…
      const channelFilter = url.searchParams.get("channel");
      const orders = await loadJson(ORDERS_FILE);
      let list = Object.values(orders);
      if (statusFilter === "open" || !statusFilter) {
        list = list.filter((o) => OPEN_STATUSES.has(o.status));
      } else if (statusFilter !== "all") {
        list = list.filter((o) => o.status === statusFilter);
      }
      if (channelFilter) {
        list = list.filter((o) => o.channel === channelFilter);
      }
      list.sort((a, b) => String(b.receivedAt).localeCompare(String(a.receivedAt)));
      return send(res, 200, { orders: list, count: list.length });
    }

    if (
      req.method === "GET" &&
      url.pathname.startsWith("/v1/orders/") &&
      !url.pathname.endsWith("/demo")
    ) {
      const rest = url.pathname.slice("/v1/orders/".length);
      const id = decodeURIComponent(rest.split("/")[0] || "");
      if (!id || rest.includes("/")) {
        return send(res, 404, { error: "Route inconnue" });
      }
      const orders = await loadJson(ORDERS_FILE);
      const order = orders[id];
      if (!order) return send(res, 404, { error: "Introuvable" });
      return send(res, 200, { order });
    }

    if (req.method === "POST" && url.pathname.match(/^\/v1\/orders\/[^/]+\/(ack|cancel|complete)$/)) {
      const parts = url.pathname.split("/");
      const id = decodeURIComponent(parts[3]);
      const action = parts[4];
      const body = await readJson(req).catch(() => ({}));
      const orders = await loadJson(ORDERS_FILE);
      const order = orders[id];
      if (!order) return send(res, 404, { error: "Introuvable" });

      const nextStatus =
        action === "cancel"
          ? "cancelled"
          : action === "complete"
            ? "completed"
            : body.status && ["accepted", "preparing", "ready"].includes(body.status)
              ? body.status
              : "accepted";

      order.status = nextStatus;
      order.updatedAt = new Date().toISOString();
      order.ackedAt = order.ackedAt || new Date().toISOString();
      if (body.registerId) order.registerId = body.registerId;
      orders[id] = order;
      await saveJson(ORDERS_FILE, orders);
      return send(res, 200, { status: "updated", order });
    }

    // —— Catalogue produits ——
    if (req.method === "GET" && url.pathname === "/v1/catalog") {
      const catalog = await loadJson(CATALOG_FILE);
      const products = Object.values(catalog.products || {}).sort((a, b) => {
        const byCat = String(a.category).localeCompare(String(b.category));
        if (byCat !== 0) return byCat;
        return String(a.name).localeCompare(String(b.name), "fr");
      });
      return send(res, 200, {
        version: catalog.version || 1,
        products,
        count: products.length,
      });
    }

    if (req.method === "POST" && url.pathname === "/v1/catalog/products") {
      const body = await readJson(req);
      const err = validateProduct(body);
      if (err) return send(res, 400, { error: err });
      const catalog = await loadJson(CATALOG_FILE);
      if (!catalog.products) catalog.products = {};
      let product = normalizeProduct(body);
      if (catalog.products[product.id]) {
        product = normalizeProduct(body, `${product.id}-${randomUUID().slice(0, 4)}`);
      }
      catalog.products[product.id] = product;
      catalog.version = (catalog.version || 1) + 1;
      await saveJson(CATALOG_FILE, catalog);
      return send(res, 201, { status: "created", product, version: catalog.version });
    }

    if (
      req.method === "PUT" &&
      url.pathname.startsWith("/v1/catalog/products/")
    ) {
      const id = decodeURIComponent(
        url.pathname.slice("/v1/catalog/products/".length),
      );
      if (!id || id.includes("/")) return send(res, 404, { error: "Route inconnue" });
      const body = await readJson(req);
      const err = validateProduct({ ...body, id }, { requireId: false });
      if (err) return send(res, 400, { error: err });
      const catalog = await loadJson(CATALOG_FILE);
      if (!catalog.products) catalog.products = {};
      const product = normalizeProduct({ ...body, id }, id);
      catalog.products[id] = product;
      catalog.version = (catalog.version || 1) + 1;
      await saveJson(CATALOG_FILE, catalog);
      return send(res, 200, { status: "upserted", product, version: catalog.version });
    }

    // Seed démo (tests) — génère une commande Uber factice
    if (req.method === "POST" && url.pathname === "/v1/orders/demo") {
      const body = await readJson(req).catch(() => ({}));
      const channel = CHANNELS.has(body.channel) ? body.channel : "uber_eats";
      const orderId = body.orderId || `demo-${channel}-${randomUUID().slice(0, 8)}`;
      const demo = normalizeOrder({
        orderId,
        channel,
        paidOnline: true,
        totalCentimes: 1850,
        customerName: body.customerName || "Client démo",
        phone: "0600000000",
        address: body.address || "12 rue Exemple, Troyes",
        lines: [
          {
            name: "Kebaba Klassik",
            productId: "kebaba-klassik",
            quantity: 1,
            unitPriceCentimes: 1000,
            totalCentimes: 1000,
          },
          {
            name: "Coca Cola 33 cl",
            productId: "coca-cola-33",
            quantity: 1,
            unitPriceCentimes: 200,
            totalCentimes: 200,
          },
          {
            name: "Frites petite",
            productId: "frites-petite",
            quantity: 1,
            unitPriceCentimes: 350,
            totalCentimes: 350,
          },
          {
            name: "Frais plateforme",
            quantity: 1,
            unitPriceCentimes: 300,
            totalCentimes: 300,
          },
        ],
      });
      const orders = await loadJson(ORDERS_FILE);
      if (orders[orderId]) {
        return send(res, 200, { status: "duplicate", order: orders[orderId] });
      }
      orders[orderId] = demo;
      await saveJson(ORDERS_FILE, orders);
      return send(res, 201, { status: "created", order: demo });
    }

    return send(res, 404, { error: "Route inconnue" });
  } catch (e) {
    console.error(e);
    return send(res, 500, { error: "Erreur serveur" });
  }
});

await ensureStore();
server.listen(PORT, "0.0.0.0", () => {
  console.log(`kebaba-caisse-api écoute sur :${PORT}`);
});
