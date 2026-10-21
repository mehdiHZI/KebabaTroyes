import { NextResponse } from "next/server";
import { eq } from "drizzle-orm";
import bcrypt from "bcryptjs";
import { z } from "zod";
import { db } from "@/db";
import { users } from "@/db/schema";

const registerSchema = z.object({
  name: z.string().trim().min(2, "Nom trop court").max(80),
  email: z.string().trim().email("E-mail invalide").max(120),
  phone: z
    .string()
    .trim()
    .max(30)
    .optional()
    .or(z.literal("")),
  password: z
    .string()
    .min(8, "Mot de passe : 8 caractères minimum")
    .max(100),
});

export async function POST(request: Request) {
  let body: unknown;
  try {
    body = await request.json();
  } catch {
    return NextResponse.json({ error: "Requête invalide." }, { status: 400 });
  }

  const parsed = registerSchema.safeParse(body);
  if (!parsed.success) {
    const msg = parsed.error.issues[0]?.message ?? "Données invalides.";
    return NextResponse.json({ error: msg }, { status: 400 });
  }

  const email = parsed.data.email.toLowerCase();
  const phone = parsed.data.phone?.trim() || null;

  const [existing] = await db
    .select({ id: users.id })
    .from(users)
    .where(eq(users.email, email))
    .limit(1);

  if (existing) {
    return NextResponse.json(
      { error: "Un compte existe déjà avec cet e-mail." },
      { status: 409 },
    );
  }

  const passwordHash = await bcrypt.hash(parsed.data.password, 12);

  const [created] = await db
    .insert(users)
    .values({
      name: parsed.data.name,
      email,
      phone,
      passwordHash,
    })
    .returning({ id: users.id, email: users.email, name: users.name });

  return NextResponse.json(
    { ok: true, user: { id: created.id, email: created.email, name: created.name } },
    { status: 201 },
  );
}
