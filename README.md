# Iris — Backend

> Personal crypto trading platform. Real-time price monitoring, alert engine, paper trading → real orders.

Spring Boot 3.5 backend built with hexagonal architecture and CQRS. Currently in active development — see layer roadmap below.

## Tech stack

| Concern | Technology |
|---|---|
| Language | Java 25 (Eclipse Temurin LTS) |
| Framework | Spring Boot 3.5 |
| Architecture | Hexagonal + CQRS (modular monolith) |
| Persistence (write) | PostgreSQL + JOOQ 3.19 (hand-written type-safe SQL) |
| Persistence (read) | MongoDB 7.0 |
| DB migrations | Flyway |
| IDs | TSID (hypersistence-tsid) — BIGINT in DB |
| Event bus | Kafka (Confluent KRaft, no ZooKeeper) |
| Security | Spring Security + JWT (httpOnly cookies, refresh tokens) |
| Market data | CoinGecko REST + Binance WebSocket |
| Test coverage | JaCoCo — 80% minimum line coverage per package |

## Getting started

### Prerequisites

- Java 25 (Eclipse Temurin)
- Maven 3.9+
- Docker + Docker Compose

### Setup

1. Start all infrastructure services (PostgreSQL, Kafka, MongoDB):
   ```bash
   docker compose up -d
   ```

2. Create your local properties file (gitignored):
   ```bash
   cp src/main/resources/application-local.properties.example src/main/resources/application-local.properties
   # Fill in jwt.secret — generate one with: openssl rand -base64 64
   ```

3. Run with the local profile:
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=local
   ```

The API starts on `http://localhost:8080`.

### Infrastructure ports

| Service | Port |
|---|---|
| PostgreSQL | 5432 |
| Kafka | 9092 |
| MongoDB | 27017 |

## API endpoints

### Auth (`/api/v1/auth`)

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/register` | Public | Create account |
| POST | `/login` | Public | Login, sets httpOnly JWT cookies |
| POST | `/logout` | Required | Clear cookies |
| POST | `/refresh` | Cookie | Issue new access token |
| GET | `/users/me` | Required | Current user profile |

### Market data (`/api/v1/market`)

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/coins?limit=250` | Required | Top N coins by market cap |
| GET | `/coins/{coinId}` | Required | Single coin price |
| GET | `/coins/{coinId}/candles?interval=1m&from=&to=` | Required | OHLCV candles from MongoDB |

## Architecture

Bounded contexts:

```
com.msj/
├── auth/          Layer 1 — JWT auth, user management, refresh tokens
├── marketdata/    Layer 3 — CoinGecko REST + Binance WebSocket price feeds
├── trading/       Layer 8 — Alpaca paper orders (planned)
├── portfolio/     Layer 6 — positions, P&L (planned)
├── alerting/      Layer 7 — rule engine, email alerts (planned)
└── shared/        Shared kernel
```

Each context follows:
```
{bc}/
├── domain/           Aggregates, value objects, domain exceptions
├── application/
│   ├── command/      Write side — mutates state
│   └── query/        Read side — returns DTOs
├── infrastructure/
│   ├── ports/        Interfaces (repositories, providers)
│   └── adapters/     JOOQ, HTTP clients, WebSocket
└── api/              REST controllers + request/response DTOs
```

## Layer roadmap

| Layer | Status | Description |
|---|---|---|
| 1 | Done | Auth backend — JWT httpOnly cookies, CQRS, hexagonal |
| 2 | Done | Auth frontend — Vite + React 19, login/register |
| 3 | Done | Market data backend — CoinGecko + Binance WebSocket, Caffeine cache |
| 4 | Done | Market data frontend — AG Grid, 250 coins, live price feed |
| 5 | In progress | Kafka + MongoDB — price tick persistence, OHLCV candles, candlestick chart |
| 6 | | Portfolio & positions |
| 7 | | Alert engine — rules + email |
| 8 | | Alpaca paper trading orders |
| 9 | | Observability — Prometheus + Grafana, Loki, Tempo (OpenTelemetry) |
| 10 | | SonarCloud, ArgoCD + Helm, Traefik |
| 11 | | Kubernetes — minikube local → AWS EKS prod |

## Commands

```bash
mvn clean install                                       # Full build + tests + coverage
mvn spring-boot:run                                     # Run locally
mvn test -Dtest=RegisterCommandHandlerTest              # Single test class
open target/site/jacoco/index.html                      # Coverage report
```

---

© 2026 Mohamed Jmal. All rights reserved.
