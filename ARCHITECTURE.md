# Architecture

## Bounded Context Map

```
┌─────────────────────────────────────────────────────────────────┐
│                        trading-core (monolith)                  │
│                                                                 │
│  ┌──────────┐   ┌────────────┐   ┌──────────┐   ┌──────────┐  │
│  │   auth   │   │ marketdata │   │ trading  │   │alerting  │  │
│  │          │   │            │   │          │   │          │  │
│  │ register │   │ CoinGecko  │   │ Alpaca   │   │ rules    │  │
│  │ login    │   │ Binance WS │   │ orders   │   │ email    │  │
│  │ JWT      │   │ prices     │   │ algo     │   │ notify   │  │
│  └──────────┘   └────────────┘   └──────────┘   └──────────┘  │
│                                                                 │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                       portfolio                          │  │
│  │              positions · P&L · history                   │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
          │ Kafka events │                │ Kafka events │
```

Bounded contexts communicate via **Kafka domain events** only — no direct package imports across context boundaries.

---

## Hexagonal Architecture (per bounded context)

```
                    ┌─────────────────────────────┐
  REST / WS ───────▶│           api/              │ (inbound adapter)
                    │   Controller · DTOs          │
                    └──────────────┬──────────────┘
                                   │
                    ┌──────────────▼──────────────┐
                    │        application/          │
                    │  command/   │   query/       │
                    │  Handlers   │   Handlers     │
                    └──────┬──────┴──────┬─────────┘
                           │             │
                    ┌──────▼─────────────▼─────────┐
                    │          domain/              │
                    │  Aggregates · Value Objects   │ ◀── no framework deps
                    │  Domain exceptions            │
                    └──────────────────────────────┘
                    ┌──────────────────────────────┐
                    │       infrastructure/         │
                    │  ports/     │  adapters/      │
                    │  (interfaces│  JOOQ · Kafka   │ (outbound adapters)
                    │  owned by   │  HTTP clients   │
                    │  app layer) │  Mongo          │
                    └──────────────────────────────┘
```

**The rule**: dependencies point inward only. `domain/` has zero framework imports. `application/` depends on `domain/` and `ports/` only. `adapters/` implement `ports/`.

---

## CQRS Flow

```
Write side (Command)
─────────────────────────────────────────────────────────────
HTTP POST → Controller → XxxCommand (record)
         → XxxCommandHandler (@Service)
               → domain logic on aggregate
               → port.save() → JOOQ → PostgreSQL
               → eventPublisher.publish() → Kafka topic
                                               │
                                               ▼
Read side (Query) — updated via Kafka projection consumer
─────────────────────────────────────────────────────────────
HTTP GET  → Controller → GetXxxQuery (record)
         → GetXxxQueryHandler (@Service)
               → repository.find() → MongoDB read model (Layer 5+)
               → returns DTO (never the domain aggregate)
```

Until Layer 5, both sides read/write PostgreSQL. MongoDB read models are introduced at Layer 5 alongside Kafka projections.

---

## Data Ownership

| Store | Owns |
|---|---|
| **PostgreSQL** | Users, roles, orders, positions — anything requiring ACID transactions |
| **MongoDB** | Price history (OHLCV), read model projections, portfolio dashboards |
| **Kafka** | In-flight domain events between bounded contexts |
| **Elasticsearch** (Layer 9) | Log aggregation, metrics, alerting dashboards |

---

## ID Strategy

All entity IDs use **TSID** (`io.hypersistence:hypersistence-tsid`):
- Stored as `BIGINT` in PostgreSQL
- Wrapped in value object records: `UserId`, `CryptoId`, etc.
- Time-sortable, collision-resistant, no UUID overhead

---

## Security Model

```
Browser
  │  POST /api/v1/auth/login  {username, password}
  ▼
AuthController → LoginCommandHandler → BCrypt verify
  │
  └─▶ Set-Cookie: access_token=<JWT>;  HttpOnly; SameSite=Strict; Max-Age=900
      Set-Cookie: refresh_token=<JWT>; HttpOnly; SameSite=Strict; Max-Age=604800

Subsequent requests: browser sends cookies automatically
  │
JwtAuthenticationFilter: reads access_token cookie → validates → sets SecurityContext
  │
  └─▶ Falls back to Authorization: Bearer <token> (Swagger/Postman in dev)
```

CSRF not needed: `SameSite=Strict` on httpOnly cookies prevents cross-origin cookie submission.

---

## Microservice Extraction Candidates

| Bounded Context | Extract when |
|---|---|
| `marketdata` | Ingesting high-frequency WebSocket ticks requires independent scaling |
| `alerting` | Notification volume justifies separate deployment |
| `trading` | Regulatory or risk isolation requirements arise |

Target: stay monolith through Layer 8. Re-evaluate at Layer 3 based on `marketdata` ingestion load.

---

## Layer Roadmap

| # | Status | Description |
|---|---|---|
| 1 | Done | Auth backend — JWT httpOnly cookies, hexagonal, CQRS |
| 2 | Next | Auth frontend — Vite + React, login/register |
| 3 | | Market data backend — CoinGecko REST + Binance WebSocket |
| 4 | | Market data frontend — real-time price UI |
| 5 | | Kafka + MongoDB read models (CQRS fully event-driven) |
| 6 | | Portfolio & positions |
| 7 | | Alert/rule engine + email |
| 8 | | Alpaca paper trading orders |
| 9 | | ELK stack |
| 10 | | AWS deployment |