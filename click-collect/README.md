# Kebaba — Click & Collect (PWA)

Application web click & collect pour **kebaba.fr** : catalogue, panier, compte obligatoire, paiement Stripe ou au retrait, sync caisse.

## Stack

- Next.js (App Router) + TypeScript + Tailwind
- PostgreSQL + Drizzle ORM
- Auth.js (à finaliser) · Stripe (à brancher)
- Docker + Traefik sur VPS OVH (`proxy-net`)

## Dev local

```bash
cp .env.example .env
# Démarrer Postgres (ex. docker compose up db -d) puis :
npm install
npm run db:push
npm run db:seed
npm run dev
```

Sans BDD, le menu charge le **seed** en mémoire (carte Kebaba).

## Compte client

- Inscription / connexion e-mail + mot de passe (`/compte`)
- Google OAuth : renseigner `AUTH_GOOGLE_ID` + `AUTH_GOOGLE_SECRET`
  1. [Google Cloud Console](https://console.cloud.google.com/) → APIs & Services → Identifiants
  2. Créer un client OAuth « Application Web »
  3. URI autorisées : `https://kebaba.fr`
  4. URI de redirection : `https://kebaba.fr/api/auth/callback/google`
  5. Redéployer : `docker compose up -d --build`

## Déploiement VPS

1. Réseau Docker `proxy-net` déjà en place (Traefik).
2. Copier `.env` avec secrets (jamais committer).
3. `docker compose up -d --build`
4. `docker compose exec app npm run db:push && docker compose exec app npm run db:seed`  
   (ou job one-shot — à peaufiner)

DNS `kebaba.fr` / `www` → `141.94.246.122` (+ AAAA VPS).

## Catalogue

Source de vérité : règle Cursor `carte-kebaba`. Ne pas inventer de produits hors règle.
