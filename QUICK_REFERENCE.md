# Quick Reference - Security & Testing Implementation

## 🔐 JWT Authentication Flow

```
┌─────────────────────────────────────────────────────────────────┐
│ 1. USER LOGS IN                                                 │
│    POST /api/v1/auth/login                                      │
│    { "username": "user", "password": "password" }               │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│ 2. TOKENS GENERATED                                             │
│    JwtTokenProvider.generateAccessToken()                       │
│    - Access Token (1 hour)                                      │
│    - Refresh Token (24 hours)                                   │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│ 3. CLIENT STORES TOKEN                                          │
│    Save in localStorage or sessionStorage                       │
│    Later requests include: Authorization: Bearer <token>        │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│ 4. EACH REQUEST VALIDATED                                       │
│    JwtAuthenticationFilter intercepts request                   │
│    - Extract token from header                                  │
│    - Validate signature (using secret key)                      │
│    - Check expiration                                           │
│    - Extract username                                           │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│ 5. SECURITY CONTEXT SET                                         │
│    SecurityContextHolder.getContext().setAuthentication()       │
│    - User available to @PreAuthorize methods                    │
│    - Spring knows user is authenticated                         │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│ 6. REQUEST ALLOWED/DENIED                                       │
│    Valid token → Process request                                │
│    Invalid token → 403 Forbidden                                │
│    No token → 401 Unauthorized                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🧪 AssertJ vs JUnit Assertions

### JUnit Style (Old)
```java
// ❌ Less readable, error message unclear
assertNotNull(result);
assertEquals(result.getSymbol(), "BTC");
assertTrue(result.getPrice().compareTo(BigDecimal.ZERO) > 0);
assertThrows(CryptoNotFoundException.class, () -> {
    service.getCryptoById(invalidId);
});
```

### AssertJ Style (New) ✅
```java
// ✅ Fluent, readable, better errors
assertThat(result)
    .isNotNull()
    .extracting(Crypto::getSymbol)
    .isEqualTo("BTC");

assertThat(result.getPrice())
    .isPositive()
    .isGreaterThan(BigDecimal.ZERO);

assertThatThrownBy(() -> service.getCryptoById(invalidId))
    .isInstanceOf(CryptoNotFoundException.class)
    .hasMessageContaining("not found");

// Soft assertions - all failures reported
assertSoftly(softly -> {
    softly.assertThat(result.getSymbol()).isEqualTo("BTC");
    softly.assertThat(result.getPrice()).isPositive();
    softly.assertThat(result.getName()).isNotBlank();
});
```

---

## 📊 SOLID Principles - Quick Reference

### Single Responsibility ✅
```
JwtTokenProvider          → ONLY token operations
JwtAuthenticationFilter   → ONLY request filtering
SecurityConfig            → ONLY security setup
CryptoService             → ONLY business logic
```

### Open/Closed ✅
```
Can add new authentication methods
    WITHOUT changing existing code
    
Example: Add OAuth2 adapter
    → Implement new filter
    → Add to SecurityConfig
    → No changes to JwtTokenProvider
```

### Liskov Substitution ✅
```
Any PasswordEncoder → Can replace any other
Any AuthenticationProvider → Can substitute

SecurityConfig doesn't care which implementation
```

### Interface Segregation ✅
```
CryptoRepository
    → Only CRUD methods
    → NOT event publishing
    
CryptoEventPublisher  
    → Only event methods
    → NOT persistence
    
NOT combined into: CryptoService (too big)
```

### Dependency Inversion ✅
```
Service depends on:
    CryptoRepository (abstraction)  ← Interface
    CryptoEventPublisher (abstraction)  ← Interface
    
NOT:
    JooqCryptoRepositoryAdapter (concrete)
    KafkaCryptoEventPublisher (concrete)
```

---

## 🔄 Database DDL Auto Comparison

### ✅ CORRECT (This Project)
```properties
spring.jpa.hibernate.ddl-auto=none
spring.flyway.enabled=true

Benefits:
  ✅ Safe for production
  ✅ All changes in version control
  ✅ Clear migration history
  ✅ Team coordination possible
```

### ❌ WRONG
```properties
spring.jpa.hibernate.ddl-auto=create
spring.jpa.hibernate.ddl-auto=update
spring.jpa.hibernate.ddl-auto=create-drop

Problems:
  ❌ Unsafe for production (automatic changes)
  ❌ No audit trail
  ❌ Can destroy data
  ❌ Hard to coordinate in teams
