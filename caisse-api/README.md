# API caisse Kebaba (VPS)

Réception des tickets encaissés depuis la tablette Android (idempotent `ticketId`).

## Endpoints

| Méthode | Route | Auth | Rôle |
|---------|-------|------|------|
| GET | `/health` | non | healthcheck |
| POST | `/v1/tickets` | Bearer | push ticket caisse (idempotent) |
| GET | `/v1/tickets` | Bearer | liste tickets |
| GET | `/v1/tickets/:id` | Bearer | détail ticket |
| POST | `/v1/orders` | Bearer | commande multi-canal (Uber/Deliveroo/…) |
| GET | `/v1/orders?status=open` | Bearer | file ouverte pour la caisse |
| POST | `/v1/orders/:id/ack` | Bearer | acceptée / en préparation |
| POST | `/v1/orders/:id/cancel` | Bearer | annulée |
| POST | `/v1/orders/demo` | Bearer | commande factice de test |

Voir [INTEGRATION_AGGREGATEUR.md](./INTEGRATION_AGGREGATEUR.md).

## Déploiement VPS

1. DNS `caisse-api.kebaba.fr` → IP publique Traefik
2. Copier `.env.example` → `.env`, renseigner `CAISSE_API_TOKEN`
3. `docker compose up -d --build` (réseau `proxy-net`)

## Android

Configurer dans l’app (ou `local.properties`) :

- base URL : `https://caisse-api.kebaba.fr`
- token = même `CAISSE_API_TOKEN`
