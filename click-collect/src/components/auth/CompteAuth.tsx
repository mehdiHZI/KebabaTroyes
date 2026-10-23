"use client";

import { useState } from "react";
import { signIn, signOut } from "next-auth/react";
import Link from "next/link";

type SessionUser = {
  id: string;
  name?: string | null;
  email?: string | null;
  image?: string | null;
};

type Props = {
  user: SessionUser | null;
  googleEnabled: boolean;
  errorCode?: string;
};

type Mode = "login" | "register";

const inputClass =
  "w-full rounded-xl border border-[var(--kebaba-bordeaux)]/30 bg-white px-3 py-2 font-normal";

export function CompteAuth({ user, googleEnabled, errorCode }: Props) {
  const [mode, setMode] = useState<Mode>("login");
  const [pending, setPending] = useState(false);
  const [message, setMessage] = useState<string | null>(
    errorCode ? mapAuthError(errorCode) : null,
  );

  if (user) {
    return (
      <div className="kebaba-card space-y-4 p-6">
        <p className="text-sm text-[var(--kebaba-ink-muted)]">Connecté en tant que</p>
        <div className="flex items-center gap-3">
          {user.image ? (
            // eslint-disable-next-line @next/next/no-img-element
            <img
              src={user.image}
              alt=""
              className="h-12 w-12 rounded-full border border-[var(--kebaba-bordeaux)]/20"
            />
          ) : (
            <div className="flex h-12 w-12 items-center justify-center rounded-full bg-[var(--kebaba-bordeaux)] font-display text-lg font-bold text-white">
              {(user.name ?? user.email ?? "?").charAt(0).toUpperCase()}
            </div>
          )}
          <div>
            <p className="font-bold text-[var(--kebaba-bordeaux)]">
              {user.name ?? "Client Kebaba"}
            </p>
            <p className="text-sm text-[var(--kebaba-ink-muted)]">{user.email}</p>
          </div>
        </div>
        <div className="flex flex-col gap-2">
          <Link href="/commandes" className="kebaba-btn w-full text-center">
            Mes commandes &amp; suivi
          </Link>
          <div className="flex flex-col gap-2 sm:flex-row">
            <Link href="/menu" className="kebaba-btn-outline flex-1 text-center">
              Commander
            </Link>
            <button
              type="button"
              className="kebaba-btn-outline flex-1"
              onClick={() => signOut({ callbackUrl: "/compte" })}
            >
              Se déconnecter
            </button>
          </div>
        </div>
      </div>
    );
  }

  async function onLogin(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setPending(true);
    setMessage(null);
    const fd = new FormData(e.currentTarget);
    const email = String(fd.get("email") ?? "");
    const password = String(fd.get("password") ?? "");
    const res = await signIn("credentials", {
      email,
      password,
      redirect: false,
      callbackUrl: "/compte",
    });
    setPending(false);
    if (res?.error) {
      setMessage("E-mail ou mot de passe incorrect.");
      return;
    }
    window.location.href = res?.url || "/compte";
  }

  async function onRegister(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setPending(true);
    setMessage(null);
    const fd = new FormData(e.currentTarget);
    const payload = {
      name: String(fd.get("name") ?? ""),
      email: String(fd.get("email") ?? ""),
      phone: String(fd.get("phone") ?? ""),
      password: String(fd.get("password") ?? ""),
    };
    const reg = await fetch("/api/auth/register", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    });
    const data = (await reg.json()) as { error?: string };
    if (!reg.ok) {
      setPending(false);
      setMessage(data.error ?? "Inscription impossible.");
      return;
    }
    const login = await signIn("credentials", {
      email: payload.email,
      password: payload.password,
      redirect: false,
      callbackUrl: "/compte",
    });
    setPending(false);
    if (login?.error) {
      setMessage("Compte créé — connectez-vous.");
      setMode("login");
      return;
    }
    window.location.href = login?.url || "/compte";
  }

  return (
    <div className="space-y-4">
      <div className="kebaba-card space-y-4 p-6">
        <div className="flex rounded-xl border border-[var(--kebaba-bordeaux)]/25 p-1">
          <button
            type="button"
            className={`flex-1 rounded-lg py-2 text-sm font-bold ${
              mode === "login"
                ? "bg-[var(--kebaba-bordeaux)] text-white"
                : "text-[var(--kebaba-bordeaux)]"
            }`}
            onClick={() => {
              setMode("login");
              setMessage(null);
            }}
          >
            Connexion
          </button>
          <button
            type="button"
            className={`flex-1 rounded-lg py-2 text-sm font-bold ${
              mode === "register"
                ? "bg-[var(--kebaba-bordeaux)] text-white"
                : "text-[var(--kebaba-bordeaux)]"
            }`}
            onClick={() => {
              setMode("register");
              setMessage(null);
            }}
          >
            Créer un compte
          </button>
        </div>

        {mode === "login" ? (
          <form className="space-y-3" onSubmit={onLogin}>
            <label className="block space-y-1 text-sm font-semibold">
              E-mail
              <input
                type="email"
                name="email"
                required
                autoComplete="email"
                className={inputClass}
                placeholder="vous@exemple.fr"
              />
            </label>
            <label className="block space-y-1 text-sm font-semibold">
              Mot de passe
              <input
                type="password"
                name="password"
                required
                autoComplete="current-password"
                className={inputClass}
                placeholder="••••••••"
              />
            </label>
            <button type="submit" className="kebaba-btn w-full" disabled={pending}>
              {pending ? "Connexion…" : "Se connecter"}
            </button>
          </form>
        ) : (
          <form className="space-y-3" onSubmit={onRegister}>
            <label className="block space-y-1 text-sm font-semibold">
              Nom
              <input
                type="text"
                name="name"
                required
                minLength={2}
                autoComplete="name"
                className={inputClass}
                placeholder="Votre prénom / nom"
              />
            </label>
            <label className="block space-y-1 text-sm font-semibold">
              E-mail
              <input
                type="email"
                name="email"
                required
                autoComplete="email"
                className={inputClass}
                placeholder="vous@exemple.fr"
              />
            </label>
            <label className="block space-y-1 text-sm font-semibold">
              Téléphone{" "}
              <span className="font-normal text-[var(--kebaba-ink-muted)]">(optionnel)</span>
              <input
                type="tel"
                name="phone"
                autoComplete="tel"
                className={inputClass}
                placeholder="06 …"
              />
            </label>
            <label className="block space-y-1 text-sm font-semibold">
              Mot de passe
              <input
                type="password"
                name="password"
                required
                minLength={8}
                autoComplete="new-password"
                className={inputClass}
                placeholder="8 caractères minimum"
              />
            </label>
            <button type="submit" className="kebaba-btn w-full" disabled={pending}>
              {pending ? "Création…" : "Créer mon compte"}
            </button>
          </form>
        )}

        {message ? (
          <p className="text-sm font-semibold text-red-700" role="alert">
            {message}
          </p>
        ) : null}

        {googleEnabled ? (
          <>
            <div className="relative py-1 text-center text-xs font-semibold uppercase tracking-wide text-[var(--kebaba-ink-muted)]">
              <span className="relative z-10 bg-[var(--kebaba-card)] px-2">ou</span>
              <span className="absolute inset-x-0 top-1/2 h-px -translate-y-1/2 bg-[var(--kebaba-bordeaux)]/20" />
            </div>
            <button
              type="button"
              className="kebaba-btn-outline flex w-full items-center justify-center gap-2"
              disabled={pending}
              onClick={() => signIn("google", { callbackUrl: "/compte" })}
            >
              <GoogleIcon />
              Continuer avec Google
            </button>
          </>
        ) : null}
      </div>
    </div>
  );
}

