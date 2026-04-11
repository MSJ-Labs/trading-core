# Maven Build Configuration Reference

## Build Commands

### Development

```bash
# Full clean build with tests
mvn clean install

# Fast compile without tests
mvn clean compile

# Run application
mvn spring-boot:run

# Run specific tests
mvn -Dtest=CryptoTest test
mvn -Dtest=CryptoApplicationServiceTest test
mvn -Dtest=CryptoControllerTest test
```

### Testing & Coverage

```bash
# Run all tests
mvn test

# Run tests with JaCoCo coverage
mvn clean test

# View coverage report
open target/site/jacoco/index.html

# Skip coverage check (for quick builds)
mvn test -Djacoco.skip=true

# Run integration tests
mvn -Dtest=TradingCoreApplicationTests test
```

### Building

```bash
# Package application (creates JAR)
mvn clean package

# Package without tests
mvn clean package -DskipTests

# Build and skip code coverage check
mvn clean package -Djacoco.skip=true

# Build with specific Java version
mvn clean package -Djavadoc.skip=true
```

### Code Generation

```bash
# Generate JOOQ code (requires DB running)
mvn clean generate-sources

# Skip JOOQ generation
mvn clean compile -Djooq.skip=true
```

### Debugging

```bash
# Build with debug logging
mvn clean install -X

# Build with timing information
mvn clean install -T 1C

# Build specific module only
mvn clean install -pl module-name

# Offline mode (use cached dependencies)
mvn clean install -o
```

## Maven Properties

### Override Properties

```bash
# Change database credentials
mvn -Dspring.datasource.url=jdbc:postgresql://host:port/db \
    -Dspring.datasource.username=user \
    -Dspring.datasource.password=pass \
    clean install

# Change server port
mvn -Dserver.port=9090 spring-boot:run

# Skip JaCoCo coverage
mvn test -Djacoco.skip=true
```

## POM Configuration Details

### Dependency Management

The project uses Spring Boot BOM (Bill of Materials) for consistent dependency versions:

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-dependencies</artifactId>
      <version>3.3.0</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```

### Java Version

```xml
<properties>
  <maven.compiler.source>25</maven.compiler.source>
  <maven.compiler.target>25</maven.compiler.target>
</properties>
```

### Key Dependencies

- `spring-boot-starter-web`: REST API
- `spring-boot-starter-data-jpa`: ORM support
- `spring-boot-starter-jooq`: JOOQ support
- `spring-kafka`: Kafka integration
- `flyway-core`: Database migrations
- `postgresql`: PostgreSQL driver
- `jooq`: Query builder
- `lombok`: Boilerplate reduction

### Test Dependencies

- `spring-boot-starter-test`: Spring Test, JUnit 5, Mockito
- `spring-kafka-test`: Kafka test support
- `testcontainers`: Docker-based test containers
- `testcontainers:postgresql`: PostgreSQL test container

## Build Plugins

### Maven Compiler Plugin
```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-compiler-plugin</artifactId>
  <version>3.11.0</version>
</plugin>
```

### Spring Boot Maven Plugin
```xml
<plugin>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-maven-plugin</artifactId>
  <version>3.3.0</version>
</plugin>
```

### JOOQ Code Generation
```xml
<plugin>
  <groupId>org.jooq</groupId>
  <artifactId>jooq-codegen-maven</artifactId>
  <version>3.19.8</version>
  <phase>generate-sources</phase>
</plugin>
```

### JaCoCo Coverage
```xml
<plugin>
  <groupId>org.jacoco</groupId>
  <artifactId>jacoco-maven-plugin</artifactId>
  <version>0.8.10</version>
  <!-- Minimum 60% line coverage -->
</plugin>
```

### Maven Surefire
```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-surefire-plugin</artifactId>
  <version>3.0.0</version>
  <!-- Runs *Test.java and *Tests.java -->
</plugin>
```

## Profiles (For Future Use)

Add to pom.xml to enable environment-specific builds:

```xml
<profiles>
  <profile>
    <id>dev</id>
    <properties>
      <spring.profiles.active>dev</spring.profiles.active>
    </properties>
  </profile>
  <profile>
    <id>prod</id>
    <properties>
      <spring.profiles.active>prod</spring.profiles.active>
    </properties>
  </profile>
</profiles>
```

Usage: `mvn clean install -P dev`

## Settings.xml (Optional)

For Maven repositories and credentials, create/edit `~/.m2/settings.xml`:

```xml
<settings>
  <profiles>
    <profile>
      <id>default</id>
      <repositories>
        <repository>
          <id>central</id>
          <url>https://repo.maven.apache.org/maven2</url>
          <snapshots>
            <enabled>false</enabled>
          </snapshots>
        </repository>
      </repositories>
    </profile>
  </profiles>
  <activeProfiles>
    <activeProfile>default</activeProfile>
  </activeProfiles>
</settings>
```

## Environment Variables

```bash
# For Maven
export MAVEN_HOME=/opt/homebrew/Cellar/maven/3.9.14
export PATH=$MAVEN_HOME/bin:$PATH

# For Java 25
export JAVA_HOME=$(/usr/libexec/java_home -v 25)

# For Spring Boot
export SERVER_PORT=8080
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/trading_core
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=postgres
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

## Common Issues & Solutions

### Issue: "Could not find or load main class"
```bash
# Solution: Ensure Java 25 is set
export JAVA_HOME=$(/usr/libexec/java_home -v 25)
mvn clean compile
```

### Issue: JOOQ code generation fails
```bash
# Solution: Ensure PostgreSQL is running and schema exists
docker-compose up postgres -d
mvn clean generate-sources
```

### Issue: Tests fail with coverage
```bash
# Solution: Either fix coverage or skip
mvn test -Djacoco.skip=true

# Or run specific test
mvn -Dtest=CryptoTest test
```

### Issue: Slow Maven builds
```bash
# Solution: Use parallel builds
mvn -T 1C clean install

# Or skip tests for development
mvn clean install -DskipTests
```

### Issue: Port 8080 already in use
```bash
# Solution 1: Change port
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=9090"

# Solution 2: Kill process
lsof -i :8080 | grep LISTEN | awk '{print $2}' | xargs kill -9
```

## Performance Optimization

### Faster Builds
```bash
# Parallel compilation (-T flag)
mvn clean install -T 1C

# Incremental compilation
mvn -amd clean install -pl :trading-core

# Skip non-essential steps
mvn clean compile -DskipTests -Djacoco.skip=true
```

### Better Caching
```bash
# Use local repository cache
mvn clean install -o  # Offline mode (after initial build)

# Update snapshots
mvn clean install -U
```

## Continuous Integration

### For CI/CD Pipeline

```bash
# Build and test (typical CI command)
mvn clean install

# Generate coverage report (for SonarQube, etc.)
mvn clean test

# Build Docker image
mvn clean package -DskipTests
docker build -t trading-core:1.0-SNAPSHOT .
```

## Useful Maven Goals

```bash
# Display dependency tree
mvn dependency:tree

# Display plugin info
mvn help:describe -Dplugin=org.apache.maven.plugins:maven-compiler-plugin

# List all profiles
mvn help:all-profiles

# Show active profile info
mvn help:active-profiles

# Check for outdated dependencies
mvn versions:display-dependency-updates

# Check for plugin updates
mvn versions:display-plugin-updates
```

---

**Maven Version**: 3.9.14
**Java Version**: 25.0.2
**Spring Boot**: 3.3.0

