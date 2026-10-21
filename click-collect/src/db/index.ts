import { drizzle } from "drizzle-orm/postgres-js";
import postgres from "postgres";
import * as schema from "./schema";

const connectionString = process.env.DATABASE_URL;

if (!connectionString) {
  console.warn("DATABASE_URL is not set — DB calls will fail until configured.");
}

const client = postgres(connectionString ?? "postgres://localhost:5432/kebaba_click_collect", {
  max: 10,
  idle_timeout: 20,
});

export const db = drizzle(client, { schema });
