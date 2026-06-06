# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
mvn clean install                                      # Full build + tests + coverage check
mvn compile                                            # Compile only
mvn test                                               # Run all tests + JaCoCo report
mvn test -Dtest=RegisterCommandHandlerTest             # Single test class
mvn test -Dtest=RegisterCommandHandlerTest#register_success  # Single test method
mvn spring-boot:run                                    # Run locally
open target/site/jacoco/index.html                     # Coverage report
```

JaCoCo enforces **80% minimum line coverage per package**. The build fails below this threshold.

## Vision

Personal crypto trading platform: real-time price monitoring, algorithm/rule engine, email + push alerts, paper trading (Alpaca sandbox) → real orders, order management. Built as a **modular monolith** first; bounded contexts extracted to microservices when scaling justifies it.

## Architecture

### Bounded contexts (current + planned)

```
com.msj/
├── auth/          ← Layer 1 (DONE): JWT auth, user management
├── marketdata/    ← Layer 3: CoinGecko REST + Binance WebSocket price feeds
├── trading/       ← Layer 8: Alpaca paper orders, order management
├── portfolio/     ← Layer 6: positions, P&L
├── alerting/      ← Layer 7: rule engine, email, notifications
└── shared/        ← Shared kernel: base types, common events (add when needed)
```

Each bounded context is internally structured as:
```
{bc}/
├── domain/           Pure domain: aggregates, value objects, domain exceptions
├── application/
│   ├── command/      CommandHandlers (write side — mutates state)
│   └── query/        QueryHandlers (read side — returns DTOs, no mutation)
├── infrastructure/
│   ├── ports/        Interfaces owned by the application (UserRepository, etc.)
│   └── adapters/     Implementations: JOOQ, Kafka, HTTP clients
└── api/              REST controllers + DTOs (inbound adapter)
```

### CQRS pattern

```
POST /api/v1/auth/login
  → LoginCommandHandler → domain logic → PostgreSQL write → Kafka event (future)

GET /api/v1/users/me
  → GetUserProfileQueryHandler → PostgreSQL read → DTO returned
```

- **Command side**: Commands are Java records (`LoginCommand`, `RegisterCommand`). Handlers are `@Service` classes. Write to PostgreSQL. Return domain object or void.
- **Query side**: Queries are Java records (`GetUserProfileQuery`). Handlers are `@Service` classes. Read from PostgreSQL (later MongoDB for read models). Return response DTOs directly.
- **Naming**: `GetXxxQuery` / `GetXxxQueryHandler` for query side. Repository methods still use `findByXxx` (Spring convention).

### Data split

| PostgreSQL | MongoDB (Layer 5+) |
|---|---|
| Commands, orders, positions, users | Read models, price history, OHLCV candles |
| Financial integrity, transactions | Denormalized query projections, dashboards |
| Auth, roles, permissions | Market data time-series |

### Technology stack

| Concern | Technology |
|---|---|
| Language | Java 25 (Eclipse Temurin LTS) |
| Framework | Spring Boot 3.5 |
| Persistence (write) | JOOQ 3.19 (type-safe SQL, no code-gen — hand-written `Tables.java`) |
| Persistence (read, Layer 5+) | MongoDB |
| DB migrations | Flyway — DDL managed exclusively here |
| IDs | TSID (hypersistence-tsid) — BIGINT in DB, wrapped in value object records |
| Event bus | Kafka (Layer 5+) |
| Security | Spring Security + JWT (httpOnly cookies) |
| Market data | CoinGecko REST + Binance WebSocket (Layer 3) |
| Paper trading | Alpaca sandbox API (Layer 8) |

## auth/ bounded context (Layer 1 — completed)

### JWT cookie strategy

- **Access token**: 15 min, `httpOnly`, `SameSite=Strict`, `secure=false` dev / `true` prod
- **Refresh token**: 7 days, `httpOnly`, `SameSite=Strict`
- `JwtAuthenticationFilter` reads cookie first, falls back to `Authorization: Bearer` header (keeps Swagger/Postman working in dev)
- CSRF: disabled — `SameSite=Strict` on httpOnly cookies makes CSRF impossible

### Endpoints

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/api/v1/auth/register` | Public | Register new user |
| POST | `/api/v1/auth/login` | Public | Login, sets cookies |
| POST | `/api/v1/auth/logout` | Required | Clears cookies |
| POST | `/api/v1/auth/refresh` | Cookie | Issues new access token |
| GET | `/api/v1/users/me` | Required | Current user profile |

