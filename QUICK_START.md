# Quick Start Guide - Trading Core

## Prerequisites Installed ✅
- ✅ Maven 3.9.14
- ✅ Java 25.0.2
- ✅ Java 21.0.8 (available)

## Step 1: Start Infrastructure (5 minutes)

```bash
# Ensure Docker is running, then start services
docker-compose up -d

# Verify services are running
docker-compose ps
```

Expected output:
```
CONTAINER ID   IMAGE                             PORTS
<id>          postgres:16-alpine                5432:5432
<id>          confluentinc/cp-zookeeper:7.5.0  2181:2181
<id>          confluentinc/cp-kafka:7.5.0      9092:9092
```

## Step 2: Create Database (2 minutes)

```bash
# Method 1: Using createdb
createdb trading_core

# Method 2: Using psql
psql -U postgres -c "CREATE DATABASE trading_core;"

# Verify
psql -U postgres -l | grep trading_core
```

## Step 3: Build Project (5 minutes)

```bash
cd /Users/mohamedjmal/finance/trading-core

# Full build with tests
mvn clean install

# Quick compile only
mvn clean compile
```

## Step 4: Run Application (1 minute)

```bash
# Option 1: Using Maven
mvn spring-boot:run

# Option 2: Using Java (after packaging)
mvn clean package
java -jar target/trading-core-1.0-SNAPSHOT.jar
```

Expected output:
```
...
Started TradingCoreApplication in X.XXX seconds (JVM running for X.XXX)
```

Application available at: `http://localhost:8080`

## Step 5: Test the API (2 minutes)

### 5a. Create a Crypto

```bash
curl -X POST http://localhost:8080/api/v1/cryptos \
  -H "Content-Type: application/json" \
  -d '{
    "symbol": "BTC",
    "name": "Bitcoin",
    "currentPrice": 50000.00,
    "marketCap": 1000000000000.00,
    "volume24h": 50000000000.00,
    "changePercent24h": 2.50,
    "description": "The original cryptocurrency"
  }'
```

Response:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "symbol": "BTC",
  "name": "Bitcoin",
  "currentPrice": 50000.00,
  "marketCap": 1000000000000.00,
  "volume24h": 50000000000.00,
  "changePercent24h": 2.50,
  "description": "The original cryptocurrency",
  "createdAt": "2026-04-11T10:30:00",
  "updatedAt": "2026-04-11T10:30:00"
}
```

### 5b. Get All Cryptos

```bash
curl http://localhost:8080/api/v1/cryptos
```

### 5c. Get Crypto by Symbol

```bash
curl http://localhost:8080/api/v1/cryptos/symbol/BTC
```

### 5d. Get Crypto by ID

```bash
curl http://localhost:8080/api/v1/cryptos/{id}
```

### 5e. Update Crypto

```bash
curl -X PUT http://localhost:8080/api/v1/cryptos/{id} \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Bitcoin Updated",
    "currentPrice": 55000.00
  }'
```

### 5f. Delete Crypto

```bash
curl -X DELETE http://localhost:8080/api/v1/cryptos/{id}
```

## Step 6: Run Tests (3 minutes)

```bash
# Run all tests
mvn test

# Run with coverage report
mvn clean test

# View coverage report
open target/site/jacoco/index.html
```

Expected: 25+ tests passing with 60%+ code coverage

## Step 7: Monitor Logs

```bash
# View Spring Boot logs
tail -f /var/log/trading-core/application.log

# View application logs (if redirected)
tail -f logs/trading-core.log

# In console, logs show DEBUG level for com.msj package
```

## Step 8: Verify Kafka Events

```bash
# List Kafka topics
docker exec trading-core-kafka kafka-topics.sh --bootstrap-server localhost:9092 --list

# Expected topics:
# - crypto-created
# - crypto-updated
# - crypto-deleted

# Consume from topic
docker exec -it trading-core-kafka kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic crypto-created \
  --from-beginning
