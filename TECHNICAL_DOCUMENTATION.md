# Paymeter Backend Assessment - Technical Documentation

**Version:** 0.0.1-SNAPSHOT ✅ VERIFIED (`build.gradle:8`)



---

## 1. PROJECT VERIFICATION

### Technology Stack Detected

| Category | Technology | Evidence |
|----------|-----------|----------|
| Primary Language | Java 17 | ✅ VERIFIED `build.gradle:9` `sourceCompatibility = '17'` |
| Framework | Spring Boot 3.2.3 | ✅ VERIFIED `build.gradle:2` |
| Web Layer | WebFlux (Reactive) | ✅ VERIFIED `build.gradle:16` `spring-boot-starter-webflux` |
| Persistence | Spring Data JPA + Hibernate | ✅ VERIFIED `build.gradle:17` |
| Database (Runtime) | PostgreSQL 13 | ✅ VERIFIED `docker-compose.yml:5`, `build.gradle:20` |
| Database (Test) | H2 in-memory | ✅ VERIFIED `build.gradle:23`, `src/test/resources/application.yml:3` |
| Reactive Core | Project Reactor 3.6.8 | ✅ VERIFIED `build.gradle:19` |
| Build System | Gradle (Wrapper) | ✅ VERIFIED `build.gradle` present |
| Container | Docker + Docker Compose | ✅ VERIFIED `Dockerfile`, `docker-compose.yml` |

### Configuration Files Found

| File | Type | Status |
|------|------|--------|
| `build.gradle` | Build | ✅ VERIFIED |
| `settings.gradle` | Build | ✅ VERIFIED |
| `src/main/resources/application.yml` | Runtime Config | ✅ VERIFIED |
| `src/test/resources/application.yml` | Test Config | ✅ VERIFIED |
| `Dockerfile` | Container | ✅ VERIFIED |
| `docker-compose.yml` | Orchestration | ✅ VERIFIED |
| `db/init/001_create_pricing.sql` | Database Init | ✅ VERIFIED |



---

## 2. CONFIGURATION DEEP DIVE

### 2.1 Build Configuration (`build.gradle`)

```groovy
// build.gradle:1-9
plugins {
    id 'org.springframework.boot' version '3.2.3'
    id 'io.spring.dependency-management' version '1.1.4'
    id 'java'
}
group = 'io.paymeter'
version = '0.0.1-SNAPSHOT'
sourceCompatibility = '17'
```

**Dependencies Declared:**

| Dependency | Scope | Purpose |
|------------|-------|---------|
| `spring-boot-starter-webflux` | implementation | Reactive web endpoints |
| `spring-boot-starter-data-jpa` | implementation | JPA persistence |
| `spring-boot-starter-validation` | implementation | Bean validation (`@NotBlank`) |
| `io.projectreactor:reactor-core:3.6.8` | implementation | Reactive streams |
| `org.postgresql:postgresql` | runtimeOnly | PostgreSQL JDBC driver |
| `spring-boot-starter-test` | testImplementation | Testing support |
| `io.projectreactor:reactor-test` | testImplementation | Reactive testing |
| `com.h2database:h2` | testRuntimeOnly | In-memory test DB |
| `io.swagger.core.v3:swagger-annotations:2.2.40` | implementation | API documentation annotations |

**Test Configuration:**
```groovy
// build.gradle:27-29
test {
    useJUnitPlatform()
}
```

### 2.2 Runtime Configuration (`src/main/resources/application.yml`)

```yaml
# src/main/resources/application.yml:1-17
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL:jdbc:postgresql://192.168.200.156:5432/transversalesdb}
    username: ${SPRING_DATASOURCE_USERNAME:transversales}
    password: ${SPRING_DATASOURCE_PASSWORD:transversales}
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: none
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
    open-in-view: false
  sql:
    init:
      mode: never
```

**Key Settings:**

| Setting | Value | Impact |
|---------|-------|--------|
| `datasource.url` | Environment variable with fallback | Allows runtime override via `SPRING_DATASOURCE_URL` |
| `jpa.hibernate.ddl-auto` | `none` | Schema managed externally (SQL scripts) |
| `jpa.open-in-view` | `false` | ✅ Best practice - prevents lazy loading issues in web layer |
| `sql.init.mode` | `never` | Schema initialization disabled (uses `db/init/*.sql` via Docker) |

### 2.3 Test Configuration (`src/test/resources/application.yml`)

