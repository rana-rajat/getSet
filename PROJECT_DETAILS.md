# GetSet Backend — Developer Onboarding Guide

> **Purpose of this document**: Help a new developer understand the codebase quickly by walking through the project step-by-step — from how the app boots, to how a request travels through every layer, to how each feature is structured.

---

## Table of Contents

1. [Project at a Glance](#1-project-at-a-glance)
2. [Step 1 — Start Here: The Entry Point](#step-1--start-here-the-entry-point)
3. [Step 2 — Configuration Layer](#step-2--configuration-layer)
4. [Step 3 — Security & JWT](#step-3--security--jwt)
5. [Step 4 — How a Request Flows Through the App](#step-4--how-a-request-flows-through-the-app)
6. [Step 5 — Authentication Feature (End-to-End Walkthrough)](#step-5--authentication-feature-end-to-end-walkthrough)
7. [Step 6 — Data Layer: Documents & Repositories](#step-6--data-layer-documents--repositories)
8. [Step 7 — Feature Packages (Explore in This Order)](#step-7--feature-packages-explore-in-this-order)
9. [Step 8 — Cross-Cutting Concerns](#step-8--cross-cutting-concerns)
10. [Step 9 — Tests](#step-9--tests)
11. [Step 10 — Configuration Files](#step-10--configuration-files)
12. [Common Patterns](#common-patterns)
13. [Troubleshooting](#troubleshooting)

---

## 1. Project at a Glance

GetSet is a **rental property platform backend** — think Airbnb/NoBroker. It lets:

- **OWNERs** list rental properties, respond to enquiries, and message renters.
- **RENTERs** search and filter properties (including geo-search), enquire, favourite, and message owners.
- **ADMINs** access analytics and admin endpoints.

**Key numbers:**
- **87** Java source files
- **35+** REST endpoints
- **6** MongoDB collections
- **5** Spring Boot feature packages
- **20** unit tests

---

## Step 1 — Start Here: The Entry Point

**File:** `src/main/java/com/getset/GetSetApplication.java`

```java
@SpringBootApplication
@EnableMongoAuditing   // ← enables @CreatedDate / @LastModifiedDate on documents
public class GetSetApplication {
    public static void main(String[] args) {
        SpringApplication.run(GetSetApplication.class, args);
    }
}
```

**What it does:**
- Bootstraps the entire Spring context.
- `@EnableMongoAuditing` is required for `createdAt`/`updatedAt` fields to be auto-populated on save.

**Next file to open:** `src/main/resources/application.yml`

---

## Step 2 — Configuration Layer

**Package:** `src/main/java/com/getset/config/`

Read these files in order:

### 2a. `application.yml` (resources/)

The single source of truth for all configuration. Key sections:

```yaml
spring:
  data:
    mongodb.uri: ${MONGODB_URI}          # MongoDB connection
    elasticsearch.uris: ${ELASTICSEARCH_HOST}
    redis.host: ${REDIS_HOST}

  security:
    jwt:
      secret-key: ${JWT_SECRET}          # MUST be set — app won't start otherwise
      expiration: 86400000               # 24h access token
      refresh-expiration: 604800000      # 7 day refresh token

server:
  port: 8080
  servlet.context-path: /api/v1         # All endpoints are prefixed /api/v1
```

> **Why no default for JWT_SECRET?** A missing fallback means the app fails on startup rather than running with a publicly-known key. This is intentional.

### 2b. `SecurityConfig.java`

**The most important config file.** Defines:
- Which endpoints are public vs. protected.
- The JWT filter placement in the chain.
- Session policy (stateless — no server sessions).

```
Public endpoints:
  POST /auth/register
  POST /auth/login
  POST /auth/refresh
  GET  /properties (read-only search)
  GET  /properties/{id}
  GET  /properties/nearby
  /swagger-ui.html, /api-docs
  /actuator/health

All other endpoints → require a valid JWT Bearer token.
```

### 2c. `CorsConfig.java`

Reads allowed origins from the `APP_CORS_ALLOWED_ORIGINS` environment variable (comma-separated). Defaults to `localhost:3000` and `localhost:5173` for local dev.

### 2d. `OpenApiConfig.java`

Sets up the Swagger UI with Bearer token support. This is what powers the interactive docs at `/api/v1/swagger-ui.html`.

### 2e. Other config files
- `MongoConfig.java` — MongoTemplate, custom converters
- `RedisConfig.java` — Redis connection factory, template setup
- `AppConfig.java` — `PasswordEncoder` (BCrypt), `ModelMapper` beans

---

## Step 3 — Security & JWT

**Package:** `src/main/java/com/getset/security/`

### 3a. `JwtService.java`

The core JWT utility. Understand this before looking at anything auth-related.

| Method | What it does |
|---|---|
| `generateToken(UserDetails)` | Creates a short-lived access token (24h) |
| `generateRefreshToken(UserDetails)` | Creates a long-lived refresh token (7 days) |
| `extractUsername(token)` | Pulls the email (subject) from a token |
| `isTokenValid(token, UserDetails)` | Verifies signature + expiry + username match |

Both tokens are standard HS256 JWTs signed with the `JWT_SECRET` key.

### 3b. `JwtAuthenticationFilter.java`

A `OncePerRequestFilter` that runs on **every incoming HTTP request**:

```
1. Read the "Authorization: Bearer <token>" header
2. Extract the token
3. Call JwtService.extractUsername(token) → gets user email
4. Load the user from MongoDB via UserDetailsService
5. Call JwtService.isTokenValid(token, user)
6. If valid → set SecurityContextHolder authentication
7. If invalid or missing → do nothing (request proceeds unauthenticated)
```

The filter does **not** reject requests — `SecurityConfig` decides what's allowed through.

---

## Step 4 — How a Request Flows Through the App

Every HTTP request follows this exact path:

```
HTTP Request (from browser / Postman)
        │
        ▼
[ JwtAuthenticationFilter ]  ← validates token, populates SecurityContext
        │
        ▼
[ SecurityConfig filter chain ]  ← checks if endpoint requires auth/role
        │
        ▼
[ @RestController ]  ← parses request, calls @Valid on body
        │
        ▼
[ @Service / @ServiceImpl ]  ← business logic, ownership checks, transactions
        │
        ▼
[ @Repository ]  ← MongoDB query via Spring Data or custom MongoTemplate
        │
        ▼
[ MongoDB ]  ← actual database read/write
        │
        ▼
[ Service maps Document → DTO ]  ← never expose the raw Document to the client
        │
        ▼
[ Controller returns ResponseEntity<DTO> ]
        │
        ▼
HTTP Response (JSON)
```

**Key rule:** The Controller never calls the Repository directly. Always goes through the Service.

---

## Step 5 — Authentication Feature (End-to-End Walkthrough)

The best way to understand the architecture is to trace one complete feature. Start with **auth** — it's the simplest.

**Package:** `src/main/java/com/getset/auth/`

### Files to read in order:

#### 1. `dto/RegisterRequest.java`
The incoming request body. Note Lombok `@Data`/`@Builder` and Jakarta `@NotBlank`/`@Email` validation annotations.

#### 2. `dto/AuthResponse.java`
The outgoing response. Contains `accessToken`, `refreshToken`, `id`, `name`, `email`, `role`.

#### 3. `AuthController.java`
Thin controller — no business logic. Four endpoints:

| Endpoint | Method called |
|---|---|
| `POST /auth/register` | `authService.register(request)` → 201 Created |
| `POST /auth/login` | `authService.login(request)` → 200 OK |
| `POST /auth/refresh` | `authService.refreshToken(request)` → 200 OK |
| `GET /auth/me` | `authService.getCurrentUser(email)` → 200 OK |

#### 4. `AuthService.java`
The business logic. Key things to notice:

- **`register()`** — checks email uniqueness → BCrypt-hashes password → saves to MongoDB → generates **both** access and refresh tokens → returns `AuthResponse`.
- **`login()`** — delegates credential check to Spring's `AuthenticationManager` → finds user → generates access token → returns `AuthResponse`.
- **`refreshToken()`** — extracts username from refresh token → validates against DB user → issues new access token.
- **Resilience4j** annotations: `@Retry` and `@CircuitBreaker` wrap the DB calls. If MongoDB is temporarily unreachable, the circuit opens and the fallback method runs instead of crashing.
- **Rate Limiting**: `@RateLimiter(name = "authService")` limits login/register to **10 requests per minute** per instance to prevent brute-force attacks.

---

## Step 6 — Data Layer: Documents & Repositories

**Package:** `src/main/java/com/getset/user/`

### `UserDocument.java`
The MongoDB document model annotated with `@Document(collection = "users")`.

```java
@Document(collection = "users")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UserDocument implements UserDetails {
    @Id private String id;
    private String name;
    @Indexed(unique = true) private String email;
    private String password;
    private Role role;
    private String phone;
    @CreatedDate private Instant createdAt;
    @LastModifiedDate private Instant updatedAt;

    // implements UserDetails — used by Spring Security
    @Override public Collection<GrantedAuthority> getAuthorities() { ... }
    @Override public String getUsername() { return email; }
}
```

**Important:** `UserDocument` implements `UserDetails` directly. This means it can be passed straight to Spring Security's `AuthenticationManager`, and is used as the `UserDetails` in the JWT filter.

### `UserRepository.java`
Extends `MongoRepository<UserDocument, String>`. Methods:
```java
Optional<UserDocument> findByEmail(String email);
boolean existsByEmail(String email);
```

### `Role.java` (enum)
```java
public enum Role { RENTER, OWNER, ADMIN }
```

> Note the roles are **RENTER, OWNER, ADMIN** — not USER. If you see `Role.USER` anywhere it's a bug.

---

## Step 7 — Feature Packages (Explore in This Order)

Each feature package follows the **same consistent pattern**:

```
{feature}/
├── {Feature}Document.java       ← MongoDB document model
├── {Feature}Repository.java     ← Spring Data Mongo interface
├── {Feature}Service.java        ← Service interface
├── {Feature}ServiceImpl.java    ← Business logic implementation
├── {Feature}Controller.java     ← REST endpoints
└── dto/                         ← Request/Response DTOs
```

Explore them in this order (simplest → most complex):

---

### 🏠 Property (`com.getset.property`)

**What it does:** Core listing management — CRUD, search, geo-search.

**Key files:**
- `PropertyDocument.java` — has a `location` field of type `GeoJsonPoint` for geo-spatial queries.
- `PropertyRepositoryImpl.java` — custom `MongoTemplate` queries for the advanced search (city filter, price range, bedroom count, furnished flag, pagination). This is where the non-trivial MongoDB aggregation lives.
- `PropertyServiceImpl.java` — notice the `ForbiddenException` thrown when an OWNER tries to update another owner's property (`doc.getOwnerId().equals(ownerId)` check).
- `PropertyController.java` — exposes paginated `PageResponse<PropertyResponse>` from the search endpoint (not a raw List).

**Key concept — Pagination:**
```java
// Controller receives page/size as query params
@GetMapping
public ResponseEntity<PageResponse<PropertyResponse>> searchProperties(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size, ...) { ... }

// Service returns PageResponse wrapping the results
return PageResponse.of(results, total, page, size);
```

---

### 📋 Enquiry (`com.getset.enquiry`)

**What it does:** Renters express interest in a property → owner accepts or rejects.

**Key files:**
- `EnquiryDocument.java` — stores `propertyId`, `renterId`, `ownerId`, and `EnquiryStatus`.
- `EnquiryStatus.java` — `PENDING`, `ACCEPTED`, `REJECTED`.
- `EnquiryServiceImpl.java` — when an enquiry is created or status changes, it calls `NotificationService` to send email + in-app notification. This is a **multi-document operation** guarded with `@Transactional`.

**Flow:**
```
RENTER POSTs /enquiries
  → saves EnquiryDocument
  → creates NotificationDocument for OWNER
  → sends email to OWNER
  → returns EnquiryResponse

OWNER PUTs /enquiries/{id} (accept/reject)
  → updates EnquiryDocument.status
  → creates NotificationDocument for RENTER
  → sends email to RENTER
```

---

### ⭐ Favorite (`com.getset.favorite`)

**What it does:** RENTERs save properties to a wishlist with optional personal notes.

**Key files:**
- `FavoriteDocument.java` — just `renterId`, `propertyId`, `notes`.
- `FavoriteServiceImpl.java` — straightforward CRUD. Notice the composite uniqueness check: a renter can't favorite the same property twice.

---

### 💬 Message (`com.getset.message`)

**What it does:** Direct messaging between owners and renters, grouped by conversation threads.

**Key files:**
- `MessageDocument.java` — has a `threadId` field (a string combining `senderId + recipientId + propertyId` sorted, so the thread ID is the same regardless of who sends first).
- `MessageServiceImpl.java` — `@Transactional` on `sendMessage()` because it saves the message AND creates a notification in a single operation.

---

### 🔔 Notification (`com.getset.notification`)

**What it does:** In-app notification store + email dispatch.

**Key files:**
- `NotificationDocument.java` — tracks `read`, `emailSent`, `emailSentError`.
- `NotificationServiceImpl.java` — two responsibilities:
  1. **Persist** the notification document to MongoDB.
  2. **Send email** via `JavaMailSender`. Email failures are caught and logged (not re-thrown) — a failed email does **not** roll back the notification save.

---

### 🔍 Search (`com.getset.search`)

**What it does:** Elasticsearch-backed full-text search with autocomplete and trending.

**Key files:**
- Elasticsearch sync (scheduled) — indexes property documents from MongoDB into Elasticsearch.
- Redis caching — 5-level cache strategy with TTL management for popular searches.
- Admin endpoints for sync, trending analytics, and statistics.

> **Note**: The Elasticsearch and Redis integrations add infrastructure dependencies. If you're running locally without them, properties will fall back to MongoDB-based search.

---

## Step 8 — Cross-Cutting Concerns

These packages handle things that span all features:

### `com.getset.exception`

| Class | HTTP Status |
|---|---|
| `GetSetException` | Base class (no status) |
| `NotFoundException` | 404 Not Found |
| `ForbiddenException` | 403 Forbidden |
| `UnauthorizedException` | 401 Unauthorized |

### `com.getset.exception.GlobalExceptionHandler`

A `@RestControllerAdvice` that catches all exceptions and returns a consistent JSON error response:

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Property not found",
  "timestamp": "2026-02-22T13:45:00Z"
}
```

Never let controller methods return error strings manually — always throw the right exception and let this handler format the response.

### `com.getset.common.PageResponse<T>`

A generic paginated response wrapper:

```java
public class PageResponse<T> {
    List<T> content;
    int page;
    int size;
    long totalElements;
    int totalPages;
    boolean last;
}
```

Use this on **any endpoint that can return multiple results**. Never return a raw `List<T>` to the client.

### `com.getset.util.AuditLogger`

A dedicated structured audit logger. Called in `AuthService` to record every login attempt, registration, and authorization failure with a structured log line. Feeds into the Logstash JSON log output. Don't write raw `log.info("User logged in")` for security events — go through `AuditLogger`.

---

## Step 9 — Tests

**Package:** `src/test/java/com/getset/`

| Test File | Type | What It Covers |
|---|---|---|
| `security/JwtServiceTest.java` | Unit | Token generation, expiry, validation (5 tests) |
| `auth/AuthServiceTest.java` | Unit | register, login, refresh, getCurrentUser, error cases (8 tests) |
| `auth/AuthControllerTest.java` | MockMvc | HTTP layer — status codes, request/response JSON (2 tests) |
| `property/PropertyServiceImplTest.java` | Unit | CRUD, ownership guard, not-found (5 tests) |

### Running Tests

```bash
# Run all tests
mvn clean test

# Run a specific test class
mvn test -Dtest=AuthServiceTest

# Run with a specific profile
mvn test -Dspring.profiles.active=dev
```

### Test Patterns Used

**Unit tests** (`AuthServiceTest`, `PropertyServiceImplTest`, `JwtServiceTest`) use:
- `@ExtendWith(MockitoExtension.class)` — pure Mockito, no Spring context
- `@Mock` for dependencies, `@InjectMocks` for the class under test
- `when(...).thenReturn(...)` to stub behavior
- `assertThrows(...)` to verify exception paths

**Controller tests** (`AuthControllerTest`) use:
- `MockMvcBuilders.standaloneSetup(controller)` — lightweight, no full Spring context
- `mockMvc.perform(post(...).content(...)).andExpect(status().isCreated())`

---

## Step 10 — Configuration Files

| File | Location | Purpose |
|---|---|---|
| `application.yml` | `src/main/resources/` | Base config — all environments |
| `application-dev.yml` | `src/main/resources/` | Dev overrides (DEBUG logging, relaxed settings) |
| `application-prod.yml` | `src/main/resources/` | Prod overrides (WARN logging, strict settings) |
| `logback-spring.xml` | `src/main/resources/` | Log format — JSON for prod, pretty for dev |
| `.env.example` | project root | Template for all required environment variables |
| `docker-compose.yml` | project root | Starts app + MongoDB + Redis |
| `Dockerfile` | `getset-backend/` | Multi-stage build → minimal JRE image |
| `pom.xml` | `getset-backend/` | All Maven dependencies and plugins |

---

## Common Patterns

### Pattern 1: Service → Repository → Mapping

Every service method follows this shape:

```java
public PropertyResponse getProperty(String id) {
    // 1. Fetch or throw
    PropertyDocument doc = propertyRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("Property not found"));

    // 2. Map Document → DTO (never expose the Document itself)
    return PropertyMapper.toResponse(doc);
}
```

### Pattern 2: Ownership Guard

Any mutating operation checks the caller owns the resource:

```java
if (!doc.getOwnerId().equals(currentUserId)) {
    throw new ForbiddenException("Not authorized to modify this resource");
}
```

### Pattern 3: Getting the Current User in a Service

The authenticated user's email is available from Spring Security:

```java
// In a controller:
public ResponseEntity<?> someEndpoint(Authentication authentication) {
    String email = authentication.getName();
    ...
}
```

### Pattern 4: Resilience4j on Service Methods

DB-calling service methods are wrapped with:

```java
@Retry(name = "userRepository")
@CircuitBreaker(name = "userRepository", fallbackMethod = "myFallback")
public SomeResponse myMethod(...) { ... }

public SomeResponse myFallback(..., Exception ex) {
    throw new RuntimeException("Service temporarily unavailable");
}
```

### Pattern 5: Returning Paginated Results

```java
// Controller
@GetMapping
public ResponseEntity<PageResponse<PropertyResponse>> list(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
    return ResponseEntity.ok(propertyService.list(page, size));
}

// Service
public PageResponse<PropertyResponse> list(int page, int size) {
    Page<PropertyDocument> result = repository.findAll(PageRequest.of(page, size));
    List<PropertyResponse> content = result.getContent().stream()
        .map(PropertyMapper::toResponse)
        .toList();
    return PageResponse.of(content, result.getTotalElements(), page, size);
}
```

---

## Troubleshooting

### App fails to start — `JWT_SECRET`
```
Caused by: java.lang.IllegalArgumentException: Could not resolve placeholder 'JWT_SECRET'
```
**Fix:** Set `JWT_SECRET` as an environment variable (min 32 characters, Base64-encoded recommended):
```powershell
$env:JWT_SECRET = "your-super-secret-key-minimum-32-chars"
```

### App fails to start — MongoDB not running
```
com.mongodb.MongoTimeoutException: Timed out after 30000ms
```
**Fix:** Start MongoDB via Docker:
```bash
docker run -d -p 27017:27017 --name mongo mongo:7
```

### CORS error from frontend
**Fix:** Set the env var:
```powershell
$env:APP_CORS_ALLOWED_ORIGINS = "http://localhost:3000,http://localhost:5173"
```

### Token expired (401 Unauthorized)
Use the `/auth/refresh` endpoint with your `refreshToken` from login to get a new access token (valid 7 days). If the refresh token is also expired, log in again.

### Port 8080 in use
```powershell
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

### Build failure — Java version
```bash
java -version  # Must be 21+
```

---

## Where Each Type of Change Goes

| Change type | File(s) to edit |
|---|---|
| New REST endpoint | Add to `{Feature}Controller.java` |
| New business rule | Edit `{Feature}ServiceImpl.java` |
| New MongoDB field | Edit `{Feature}Document.java` |
| New DTO field | Edit relevant DTO in `dto/` |
| Change which endpoints are public | Edit `SecurityConfig.java` |
| Change CORS origins | Change `APP_CORS_ALLOWED_ORIGINS` env var |
| Change log verbosity | Edit `application-dev.yml` or `application-prod.yml` |
| Add a new error type | Create class in `exception/`, extend `GetSetException` |
| Add a new config property | Add to `application.yml` with `${ENV_VAR:default}` pattern |

---

*Happy exploring! Start at `GetSetApplication.java` and follow the path. 🏠*