### Account security

- BCrypt strength 12
- Account locks after 5 failed login attempts (30 min lockout, auto-unlocks)
- `BadCredentialsException` always returns generic "Invalid credentials" (no username enumeration)
- Default role on registration: `ROLE_USER` (seeded in V2 migration, id=2)

## Database migrations

Flyway migrations in `src/main/resources/db/migration/`. Naming convention: **one file per month**, `V{n}__{YYYY}_{Month}.sql`.

- `V1__2026_April.sql` — `auth` schema: `users`, `roles`, `user_roles` + seed data
- `V2__2026_April.sql` — `auth.refresh_tokens` (server-side token revocation)

When adding tables or columns within the same calendar month, increment V and keep the same month name (e.g. `V3__2026_April.sql`). Start a new month name when the calendar month changes (e.g. `V4__2026_May.sql`).

## Key configuration (application.properties)

```properties
jwt.secret              # Set via JWT_SECRET env var in prod (min 512-bit for HS512)
jwt.access-token-expiration-ms   # 900000 (15 min)
jwt.refresh-token-expiration-ms  # 604800000 (7 days)
app.security.cookie.secure       # false dev, true prod (COOKIE_SECURE env var)
app.security.cookie.domain       # empty dev, set in prod (COOKIE_DOMAIN env var)
```

## Microservice extraction guide

Advise extraction when a bounded context reaches any of these thresholds:
- Needs to scale independently (e.g., `marketdata` ingesting high-frequency WebSocket ticks)
- Has a separate deployment lifecycle from the rest of the app
- Is owned by a distinct team or has a radically different failure tolerance

Current recommendation: stay monolith through Layer 8. Evaluate `marketdata` extraction at Layer 3/4 based on ingestion volume.

## Layer roadmap

| Layer | Status | Description |
|---|---|---|
| 1 | Done | Auth backend (JWT httpOnly cookies, CQRS, hexagonal) |
| 2 | Done | Auth frontend (Vite + React 19, login/register, Zustand, React Query) |
| 3 | Done | Market data backend (CoinGecko REST + Binance WebSocket, Caffeine cache) |
| 4 | Done | Market data frontend (AG Grid, 250 coins, live Binance indicator, Iris logo, Lucide icons) |
| 5 | Next | Kafka + MongoDB: persist price ticks, OHLCV candles, candlestick chart |
| 6 | | Portfolio & positions |
| 7 | | Alert engine (rules + email) |
| 8 | | Alpaca paper trading orders |
| 9 | | Observability: Prometheus + Grafana (metrics), Loki (logs), Tempo (traces via OpenTelemetry) |
| 10 | | SonarCloud (code quality), ArgoCD + Helm (GitOps CD), Traefik (ingress/LB/SSL) |
| 11 | | Kubernetes (minikube local → AWS EKS prod), HPA for auto-scaling |

## DevOps & infrastructure stack (Layers 9–11)

| Concern | Tool | Notes |
|---|---|---|
| Git + CI | GitHub + GitHub Actions | Free; builds, tests, pushes image to ghcr.io |
| Container registry | ghcr.io | Free, native GitHub integration |
| Code quality | SonarCloud | Free for public repos; wired to GitHub PRs |
| GitOps / CD | ArgoCD + Helm | Git is source of truth; auto-syncs k8s on push |
| Ingress / reverse proxy / SSL | Traefik | Auto-discovers k8s services, Let's Encrypt, WebSocket support |
| Metrics | Prometheus + Grafana | Spring Boot Actuator → `/actuator/prometheus` → Prometheus → Grafana |
| Distributed traces | Tempo + Grafana | Via OpenTelemetry (micrometer-tracing-bridge-otel) |
| Logs | Loki + Grafana | Replaces ELK — lighter, cheaper, same Grafana dashboard |
| k8s local | minikube | Dev environment |
| k8s prod | AWS EKS | Layer 11 |
| Pod scaling | HPA | CPU/memory metrics; Kafka consumer lag for marketdata |