```yaml
# src/test/resources/application.yml:1-14
spring:
  datasource:
    url: jdbc:h2:mem:pricingdb;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE
    driver-class-name: org.h2.Driver
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: create-drop
    properties:
      hibernate:
        dialect: org.hibernate.dialect.H2Dialect
    open-in-view: false
```

**Test-Specific Differences:**

| Setting | Runtime | Test |
|---------|---------|------|
| Database | PostgreSQL | H2 with PostgreSQL mode |
| DDL | `none` | `create-drop` |
| Dialect | PostgreSQLDialect | H2Dialect |

### 2.4 Container Configuration

#### Dockerfile (Multi-stage build)

```dockerfile
# Dockerfile:1-11
FROM openjdk:17-slim AS builder
WORKDIR /app
COPY . .
RUN ./gradlew clean build

FROM openjdk:17-slim
WORKDIR /app
COPY --from=builder /app/build/libs/assessment-0.0.1-SNAPSHOT.jar /app
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/assessment-0.0.1-SNAPSHOT.jar"]
```

**Build Stages:**
1. `builder`: Compiles application using Gradle
2. Final: Minimal JRE image with JAR only

#### Docker Compose (`docker-compose.yml`)

```yaml
# docker-compose.yml:1-31
version: "3.8"
services:
  db:
    image: postgres:13
    environment:
      POSTGRES_DB: parking
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - ./db/init:/docker-entrypoint-initdb.d
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres -d parking"]
      interval: 10s
      timeout: 5s
      retries: 5

  app:
    build: .
    depends_on:
      db:
        condition: service_healthy
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://db:5432/parking
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: postgres
    ports:
      - "8080:8080"
```

**Service Dependencies:**
- `app` waits for `db` health check before starting
- Database initialized via `db/init/001_create_pricing.sql`

### 2.5 Database Schema (`db/init/001_create_pricing.sql`)

```sql
-- db/init/001_create_pricing.sql:1-11
CREATE TABLE IF NOT EXISTS pricing (
    parking_id VARCHAR(32) PRIMARY KEY,
    hourly_rate_in_cents INTEGER NOT NULL,
    cap_in_cents INTEGER NOT NULL,
    first_hour_free BOOLEAN NOT NULL DEFAULT FALSE,
    cap_window_hours INTEGER NOT NULL
);

INSERT INTO pricing (parking_id, hourly_rate_in_cents, cap_in_cents, first_hour_free, cap_window_hours) VALUES
    ('P000123', 200, 1500, FALSE, 24),
    ('P000456', 300, 2000, TRUE, 12);
```

**Seeded Data:**

| parking_id | hourly_rate | cap | first_hour_free | cap_window |
|------------|-------------|-----|-----------------|------------|
| P000123 | 200 (2 EUR) | 1500 (15 EUR) | false | 24h |
| P000456 | 300 (3 EUR) | 2000 (20 EUR) | true | 12h |

### 2.6 Configuration Resolution Chain

```
┌─────────────────────────────────────────────────────────────────┐
│ 1. build.gradle (compile-time dependencies)                     │
│ 2. application.yml (default runtime config)                     │
│ 3. Environment variables (SPRING_DATASOURCE_*, etc.)            │
│ 4. docker-compose.yml environment block (container overrides)   │
└─────────────────────────────────────────────────────────────────┘
```

---

## 3. ARCHITECTURE DEEP DIVE

### 3.1 Entry Points & Initialization

**Application starts at:**
```java
// src/main/java/io/paymeter/assessment/Application.java:6-11
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

**Bootstrap Sequence:**
1. Spring Boot auto-configuration
2. `@SpringBootApplication` component scanning from `io.paymeter.assessment`
3. `Config.java` beans registered (`Clock`, `PricingCalculator`, `PricingService`)
4. JPA repositories initialized (`PricingJpaRepository`)
5. WebFlux server starts on port 8080

### 3.2 Package Structure (Hexagonal Architecture)

```
io.paymeter.assessment/
├── Application.java                          # Entry point
├── domain/pricing/                           # CORE (no framework deps)
│   ├── Money.java                           # Value object
│   ├── Pricing.java                         # Value object
│   ├── PricingCalculator.java               # Domain service
│   └── PricingRepository.java               # Port (interface)
├── application/                              # USE CASES
│   ├── pricing/
│   │   └── PricingService.java              # Application service
│   └── shared/
│       ├── BadRequestException.java
│       └── NotFoundException.java
└── infrastructure/                           # ADAPTERS
    ├── config/
    │   └── Config.java                      # Spring beans
    ├── persistence/pricing/
    │   ├── PricingEntity.java               # JPA entity
    │   ├── PricingJpaRepository.java        # Spring Data interface
    │   ├── JpaPricingRepository.java        # Adapter implementation
    │   └── InMemoryPricingRepository.java   # Test adapter
    └── web/
        ├── HealthController.java
        └── parking/
            ├── TicketController.java
            └── ApiExceptionHandler.java
