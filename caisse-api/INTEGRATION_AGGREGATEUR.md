# Commandes Uber Eats / Deliveroo → Caisse Kebaba

## Principe

```text
Uber Eats / Deliveroo
        ↓
  Agrégateur (ex. Deliverect)  ← recommandation V1
        ↓  POST /v1/orders  (Bearer token)
  caisse-api (VPS)
        ↓  GET /v1/orders?status=open  (toutes les 15 s)
  App caisse Android → écran « À encaisser livraison »
```

L’app **ne parle pas** directement à Uber/Deliveroo.  
L’agrégateur (ou un connecteur custom) pousse un JSON normalisé vers notre API.

## Auth

Header : `Authorization: Bearer <CAISSE_API_TOKEN>`

## Créer une commande

`POST https://caisse-api.kebaba.fr/v1/orders`

```json
{
  "orderId": "uber-20261023-abc123",
  "channel": "uber_eats",
  "paidOnline": true,
  "totalCentimes": 1850,
  "customerName": "Jean D.",
  "phone": "0612345678",
  "address": "5 rue Exemple, 10000 Troyes",
  "notes": "Sans oignon",
  "lines": [
    {
      "productId": "kebaba-klassik",
      "name": "Kebaba Klassik",
      "quantity": 1,
      "unitPriceCentimes": 1000,
      "totalCentimes": 1000
    },
    {
      "name": "Coca Cola 33 cl",
      "productId": "coca-cola-33",
      "quantity": 1,
      "unitPriceCentimes": 200,
      "totalCentimes": 200
    }
  ]
}
```

`channel` : `uber_eats` | `deliveroo` | `click_collect` | `phone_ai` | `borne` | `manual`  
Idempotent sur `orderId` (renvoi = `duplicate`).

## Caisse

- `GET /v1/orders?status=open` — file ouverte  
- `POST /v1/orders/:id/ack` — acceptée / en prep (`{"status":"preparing"}`)  
- `POST /v1/orders/:id/cancel` — annulée  
- `POST /v1/orders/:id/complete` — terminée  

## Test démo (sans agrégateur)

```bash
TOKEN=... # même token que .env
curl -sS -X POST https://caisse-api.kebaba.fr/v1/orders/demo \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"channel":"uber_eats"}'
```

Puis ouvrir la caisse → **À encaisser livraison** (attente ≤ 15 s).

## Suite métier

1. Choisir un agrégateur et brancher son webhook → `POST /v1/orders`
2. Aligner les `productId` sur la carte Kebaba
3. Commandes `paidOnline: true` : encaissement caisse = validation / ticket cuisine (pas de double CB client)