```

## Troubleshooting

### PostgreSQL Connection Error
```
Error: Could not connect to database
```
**Solution:**
```bash
# Check if PostgreSQL is running
docker-compose ps | grep postgres

# Check logs
docker-compose logs postgres

# Restart PostgreSQL
docker-compose restart postgres
```

### Maven Command Not Found
```
zsh: command not found: mvn
```
**Solution:**
```bash
# Reinstall Maven
brew reinstall maven

# Or add to PATH
export PATH="/opt/homebrew/bin:$PATH"
```

### Java 25 Not Available
```
Error: Could not find or load main class
```
**Solution:**
```bash
# Install Java 25
brew tap homebrew/cask-versions
brew install java25

# Set JAVA_HOME
export JAVA_HOME=$(/usr/libexec/java_home -v 25)
```

### Port Already in Use
```
Error: Port 8080 already in use
```
**Solution:**
```bash
# Change port in application.properties
server.port=8081

# Or kill process on port
lsof -i :8080
kill -9 <PID>
```

### Kafka Not Starting
```
Error: Failed to connect to Kafka
```
**Solution:**
```bash
# Check Zookeeper is healthy
docker-compose logs zookeeper

# Restart Kafka stack
docker-compose restart zookeeper kafka
```

## Development Workflow

### 1. Add New Domain Entity
```bash
# Create entity in src/main/java/com/msj/domain/{entity}/
# Create repository port in src/main/java/com/msj/infrastructure/ports/{entity}/
# Create adapter in src/main/java/com/msj/infrastructure/adapters/persistence/
```

### 2. Create REST Endpoint
```bash
# Create controller in src/main/java/com/msj/controller/
# Add tests in src/test/java/com/msj/controller/
```

### 3. Add Database Migration
```bash
# Create migration in src/main/resources/db/migration/
# Naming: V{number}__Description.sql
# Flyway auto-runs on startup
```

### 4. Ensure Tests Pass
```bash
mvn clean test
# Coverage must be >= 60%
```

## Useful Commands

```bash
# Build without tests
mvn clean package -DskipTests

# Run specific test
mvn -Dtest=CryptoTest test

# Generate JOOQ code (requires DB running)
mvn clean generate-sources

# Format code
mvn formatter:format

# Check for vulnerabilities
mvn dependency-check:check

# View dependency tree
mvn dependency:tree

# Clean everything
mvn clean
rm -rf target/
```

## Project URLs

| Service | URL |
|---------|-----|
| Application | http://localhost:8080 |
| PostgreSQL | localhost:5432 |
| Kafka | localhost:9092 |
| Zookeeper | localhost:2181 |

## Performance Tips

1. **Indexing**: Database queries use indexes on symbol, created_at, updated_at
2. **Connection Pooling**: Configured in application.properties
3. **Batch Operations**: JPA batch_size=20 configured
4. **Caching**: Add Spring Cache if needed

## Architecture Highlights

```
REST API
  ↓
CryptoController (Presentation)
  ↓
CryptoApplicationService (Application)
  ↓
CryptoRepository Port (Interface)
  ↓
JooqCryptoRepositoryAdapter (Adapter)
  ↓
PostgreSQL Database

Events ↓
  ↓
CryptoEventPublisher Port (Interface)
  ↓
KafkaCryptoEventPublisher (Adapter)
  ↓
Kafka Topics
```

## Next Steps

1. ✅ **Immediate**: Follow steps 1-7 to get application running
2. **Short-term**: Add more domain entities and endpoints
3. **Medium-term**: Add authentication/authorization
4. **Long-term**: Add additional adapters (REST client, gRPC, etc.)

## Support

- 📖 Full documentation: `README.md`
- 📋 Implementation details: `IMPLEMENTATION_SUMMARY.md`
- 🔧 Configuration: `src/main/resources/application.properties`

---

**Estimated Total Setup Time**: 20-30 minutes

Happy coding! 🚀