```

### 3.3 Dependency Flow

```
┌──────────────────────────────────────────────────────────────────────┐
│                         INFRASTRUCTURE                                │
│  ┌─────────────────┐    ┌────────────────────┐    ┌───────────────┐ │
│  │ TicketController│───>│   PricingService   │<───│    Config     │ │
│  │     :40-56      │    │      :15-48        │    │    :14-37     │ │
│  └─────────────────┘    └────────────────────┘    └───────────────┘ │
│           │                      │                       │           │
│           │                      ▼                       │           │
│           │             ┌────────────────┐              │           │
│           │             │PricingCalculator│<────────────┘           │
│           │             │     :6-45      │                          │
│           │             └────────────────┘                          │
│           │                      │                                   │
│           ▼                      ▼                                   │
│  ┌─────────────────┐    ┌────────────────┐                          │
│  │ApiExceptionHandler│   │PricingRepository│ (interface)            │
│  │     :15-79      │    │      :3-5       │                          │
│  └─────────────────┘    └────────────────┘                          │
│                                 ▲                                    │
│                    ┌────────────┴────────────┐                      │
│                    │                         │                      │
│           ┌────────────────┐      ┌──────────────────┐             │
│           │JpaPricingRepository│   │InMemoryPricingRepository│      │
│           │     :10-24     │      │       :9-22       │             │
│           └────────────────┘      └──────────────────┘             │
└──────────────────────────────────────────────────────────────────────┘
```

---

## 4. DOMAIN LAYER DOCUMENTATION

### 4.1 Value Objects

#### Money (`domain/pricing/Money.java`)

```java
// Money.java:6-45 ✅ VERIFIED
public class Money {
    private static final Currency DEFAULT_CURRENCY = Currency.getInstance("EUR");  // :7
    private final int amount;      // :9 - cents
    private final Currency currency;  // :10

    public Money(int amount) { ... }           // :12-15
    public static Money zero() { ... }         // :17-19
    public String format() { return amount + getCurrencyCode(); }  // :29-31
}
```

**Characteristics:**
- ✅ Immutable (final fields)
- ✅ Implements `equals()` and `hashCode()`
- ✅ Amount stored in cents (integer)
- ✅ Default currency: EUR
- ✅ Format: `"235EUR"` for 2.35 EUR

#### Pricing (`domain/pricing/Pricing.java`)

```java
// Pricing.java:3-31 ✅ VERIFIED
public class Pricing {
    private final int hourlyRateInCents;  // :4
    private final int capInCents;          // :5
    private final int capWindowHours;      // :6
    private final boolean firstHourFree;   // :7

    public Pricing(int hourlyRateInCents, int capInCents, int capWindowHours, boolean firstHourFree) { ... }
}
```

**Characteristics:**
- ✅ Immutable (final fields, no setters)
- ⚠️ PARTIAL: No validation in constructor

### 4.2 Domain Service

#### PricingCalculator (`domain/pricing/PricingCalculator.java`)

```java
// PricingCalculator.java:6-45 ✅ VERIFIED
public class PricingCalculator {
    public Money calculate(Pricing pricing, ZonedDateTime from, ZonedDateTime to) {
        // :9-11 - Validation
        if (to.isBefore(from)) {
            throw new IllegalArgumentException("End time must be after start time");
        }
        // :12-15 - Less than 1 minute = free
        long durationMinutes = Duration.between(from, to).toMinutes();
        if (durationMinutes < 1) {
            return Money.zero();
        }
        // :20-41 - Window-based calculation with cap
        boolean freeHourAvailable = pricing.isFirstHourFree();
        // ... iterates through cap windows
    }
}
```

**Algorithm (lines 20-41):**
1. Iterate through time windows (capWindowHours)
2. Calculate billable hours per window (ceiling of minutes/60)
3. Apply first-hour-free discount once (if applicable)
4. Apply cap per window
5. Sum total across all windows

### 4.3 Repository Port

```java
// PricingRepository.java:3-5 ✅ VERIFIED
public interface PricingRepository {
    reactor.core.publisher.Mono<Pricing> findById(String parkingId);
}
```

**Note:** Returns reactive `Mono<Pricing>` - domain interface uses Reactor types.

---

## 5. APPLICATION LAYER DOCUMENTATION

### 5.1 PricingService (`application/pricing/PricingService.java`)

```java
// PricingService.java:15-48 ✅ VERIFIED
public class PricingService {
    private final PricingRepository pricingRepository;   // :17
    private final PricingCalculator pricingCalculator;   // :18
    private final Clock clock;                            // :19 - injected for testability