**Observability standard**: OpenTelemetry (OTel) — traces, metrics, logs unified under one SDK. All three pillars visible in a single Grafana dashboard.

**GraphQL**: defer to Layer 4+. Use Netflix DGS (Spring Boot native) if frontend needs multi-resource queries in one call. REST is correct for auth and simple endpoints.

**Inter-service communication**: Kafka (async, Layer 5+), gRPC (sync low-latency, when microservices are extracted).

## Layer 5 plan (next)

Goal: persist price history, enable charting.

```
BinanceWebSocketClient
  → publish KafkaProducer → topic: market.price.updated
      → Consumer A: persist raw tick → MongoDB price_ticks collection
      → Consumer B: aggregate OHLCV 1m candles → MongoDB ohlcv_1m collection
```

New query endpoint: `GET /api/v1/market/coins/{coinId}/candles?interval=1m&from=&to=`

Frontend: candlestick chart on coin row click using **TradingView Lightweight Charts** (free, Apache 2.0).

**Approach**: go slowly — master Kafka and MongoDB step by step. Start with Docker Compose, then producer, then consumers, then read model queries, then chart UI.

**Kafka serialization**: JSON (`JsonSerializer`/`JsonDeserializer`) for now — producer and consumer are in the same JVM so no schema contract is needed. **Migrate to Avro + Confluent Schema Registry when `marketdata` is extracted to its own microservice** (or when another bounded context like `alerting` or `trading` consumes the same topic from a separate service). Migration path: add Schema Registry to docker-compose, add Avro Maven plugin, write `.avsc` schema files, swap serializers in config. Drop and recreate the topic on cutover (market data is ephemeral, no historical loss).

## marketdata/ bounded context (Layer 3+4 — completed)

### Two WebSocket connections
- **Binance → Backend**: `BinanceWebSocketClient` opens one persistent Java `HttpClient` WebSocket to Binance combined stream (`wss://...?streams=btcusdt@miniTicker/...`). Ticks arrive ~1s, update Caffeine cache, broadcast to STOMP.
- **Backend → Frontend**: Spring STOMP broker at `/ws` (SockJS fallback). Frontend subscribes to `/topic/prices`. `SimpMessagingTemplate.convertAndSend` fans out to all connected browsers.

### Price data flow
```
Binance WS → BinanceWebSocketClient → InMemoryPriceCache (Caffeine)
                                     → SimpMessagingTemplate → /topic/prices → browser
CoinGecko REST (every 60s) → InMemoryPriceCache (source of truth for non-Binance coins)
```

### No price persistence yet
All prices are ephemeral — Caffeine cache only. No history. Addressed in Layer 5.

### CoinGecko API key
`coingecko.api-key=${COINGECKO_API_KEY:}` — defaults to empty (unauthenticated). Works in dev; rate-limited to shared anonymous quota. Free Demo key optional. The `x-cg-demo-api-key` header is only sent when key is non-blank.

### Endpoints

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/api/v1/market/coins?limit=250` | Required | Top N coins by market cap |
| GET | `/api/v1/market/coins/{coinId}` | Required | Single coin price |

## Frontend (separate project `trading-ui`, Layer 2+)

**Stack**: Vite + React 19, TanStack Query v5, Zustand (UI state), AG Grid Community v35 (data grids), `@stomp/stompjs` + SockJS (WebSocket), Tailwind CSS v4, Lucide React (icons). No Redux.

**AG Grid setup**:
- Uses JS theming API: `themeQuartz.withParams({...})` — no CSS class approach
- `columnMenu="legacy"` required to fix React 19 event delegation conflict with filter popups
- `popupParent={document.body}` for filter popup positioning
- Live price updates via `gridApi.applyTransaction({ update: rows })` — no full re-render
- `enableCellChangeFlash: true` on price column for Binance tick flash

**Iris branding**:
- Logo: `src/shared/components/IrisLogo.tsx` — geometric SVG eye in teal
- Product name: Iris