function mapAuthError(code: string): string {
  switch (code) {
    case "OAuthAccountNotLinked":
      return "Cet e-mail est déjà utilisé avec un autre mode de connexion.";
    case "AccessDenied":
      return "Accès refusé.";
    case "Configuration":
      return "Configuration auth incomplète (Google / secret).";
    default:
      return "Erreur de connexion. Réessayez.";
  }
}

function GoogleIcon() {
  return (
    <svg width="18" height="18" viewBox="0 0 48 48" aria-hidden>
      <path
        fill="#FFC107"
        d="M43.6 20.5H42V20H24v8h11.3C33.7 32.7 29.3 36 24 36c-6.6 0-12-5.4-12-12s5.4-12 12-12c3 0 5.8 1.1 7.9 3l5.7-5.7C34.2 6.1 29.4 4 24 4 12.9 4 4 12.9 4 24s8.9 20 20 20 20-8.9 20-20c0-1.3-.1-2.5-.4-3.5z"
      />
      <path
        fill="#FF3D00"
        d="M6.3 14.7l6.6 4.8C14.7 16.1 19 13 24 13c3 0 5.8 1.1 7.9 3l5.7-5.7C34.2 6.1 29.4 4 24 4 16.3 4 9.6 8.3 6.3 14.7z"
      />
      <path
        fill="#4CAF50"
        d="M24 44c5.2 0 10-2 13.6-5.2l-6.3-5.2C29.3 35.3 26.8 36 24 36c-5.3 0-9.7-3.3-11.3-8l-6.5 5C9.5 39.6 16.2 44 24 44z"
      />
      <path
        fill="#1976D2"
        d="M43.6 20.5H42V20H24v8h11.3c-.8 2.2-2.2 4.1-4 5.5l.1.1 6.3 5.2C39.5 37.1 44 31.5 44 24c0-1.3-.1-2.5-.4-3.5z"
      />
    </svg>
  );
}