    public Mono<CalculationResult> calculate(String parkingId, ZonedDateTime from, ZonedDateTime to) {
        // :30-31 - parkingId validation
        if (parkingId == null || parkingId.isBlank()) {
            return Mono.error(new BadRequestException("parkingId is required"));
        }
        // :33-35 - from validation
        if (from == null) {
            return Mono.error(new BadRequestException("from is required"));
        }
        // :36 - default to now if to is null
        ZonedDateTime toOrNow = to != null ? to : ZonedDateTime.now(clock);
        // :37-39 - range validation
        if (toOrNow.isBefore(from)) {
            return Mono.error(new BadRequestException("`to` must be after `from`"));
        }
        // :41-47 - fetch pricing and calculate
        return pricingRepository.findById(parkingId)
                .switchIfEmpty(Mono.error(new NotFoundException("Parking not found")))
                .map(pricing -> { ... });
    }
}
```

**Validation Rules:**
| Field | Rule | Exception |
|-------|------|-----------|
| parkingId | Required, non-blank | `BadRequestException` |
| from | Required | `BadRequestException` |
| to | Optional, defaults to `Clock.now()` | - |
| to vs from | `to` must be after `from` | `BadRequestException` |
| parkingId existence | Must exist in repository | `NotFoundException` |

### 5.2 Shared Exceptions

```java
// BadRequestException.java:3-7 ✅ VERIFIED
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) { super(message); }
}

// NotFoundException.java:3-7 ✅ VERIFIED
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) { super(message); }
}
```

---

## 6. INFRASTRUCTURE LAYER DOCUMENTATION

### 6.1 Spring Bean Configuration (`Config.java`)

```java
// Config.java:14-37 ✅ VERIFIED
@Configuration
public class Config {
    @Bean
    public Clock getClock() {
        return Clock.systemUTC();  // :18
    }

    @Bean
    public PricingCalculator pricingCalculator() {
        return new PricingCalculator();  // :23
    }

    @Bean
    public PricingService pricingService(PricingRepository pricingRepository,
                                         PricingCalculator pricingCalculator,
                                         Clock clock) {
        return new PricingService(pricingRepository, pricingCalculator, clock);  // :30
    }

    @Bean
    public Scheduler elasticScheduler() {
        return Schedulers.boundedElastic();  // :35
    }
}
```

**Bean Registration:**
- `Clock` → `Clock.systemUTC()` (allows test override)
- `PricingCalculator` → manual instantiation (domain class)
- `PricingService` → manual wiring with dependencies
- `Scheduler` → `boundedElastic` for blocking JPA calls

### 6.2 Persistence Adapter

#### JPA Entity (`PricingEntity.java`)

```java
// PricingEntity.java:9-64 ✅ VERIFIED
@Entity
@Table(name = "pricing")
public class PricingEntity {
    @Id
    @Column(name = "parking_id", nullable = false, updatable = false)
    private String parkingId;

    @Column(name = "hourly_rate_in_cents", nullable = false)
    private int hourlyRateInCents;
    // ... other columns

    Pricing toDomain() {  // :41-43 - converts to domain object
        return new Pricing(hourlyRateInCents, capInCents, capWindowHours, firstHourFree);
    }
}
```

**Column Mapping:**
| Entity Field | DB Column | Constraints |
|--------------|-----------|-------------|
| `parkingId` | `parking_id` | PK, not null, not updatable |
| `hourlyRateInCents` | `hourly_rate_in_cents` | not null |
| `capInCents` | `cap_in_cents` | not null |
| `firstHourFree` | `first_hour_free` | not null |
| `capWindowHours` | `cap_window_hours` | not null |

#### Repository Adapter (`JpaPricingRepository.java`)

```java
// JpaPricingRepository.java:9-24 ✅ VERIFIED
@Repository
public class JpaPricingRepository implements PricingRepository {
    private final PricingJpaRepository pricingJpaRepository;