```

---

## 🛡️ Security Checklist

### Authentication ✅
- [x] JWT token generation
- [x] Token validation
- [x] Token expiration (1 hour)
- [x] Refresh token (24 hours)
- [x] BCrypt password encoding

### Authorization ✅
- [x] SecurityConfig with endpoint rules
- [x] Public vs protected endpoints
- [x] CORS configuration
- [x] CSRF disabled (stateless)

### Infrastructure ✅
- [x] Secrets in environment variables
- [x] No hardcoded passwords
- [x] JWT secret 256+ bits
- [x] Stateless sessions

### TODO (Next Phase)
- [ ] User database integration
- [ ] Account lockout mechanism
- [ ] Rate limiting
- [ ] Security audit logging
- [ ] Token blacklist for logout

---

## 🧪 Testing Checklist

### Unit Tests ✅
- [x] Mocked dependencies
- [x] AssertJ assertions
- [x] Soft assertions for multiple checks
- [x] Exception testing

### Test Framework ✅
- [x] Mockito extension
- [x] @Mock for mocks
- [x] @InjectMocks for service
- [x] JUnit 5 native

### Coverage ✅
- [x] JaCoCo 60% minimum
- [x] Critical paths 100%
- [x] Excludes: config, test classes
- [x] Generators included

### TODO (Next Phase)
- [ ] Integration tests with TestContainers
- [ ] End-to-end API tests
- [ ] Security scenario tests
- [ ] Performance tests

---

## 📦 API Endpoints Reference

### Public Endpoints
```
POST   /api/v1/auth/login              → Get JWT token
POST   /api/v1/auth/refresh            → Refresh access token
POST   /api/v1/auth/logout             → Logout
GET    /api/v1/cryptos                 → List all
GET    /api/v1/cryptos/{id}            → Get by ID
GET    /api/v1/cryptos/symbol/{symbol} → Get by symbol
GET    /swagger-ui.html                → API docs
```

### Protected Endpoints
```
POST   /api/v1/cryptos                 → Create (requires token)
PUT    /api/v1/cryptos/{id}            → Update (requires token)
DELETE /api/v1/cryptos/{id}            → Delete (requires token)
```

---

## 🔑 Configuration Reference

### JWT Settings (application.properties)
```properties
jwt.secret=your-very-long-256-bits-secret
jwt.expiration=3600000              # 1 hour
jwt.refresh-expiration=86400000     # 24 hours
```

### Database Settings
```properties
spring.jpa.hibernate.ddl-auto=none  # ✅ MUST be none
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
```

### Security Settings
```properties
spring.security.user.name=admin
spring.security.user.password=admin123
```

---

## 🚀 Getting Started

### 1. Build
```bash
mvn clean install
```

### 2. Run
```bash
docker-compose up -d
mvn spring-boot:run
```

### 3. Login
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

### 4. Use Token
```bash
TOKEN="<from_login_response>"
curl -X POST http://localhost:8080/api/v1/cryptos \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"symbol":"BTC","name":"Bitcoin",...}'
```

### 5. Check Swagger
```
http://localhost:8080/swagger-ui.html
```

---

## 📚 File Reference

### Core Security Files
- `JwtTokenProvider.java` - Token generation/validation
- `JwtAuthenticationFilter.java` - Request validation
- `SecurityConfig.java` - Spring Security configuration
- `AuthenticationController.java` - Login endpoints

### Configuration Files
- `application.properties` - All settings
- `docker-compose.yml` - Infrastructure
- `pom.xml` - Dependencies

### Documentation Files
- `SOLID_SECURITY_GUIDE.md` - Complete reference
- `SECURITY_TESTING_UPDATE.md` - Implementation details
- `QUICK_REFERENCE.md` - This file

### Test Files
- `CryptoTest.java` - Domain tests (AssertJ)
- `CryptoApplicationServiceTest.java` - Service tests (Mockito)
- `CryptoControllerTest.java` - REST tests

---

## ⚠️ Important Notes

### Production Deployment
1. Change JWT secret to strong random value
2. Enable HTTPS/TLS
3. Integrate with real user database
4. Implement account lockout
5. Add rate limiting
6. Set up security audit logging
7. Configure CORS for actual domain

### Development
1. Keep default credentials for testing
2. Use localhost JWT secret (fine for dev)
3. Test with Swagger UI
4. Run tests regularly

### Testing Recommendations
1. Use AssertJ for all tests
2. Mock external dependencies
3. Test both success and failure paths
4. Soft assertions for multiple checks
5. Clear test names describing intent

---

## 🎯 Next Implementation (Recommend Order)

1. **User Management** (1-2 days)
   - User entity, repository, service
   - Registration endpoint
   - Password validation

2. **Authorization** (1-2 days)
   - Role entity, mapping
   - @PreAuthorize annotations
   - Permission checks

3. **Advanced Security** (2-3 days)
   - Token blacklist for logout
   - Account lockout
   - Rate limiting
   - Audit logging

4. **Integration Tests** (2-3 days)
   - TestContainers setup
   - Database integration
   - End-to-end tests
   - Security scenarios

---

**Status**: 🟢 Ready for next implementation phase!

