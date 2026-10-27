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
├── identity/      ← Keycloak identity: current user (UUID `sub`) and roles
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
│   ├── query/        QueryHandlers (read side — returns DTOs, no mutation)
│   ├── port/out/     Interfaces owned by the application (CurrentUserProvider, repositories...)
│   └── result/       What handlers return
├── infrastructure/
│   └── adapters/     Implementations of the ports: JOOQ, Kafka, HTTP clients, security context
└── api/              REST controllers + DTOs (inbound adapter)
```

### CQRS pattern

```
POST /api/v1/orders   (future)
  → PlaceOrderCommandHandler → domain logic → PostgreSQL write → Kafka event

GET /api/v1/me
  → GetCurrentIdentityQueryHandler → CurrentUserProvider port → DTO returned
```

- **Command side**: Commands are Java records (`PlaceOrderCommand`). Handlers are `@Service` classes. Write to PostgreSQL. Return domain object or void.
- **Query side**: Queries are Java records (`GetCoinQuery`). Handlers are `@Service` classes. Read from PostgreSQL (later MongoDB for read models). Return response DTOs directly.
- **Naming**: `GetXxxQuery` / `GetXxxQueryHandler` for query side. Repository methods still use `findByXxx` (Spring convention).

### Data split

| PostgreSQL | MongoDB (Layer 5+) |
|---|---|
| Commands, orders, positions, users | Read models, price history, OHLCV candles |
| Financial integrity, transactions | Denormalized query projections, dashboards |
| Business data keyed by Keycloak `sub` | Market data time-series |

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
| Security | Spring Security OAuth2 resource server + Keycloak 26 (JWT) |
| Market data | CoinGecko REST + Binance WebSocket (Layer 3) |
| Paper trading | Alpaca sandbox API (Layer 8) |

## Identity (Keycloak 26)

The home-made auth BC (JWT cookies, login/register, `auth` schema) is gone: Keycloak owns credentials and sessions. Endpoint today: `GET /api/v1/me` (Bearer token, returns `sub` and roles).

Target model:
- **BFF**: the Spring backend is a confidential OAuth2 client (`trading-bff`, Authorization Code + PKCE). Tokens never reach the browser, which only holds an httpOnly session cookie.
- **Resource server**: validates signature (JWKS), issuer and audience (`aud`), maps `realm_access.roles` to `ROLE_*`.
- **Identity**: Keycloak `sub` (UUID) is the user id everywhere, wrapped in a value object per BC; `AuthenticatedPrincipal` interface in `shared.infrastructure.security`, no Spring Security types in application/domain. Handlers get the caller through a `CurrentUserProvider` port (`application/port/out`), implemented by an adapter that translates the principal into the BC's own id type and refuses anonymous callers (see `identity/`).
- **Architecture rules**: `ArchitectureTest` (ArchUnit) enforces the layering above. Temporary exception, to remove: `marketdata` still keeps its ports in `infrastructure/ports` (move them to `application/port/out`).
- Real users self-register in Keycloak; Terraform never creates them.

Infrastructure as code (`infra/terraform/keycloak/`):
- No realm JSON import. Keycloak starts empty with a bootstrap service account (`KC_BOOTSTRAP_ADMIN_CLIENT_ID/SECRET`); Terraform logs in with it and creates the realm `trading`. In prod, replace it by a narrower account and delete the bootstrap one.
- **Never redeclare or import what Keycloak creates by default** (built-in client scopes `basic`/`roles`/`profile`/`email`, default roles): they change across Keycloak versions. Declare only our own roles, clients, mappers, dev accounts.
- Beware **authoritative** resources (`keycloak_openid_client_default_scopes`, `keycloak_realm_default_client_scopes`, `keycloak_default_roles`): they replace the whole list. Omitting `basic` removes the `sub` claim. Check `terraform plan` before every apply.
- Audience: prefer an audience mapper on the client; use a shared `aud-<service>` scope only when several clients need it.
- Secrets never appear in `.tf` files: `TF_VAR_*` from the gitignored `.env`. `tfstate` holds secrets in clear text: gitignored locally, encrypted remote backend in prod.
- **Before prod**: set `terraform_deletion_protection = true` on the realm; replace the bootstrap account by a permanent least-privilege one (split `envs/dev` into `bootstrap/`, logged in with the bootstrap account, which creates realm `trading` + client `terraform-provisioner` limited to `realm-admin` of that realm, and `config/`, logged in with the provisioner; then delete the bootstrap client in `master` by hand, Terraform cannot delete the account it is logged in with; secret in a secret manager, not `.env`); remote encrypted state; remove `trading-dev-tools` and dev users; `ssl_required` and real hostname/TLS.
- **Before prod (actuator)**: do not expose health/metrics on the public port. Serve actuator on a separate management port (`management.server.port`, e.g. 9090): Kubernetes probes hit the pod IP on that port, Prometheus scrapes it from inside the cluster, and the `Service`/`Ingress` only expose 8080. Then drop the `/actuator/**` `permitAll` lines from `SecurityConfig`.
- Run: `set -a; source .env; set +a; export TF_VAR_terraform_client_secret="$TERRAFORM_CLIENT_SECRET"; cd infra/terraform/keycloak; terraform plan`

## Database migrations

Flyway migrations in `src/main/resources/db/migration/`. Naming convention: **one file per month**, `V{n}__{YYYY}_{Month}.sql`.

No migration yet: the legacy `auth` migrations were deleted (pre-prod, no data to keep), the folder only holds `.gitkeep`. Flyway stays enabled and uses the default `public` schema. The first real table is `V1__2026_October.sql` (or the current month).

When adding tables or columns within the same calendar month, increment V and keep the same month name (e.g. `V2__2026_October.sql`). Start a new month name when the calendar month changes (e.g. `V3__2026_November.sql`).

## Key configuration (application.properties)

```properties
app.security.oauth2.issuer-uri   # KEYCLOAK_ISSUER_URI, default http://localhost:8180/realms/trading
app.security.oauth2.audience     # KEYCLOAK_AUDIENCE, default trading-core
app.security.oauth2.jwks-path    # KEYCLOAK_JWKS_PATH, default /protocol/openid-connect/certs
spring.datasource.hikari.maximum-pool-size  # 5 dev (HIKARI_MAX_POOL_SIZE env var; size to (db_core_count*2)+1 in prod)
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