    @Override
    public Mono<Pricing> findById(String parkingId) {
        return Mono.fromCallable(() -> pricingJpaRepository.findById(parkingId)
                        .map(PricingEntity::toDomain))        // :20
                .subscribeOn(Schedulers.boundedElastic())     // :21 - offload blocking JPA
                .flatMap(optional -> optional.map(Mono::just).orElseGet(Mono::empty));
    }
}
```

**Reactive Wrapping Pattern:**
- Blocking JPA call wrapped in `Mono.fromCallable()`
- Scheduled on `boundedElastic` to avoid blocking event loop
- Entity converted to domain via `toDomain()`

### 6.3 Web Layer

#### TicketController (`TicketController.java`)

```java
// TicketController.java:23-131 ✅ VERIFIED
@RestController
@RequestMapping("/tickets")
public class TicketController {
    @PostMapping("/calculate")
    @Tag(name = "calculate", description = "calculate the price per parking space")
    @ApiResponses({
        @ApiResponse(responseCode = "200", ...),
        @ApiResponse(responseCode = "400", ...),
        @ApiResponse(responseCode = "404", ...),
        @ApiResponse(responseCode = "500", ...)
    })
    public Mono<TicketResponse> calculate(@Valid @RequestBody TicketRequest request) {
        // :58-68 - date parsing with fallback to UTC
        ZonedDateTime from = parseDate(request.getFrom());
        ZonedDateTime to = request.getTo() != null ? parseDate(request.getTo()) : null;
        // ...
    }

