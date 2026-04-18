# Trading Core - Spring Boot 3 with Hexagonal Architecture

A production-ready Spring Boot 3 application with Java 25, featuring hexagonal architecture for a cryptocurrency trading domain with CRUD operations, PostgreSQL, Flyway migrations, Kafka event publishing, and comprehensive test coverage with JaCoCo.

## Architecture

The project follows **hexagonal architecture** principles:

```
com.msj/
├── domain/
│   └── crypto/
│       ├── Crypto.java           # Aggregate root
│       ├── CryptoId.java         # Value object
│       └── CryptoNotFoundException.java
├── application/
│   └── service/
│       └── CryptoApplicationService.java  # Application service
├── infrastructure/
│   ├── ports/                    # Port interfaces
│   │   └── crypto/
│   │       ├── CryptoRepository.java       # Persistence port
│   │       └── CryptoEventPublisher.java   # Event port
│   └── adapters/                # Adapter implementations
│       ├── persistence/
│       │   └── JooqCryptoRepositoryAdapter.java
│       └── kafka/
│           └── KafkaCryptoEventPublisher.java
├── controller/
│   └── CryptoController.java     # REST API endpoints
└── config/
    └── KafkaConfig.java          # Infrastructure configuration
```

## Tech Stack

- **Java 25** - Latest JDK with modern language features
- **Spring Boot 3.3.0** - Latest Spring Boot version
- **PostgreSQL** - Relational database
- **JOOQ 3.19.8** - Type-safe SQL builder and query DSL
- **Flyway** - Database version control and migrations
- **Apache Kafka** - Event streaming and message publishing
- **JaCoCo 0.8.10** - Code coverage analysis (60% minimum coverage)
- **Lombok** - Reduce boilerplate code
- **JUnit 5 & Mockito** - Testing framework

## Quick Start

### Prerequisites

- Java 25+
- Maven 3.8.1+
- PostgreSQL 14+
- Docker & Docker Compose (optional)

### Setup

1. **Create PostgreSQL database:**
   ```bash
   createdb trading_core
   ```

2. **Update database credentials** in `src/main/resources/application.properties`

3. **Build the project:**
   ```bash
   mvn clean install
   ```

4. **Run the application:**
   ```bash
   mvn spring-boot:run
   ```

The application will start on `http://localhost:8080`

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/cryptos` | Create a new crypto |
| GET | `/api/v1/cryptos` | Get all cryptos |
| GET | `/api/v1/cryptos/{id}` | Get crypto by ID |
| GET | `/api/v1/cryptos/symbol/{symbol}` | Get crypto by symbol |
| PUT | `/api/v1/cryptos/{id}` | Update crypto |
| DELETE | `/api/v1/cryptos/{id}` | Delete crypto |

## Testing

```bash
mvn test
mvn clean test  # With coverage report
```

Coverage report: `target/site/jacoco/index.html`

## Database Migrations

Flyway migrations are in `src/main/resources/db/migration/`. Add new migrations with naming convention `V{number}__Description.sql`

## Kafka Configuration

The application publishes events to:
- `crypto-created` - When a crypto is created
- `crypto-updated` - When a crypto is updated
- `crypto-deleted` - When a crypto is deleted

## JaCoCo Code Coverage

Minimum line coverage: **60%**

Configuration in `pom.xml`. Adjust threshold as needed.

## Logging

Configure in `application.properties`:
```properties
logging.level.com.msj=DEBUG
logging.level.org.springframework.web=DEBUG
```

## Project Structure

```
trading-core/
├── pom.xml
├── README.md
├── src/
│   ├── main/
│   │   ├── java/com/msj/
│   │   │   ├── TradingCoreApplication.java
│   │   │   ├── domain/
│   │   │   ├── application/
│   │   │   ├── infrastructure/
│   │   │   ├── controller/
│   │   │   └── config/
│   │   └── resources/
│   │       ├── application.properties
│   │       └── db/migration/
│   └── test/java/com/msj/
└── target/
```

## Docker Compose for Local Development

See `docker-compose.yml` for Kafka setup.