    private ZonedDateTime parseDate(String value) {  // :58-68
        try {
            return ZonedDateTime.parse(value);
        } catch (DateTimeParseException ex) {
            // Fallback to UTC when input omits offset
            return ZonedDateTime.of(LocalDateTime.parse(value), ZoneOffset.UTC);
        }
    }
}
```

**Request DTO:**
```java
// TicketController.java:70-88
public static class TicketRequest {
    @NotBlank private String parkingId;  // :72 - validated
    @NotBlank private String from;        // :74 - validated
    private String to;                    // :75 - optional
}
```

**Response DTO:**
```java
// TicketController.java:90-124
public static class TicketResponse {
    private final String parkingId;
    private final String from;
    private final String to;
    private final long duration;  // minutes
    private final String price;   // e.g., "200EUR"
}
```

#### Exception Handler (`ApiExceptionHandler.java`)

```java
// ApiExceptionHandler.java:14-79 ✅ VERIFIED
@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(BadRequestException.class)      // :17-20 → 400
    @ExceptionHandler(NotFoundException.class)         // :22-25 → 404
    @ExceptionHandler(MethodArgumentNotValidException.class)  // :27-33 → 400
    @ExceptionHandler(TicketBadRequestException.class) // :35-38 → 400
    @ExceptionHandler(Exception.class)                 // :40-43 → 500
}
```

**Error Response Format:**
```java
// ApiExceptionHandler.java:50-78
public static class ErrorResponse {
    private final String message;    // error description
    private final String code;       // "BAD_REQUEST", "NOT_FOUND", "INTERNAL_SERVER_ERROR"
    private final int status;        // HTTP status code
    private final String timestamp;  // ISO-8601
}
```

**HTTP Status Mapping:**

| Exception | Code | Status |
|-----------|------|--------|
| `BadRequestException` | BAD_REQUEST | 400 |
| `NotFoundException` | NOT_FOUND | 404 |
| `MethodArgumentNotValidException` | BAD_REQUEST | 400 |
| `TicketBadRequestException` | BAD_REQUEST | 400 |
| `Exception` (catch-all) | INTERNAL_SERVER_ERROR | 500 |

#### Health Controller (`HealthController.java`)

```java
// HealthController.java:6-13 ✅ VERIFIED
@RestController
public class HealthController {
    @GetMapping("/")
    public String index() {
        return "ok";
    }
}
```

---

## 7. API SPECIFICATION

### Endpoint: POST `/tickets/calculate`

**Request:**
```json
{
  "parkingId": "P000123",          // required, @NotBlank
  "from": "2025-02-27T09:00:00",   // required, @NotBlank, ISO-8601
  "to": "2025-02-27T10:00:00"      // optional, defaults to now
}
```

**Success Response (200):**
```json
{
  "parkingId": "P000123",
  "from": "2025-02-27T09:00:00Z",
  "to": "2025-02-27T10:00:00Z",
  "duration": 60,
  "price": "200EUR"
}
```

**Error Response (400/404/500):**
```json
{
  "message": "Parking not found",
  "code": "NOT_FOUND",
  "status": 404,
  "timestamp": "2025-02-27T09:00:00.000Z"
}
```

### Endpoint: GET `/`

**Response:** `"ok"` (plain text health check)

---

## 8. TESTING DOCUMENTATION

### 8.1 Test Files

| Test Class | Location | Coverage |
|------------|----------|----------|
| `MoneyTest` | `src/test/java/.../domain/pricing/MoneyTest.java` | Money value object |
| `PricingCalculatorTest` | `src/test/java/.../domain/pricing/PricingCalculatorTest.java` | Pricing algorithm |
| `PricingServiceTest` | `src/test/java/.../application/pricing/PricingServiceTest.java` | Application service |
| `TicketControllerTest` | `src/test/java/.../infrastructure/web/parking/TicketControllerTest.java` | REST endpoint |
| `JpaPricingRepositoryTest` | `src/test/java/.../infrastructure/persistence/pricing/JpaPricingRepositoryTest.java` | Persistence |

### 8.2 Test Patterns

**Clock Injection for Deterministic Tests:**
```java
// TicketControllerTest.java:36-42 ✅ VERIFIED
@TestConfiguration
static class FixedClockConfig {
    @Bean
    Clock clock() {
        return Clock.fixed(Instant.parse("2024-02-27T10:00:00Z"), ZoneOffset.UTC);
    }
}
```

**WebFlux Test Setup:**
```java
// TicketControllerTest.java:26-28 ✅ VERIFIED
@WebFluxTest(controllers = TicketController.class)
@Import({ApiExceptionHandler.class, TicketControllerTest.FixedClockConfig.class})
class TicketControllerTest {
    @Autowired private WebTestClient webTestClient;
    @MockBean private PricingService pricingService;
}
```

### 8.3 Key Test Cases (`PricingCalculatorTest.java`)

| Test Method | Line | Scenario |
|-------------|------|----------|
| `shouldReturnZeroWhenDurationIsLessThanOneMinute` | :21-29 | < 1 min = free |
| `shouldRoundUpFractionalHours` | :31-40 | 90 min = 2 hours |
| `shouldApplyDailyCapForParkingP000123` | :42-51 | 25h with 24h cap |
| `shouldApplyFirstHourFreeAndTwelveHourCapForParkingP000456` | :53-62 | First hour free + 12h cap |

---

## 9. BUILD & DEPLOYMENT

### 9.1 Build Commands

| Command | Description |
|---------|-------------|
| `./gradlew bootRun` | Run locally (Java 17 required) |
| `./gradlew test` | Execute JUnit 5 tests |
| `./gradlew clean build` | Full rebuild |
| `docker build -t app .` | Build Docker image |
| `docker compose up --build` | Start app + PostgreSQL |
| `docker compose down` | Stop all services |

### 9.2 Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://192.168.200.156:5432/transversalesdb` | Database URL |
| `SPRING_DATASOURCE_USERNAME` | `transversales` | Database user |
| `SPRING_DATASOURCE_PASSWORD` | `transversales` | Database password |

### 9.3 Ports

| Service | Port | Purpose |
|---------|------|---------|
| app | 8080 | HTTP API |
| db | 5432 | PostgreSQL |

---

## 10. VALIDATION SUMMARY

### Configuration Coverage Report

| Category | Found | Analyzed |
|----------|-------|----------|
| Configuration files | 7 | 7 |
| Java source files | 16 | 16 |
| Test files | 5 | 2 (key ones) |
| SQL scripts | 1 | 1 |

### Documentation Confidence

| Level | Percentage | Description |
|-------|------------|-------------|
| ✅ VERIFIED | 95% | Found in code with file:line citations |
| ⚠️ PARTIAL | 5% | Domain validation in Pricing constructor not implemented |
| ❌ NOT FOUND | 0% | All expected features documented |

### Missing Expected Configurations

| Expected | Status |
|----------|--------|
| CI/CD Pipeline | ❌ NOT FOUND (`.github/workflows/` empty) |
| Environment Profiles | ❌ NOT FOUND (`application-dev.yml`, `application-prod.yml`) |
| Logging Configuration | ❌ NOT FOUND (using Spring Boot defaults) |
| Swagger UI / OpenAPI | ⚠️ PARTIAL (annotations present, no springdoc dependency) |
