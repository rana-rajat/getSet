# 🚀 GetSet Backend — Developer Walkthrough

> **For new developers joining the project.** This guide walks you through the entire backend from the ground up — what to read first, what to run first, and how every class connects to every other class.

---

## 📌 Before You Start

### Prerequisites
| Tool | Version | Purpose |
|---|---|---|
| Java | 21 | Runtime |
| Maven | 3.8+ | Build tool |
| Docker Desktop | Latest | Run all infrastructure |
| IntelliJ IDEA / VS Code | Latest | IDE |
| Git | Latest | Version control |

### Mark `src/main/java` as Source Root (VS Code)
Press `Ctrl+Shift+P` → **Java: Clean Java Language Server Workspace** → Reload. This prevents the "package does not match" IDE warning.

---

## 🗺️ Big Picture — What Is GetSet?

GetSet is a **rental property platform** where:
- **Owners** list properties and manage enquiries
- **Renters** search, enquire, favorite, and message about properties

The backend is split into **7 independent microservices** + 1 shared library + 1 API gateway:

```
Client (Browser/App)
       │
       ▼
┌─────────────────┐   Port 8090
│   API Gateway   │ ◄─── All external traffic goes here
└────────┬────────┘
         │  Routes to...
    ┌────┴─────────────────────────────────┐
    │                                      │
    ▼                                      ▼
user-service (8081)          property-service (8082)
enquiry-service (8083)       favorite-service (8084)
message-service (8085)       notification-service (8086)

Infrastructure:
  MongoDB  │  Kafka  │  Zookeeper  │  Zipkin
  (Docker Compose spins all of these up)
```

### Communication Between Services
- **Synchronous** (HTTP): Services call each other using Spring Cloud **Feign Clients**
- **Asynchronous** (Events): Enquiry and Message services publish to **Kafka** → Notification service consumes

---

## 🪜 Recommended Exploration Order

Follow this exact order. Each step builds on the previous one.

```
Step 1 → getset-common     (shared DTOs, exceptions, events)
Step 2 → user-service      (auth foundation — everything depends on JWT)
Step 3 → api-gateway       (understand how traffic flows in)
Step 4 → property-service  (core domain — properties are central)
Step 5 → enquiry-service   (introduces Feign Client + Kafka producer)
Step 6 → favorite-service  (simpler service — good pattern reference)
Step 7 → message-service   (Feign Client + Kafka producer)
Step 8 → notification-service (Kafka consumer — the final piece)
```

---

## Step 1 — `getset-common` (Shared Library)

**Location:** `getset-common/src/main/java/com/getset/common/`

> Start here — every other service depends on this. It has no business logic; it's just shared types.

### What's Inside

| Class | Package | Purpose |
|---|---|---|
| `GetSetException.java` | `exception/` | Base class all custom exceptions extend |
| `NotFoundException.java` | `exception/` | Thrown when a resource isn't found (HTTP 404) |
| `ForbiddenException.java` | `exception/` | Thrown when user doesn't own the resource (HTTP 403) |
| `UnauthorizedException.java` | `exception/` | Thrown for invalid/expired JWT (HTTP 401) |
| `ApiError.java` | `dto/` | Standard error response body: `{status, error, message, timestamp}` |
| `PageResponse<T>.java` | `dto/` | Generic paginated response: `{content, pageNumber, pageSize, totalElements, hasNext}` |
| `PropertySummaryDto.java` | `dto/` | Lightweight property data passed between services (used by favorite + enquiry) |
| `UserSummaryDto.java` | `dto/` | Lightweight user data passed between services |
| `NotificationEvent.java` | `event/` | Kafka event DTO. Has inner enum `EventType`: `ENQUIRY_RECEIVED`, `ENQUIRY_ACCEPTED`, `ENQUIRY_REJECTED`, `MESSAGE_RECEIVED` |

### Key Concept
`NotificationEvent` is the contract between producers (enquiry, message services) and consumer (notification service). Both sides import this from `getset-common`. If you change this DTO, **both ends must be updated**.

---

## Step 2 — `user-service` (Authentication + Users)

**Location:** `user-service/src/main/java/com/getset/user/`  
**Port:** `8081`  
**Database:** MongoDB collection `users`

> This service owns all authentication. Every other service validates JWTs by using the same `JwtService` logic (copied into each service — see note in Step 3).

### Read In This Order

#### `domain/`
| Class | What to Look For |
|---|---|
| `Role.java` | Simple enum: `RENTER`, `OWNER`, `ADMIN`. Used in JWT claims and security checks |
| `UserDocument.java` | MongoDB `@Document("users")`. Implements Spring Security's `UserDetails`. Fields: id, name, email, password (BCrypt), role, phone, createdAt |
| `UserRepository.java` | `MongoRepository`. Key methods: `findByEmail(String)`, `existsByEmail(String)` |

#### `security/`
| Class | What to Look For |
|---|---|
| `JwtService.java` | Core JWT utility. `generateToken()` puts `role` in claims. `extractUsername()` parses email from token. `isTokenValid()` checks expiry + username match |
| `JwtAuthenticationFilter.java` | `OncePerRequestFilter`. Extracts `Authorization: Bearer <token>` header → calls `JwtService.isTokenValid()` → sets `SecurityContextHolder` so downstream code can call `principal.getName()` |

#### `config/`
| Class | What to Look For |
|---|---|
| `SecurityConfig.java` | Defines which endpoints are public (`/api/v1/auth/**`) vs protected. Uses the `JwtAuthenticationFilter` |

#### `api/`
| Class | What to Look For |
|---|---|
| `AuthController.java` | `POST /auth/register` → `POST /auth/login` → returns `accessToken` + `refreshToken`. `GET /auth/me` returns current user profile |
| `InternalUserController.java` | `GET /internal/users/{id}` — called only by other services via Feign, **not exposed to clients through the gateway**. Returns `UserSummaryDto` |

#### `config/`
| Class | What to Look For |
|---|---|
| `AuthService.java` | Business logic. `register()`: validates email uniqueness → BCrypt hashes password → saves `UserDocument`. `login()`: `AuthenticationManager.authenticate()` → `JwtService.generateToken()` |

#### `dto/`
| Class | Purpose |
|---|---|
| `RegisterRequest.java` | name, email, password, role, phone |
| `LoginRequest.java` | email, password |
| `AuthResponse.java` | accessToken, refreshToken, userId, name, role |
| `UserResponse.java` | Full user profile (used by `/auth/me`) |
| `RefreshTokenRequest.java` | refreshToken string |

### 🔑 Key Insight
The `accessToken` returned by login is what users pass in `Authorization: Bearer` headers. The email is the **subject** of the JWT and becomes `principal.getName()` in all other controllers.

---

## Step 3 — `api-gateway` (Traffic Router)

**Location:** `api-gateway/src/main/java/com/getset/gateway/`  
**Port:** `8090`

> Clients **never** call microservices directly. All traffic hits port 8090, and the gateway routes it.

### Read In This Order

| Class | What to Look For |
|---|---|
| `config/GatewayConfig.java` | Route predicates: `/api/v1/auth/**` → `user-service`, `/api/v1/properties/**` → `property-service`, etc. Also configures Redis-based rate limiting (20 req/min for auth, 60/min for APIs) and per-route circuit breakers |
| `controller/FallbackController.java` | Returns clean `503 Service Unavailable` JSON when a circuit breaker trips. This is what the client sees when a service is down |
| `resources/application.yml` | Key settings: service URLs, Redis config, Resilience4j circuit breaker thresholds, Zipkin tracing |

### ⚠️ JWT Note
The API Gateway does **not** validate JWTs. It just passes the `Authorization` header downstream. Each microservice has its own copy of `JwtService` and `JwtAuthenticationFilter` for validation. This is a design trade-off chosen for service independence.

---

## Step 4 — `property-service` (Core Domain)

**Location:** `property-service/src/main/java/com/getset/property/`  
**Port:** `8082`  
**Database:** MongoDB collection `properties`

> Properties are the central entity. Almost every other service references a `propertyId`.

### Read In This Order

#### `domain/`
| Class | What to Look For |
|---|---|
| `PropertyDocument.java` | MongoDB document. Key fields: `ownerId`, `title`, `type` (enum), `pricePerMonth`, `bedrooms`, `furnished`, `address` (embedded), `location` (GeoJSON Point for geo queries), `isActive` |
| `PropertyRepository.java` | `MongoRepository`. `findByIdAndOwnerId()` is the ownership-check query used before updates/deletes |

#### `api/`
| Class | What to Look For |
|---|---|
| `PropertyController.java` | Standard CRUD + `GET /properties/nearby?lat=&lng=&radiusKm=`. Uses `Principal` from the JWT filter to get the current user's email |
| `InternalPropertyController.java` | `GET /internal/properties/{id}` — returns `PropertySummaryDto`. Called by enquiry and favorite services via Feign. Not exposed through gateway |

#### `client/`
| Class | What to Look For |
|---|---|
| `UserServiceClient.java` | Feign client. Calls `user-service`'s `/internal/users/{id}` to fetch owner info when building property responses |

#### `security/`
Same pattern as user-service: `JwtService` + `JwtAuthenticationFilter`.

---

## Step 5 — `enquiry-service` (Feign + Kafka Producer)

**Location:** `enquiry-service/src/main/java/com/getset/enquiry/`  
**Port:** `8083`  
**Database:** MongoDB collection `enquiries`

> This service introduces both Feign Clients (to get user + property data) and Kafka (to publish notification events).

### Read In This Order

#### `domain/`
| Class | What to Look For |
|---|---|
| `EnquiryStatus.java` | Enum: `PENDING`, `ACCEPTED`, `REJECTED`. An enquiry always starts as PENDING |
| `EnquiryDocument.java` | Fields: `renterId`, `ownerId`, `propertyId`, `message`, `status`, `rejectionReason`, timestamps |
| `EnquiryRepository.java` | `findByPropertyId()`, `findByRenterId()`, `findByIdAndOwnerId()` (for ownership-checked updates) |

#### `client/`
| Class | What to Look For |
|---|---|
| `PropertyServiceClient.java` | `@FeignClient(name = "property-service")`. Calls `/internal/properties/{id}` — gets property title, ownerId, price |
| `UserServiceClient.java` | `@FeignClient(name = "user-service")`. Calls `/internal/users/{id}` — gets renter/owner email for notifications |

#### `event/`
| Class | What to Look For |
|---|---|
| `NotificationEventPublisher.java` | Wraps `KafkaTemplate.send("notification-events", event)`. Uses `recipientId` as the Kafka partition key for ordered delivery per user |

#### `api/`
| Class | Endpoint | What Happens |
|---|---|---|
| `EnquiryController.java` | `POST /enquiries` | Calls Feign → validates property exists → saves enquiry → publishes `ENQUIRY_RECEIVED` Kafka event |
| | `PUT /enquiries/{id}` | Owner accepts/rejects → updates status → publishes `ENQUIRY_ACCEPTED` or `ENQUIRY_REJECTED` event |
| | `GET /enquiries/property/{propertyId}` | Owner sees all enquiries for their property |
| | `GET /enquiries/renter/my-enquiries` | Renter sees their own history |

### 🔑 Key Insight: The Event Flow
```
EnquiryController.createEnquiry()
  → saves EnquiryDocument to MongoDB
  → calls NotificationEventPublisher.publish(ENQUIRY_RECEIVED)
    → KafkaTemplate sends to topic "notification-events"
      → notification-service picks it up (Step 8)
```

---

## Step 6 — `favorite-service` (Simplest Service — Good Pattern Reference)

**Location:** `favorite-service/src/main/java/com/getset/favorite/`  
**Port:** `8084`  
**Database:** MongoDB collection `favorites`

> The simplest complete microservice. Great for understanding the standard pattern without Kafka complexity.

### Read In This Order

| Class | What to Look For |
|---|---|
| `domain/FavoriteDocument.java` | Fields: `renterId`, `propertyId`, `propertyTitle`, `propertyCity`, `pricePerMonth`, `notes`. Notice it **denormalizes** property data — avoids a Feign call on every GET |
| `domain/FavoriteRepository.java` | `existsByRenterIdAndPropertyId()` prevents duplicates. `countByRenterId()` for count endpoint |
| `client/PropertyServiceClient.java` | Feign client: fetches `PropertySummaryDto` from property-service when adding a new favorite |
| `api/FavoriteController.java` | 5 endpoints. `POST /{propertyId}` calls Feign to validate property exists + snapshot its data. Uses `Principal` to scope all queries to current renter |
| `config/SecurityConfig.java` | All endpoints require auth — no public routes |

### Standard Service Layer Pattern
```
Request → SecurityFilter (JWT) → Controller → Feign client (if needed) → Repository → MongoDB
```

---

## Step 7 — `message-service` (Feign + Kafka Producer)

**Location:** `message-service/src/main/java/com/getset/message/`  
**Port:** `8085`  
**Database:** MongoDB collection `messages`

> Same Feign + Kafka pattern as enquiry-service, but for threaded conversations.

### Read In This Order

| Class | What to Look For |
|---|---|
| `domain/MessageDocument.java` | Key field: `threadId` (format: `userId1_userId2_propertyId` — ensures same two users always use the same thread). Also: `senderId`, `recipientId`, `content`, `read` |
| `domain/MessageRepository.java` | `findByThreadId()` fetches entire thread. `countByRecipientIdAndReadFalse()` for unread badge |
| `client/UserServiceClient.java` | Fetches sender/recipient display names from user-service |
| `event/NotificationEventPublisher.java` | Same as enquiry-service. Publishes `MESSAGE_RECEIVED` to Kafka |
| `api/MessageController.java` | `POST /messages` → saves → publishes event → returns message. `GET /messages/conversations` groups threads by last message |

---

## Step 8 — `notification-service` (Kafka Consumer — Final Piece)

**Location:** `notification-service/src/main/java/com/getset/notification/`  
**Port:** `8086`

> This is the end of the async pipeline. It consumes Kafka events and sends emails.

### Read In This Order

| Class | What to Look For |
|---|---|
| `consumer/NotificationEventConsumer.java` | `@KafkaListener(topics = "notification-events")`. Deserializes `NotificationEvent` and routes to the correct handler based on `event.getType()` — e.g., `ENQUIRY_RECEIVED` → `sendEnquiryReceivedEmail()` |
| `service/EmailService.java` | Wraps Spring's `JavaMailSender`. Has dedicated methods for each notification type: `sendEnquiryReceivedEmail()`, `sendEnquiryAcceptedEmail()`, `sendEnquiryRejectedEmail()`, `sendMessageReceivedEmail()` |

### 🔑 The Full Async Flow
```
enquiry-service  ──────► Kafka topic "notification-events" ──────► NotificationEventConsumer
message-service  ──────►                                             │
                                                                     ▼
                                                              EmailService.sendXxxEmail()
                                                                     │
                                                                     ▼
                                                              User's email inbox
```

---

## 🔗 How Services Connect — The Full Map

```
user-service ◄── InternalUserController
   ▲                    ▲
   │                    │
   │ (Feign)       (Feign)
   │                    │
enquiry-service    message-service
   │    │               │
   │    └───────────────┘
   │         │ (Kafka events)
   │         ▼
   │  notification-service ──► email
   │
property-service ◄── InternalPropertyController
   ▲
   │ (Feign)
   │
enquiry-service
favorite-service
```

---

## 🐳 Running the Whole Stack

### Step 1: Start Infrastructure
```bash
cd d:\Projects\getSet\backend
docker-compose up -d
```
This starts: MongoDB, Kafka, Zookeeper, Zipkin, Prometheus, Grafana

### Step 2: Build the Common Library First (required!)
```bash
cd getset-common
mvn clean install
```
> ⚠️ This MUST be done before building any service. All services depend on `getset-common`.

### Step 3: Start Services
Start in this order (dependencies first):
```bash
# Terminal 1
cd user-service && mvn spring-boot:run

# Terminal 2 (after user-service is up)
cd property-service && mvn spring-boot:run

# Terminal 3
cd enquiry-service && mvn spring-boot:run

# Terminal 4
cd favorite-service && mvn spring-boot:run

# Terminal 5
cd message-service && mvn spring-boot:run

# Terminal 6
cd notification-service && mvn spring-boot:run

# Terminal 7 (last — after all services are up)
cd api-gateway && mvn spring-boot:run
```

### Step 4: Verify Everything is Running
| Service | Health Check URL |
|---|---|
| API Gateway | http://localhost:8090/actuator/health |
| User Service | http://localhost:8081/actuator/health |
| Property Service | http://localhost:8082/actuator/health |
| Enquiry Service | http://localhost:8083/actuator/health |
| Favorite Service | http://localhost:8084/actuator/health |
| Message Service | http://localhost:8085/actuator/health |
| Notification Service | http://localhost:8086/actuator/health |
| Zipkin (Tracing) | http://localhost:9411 |
| Grafana (Metrics) | http://localhost:3000 |
| Prometheus | http://localhost:9090 |

---

## 🧪 End-to-End Test Walkthrough

Follow these exact steps through the gateway (port 8090):

### 1. Register an Owner
```bash
curl -X POST http://localhost:8090/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Alice Owner","email":"alice@example.com","password":"Password123!","role":"OWNER","phone":"9999999999"}'
```

### 2. Register a Renter
```bash
curl -X POST http://localhost:8090/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Bob Renter","email":"bob@example.com","password":"Password123!","role":"RENTER","phone":"8888888888"}'
```

### 3. Login as Owner — Save accessToken
```bash
curl -X POST http://localhost:8090/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@example.com","password":"Password123!"}'
# Copy the "accessToken" from the response → OWNER_TOKEN
```

### 4. Create a Property (as Owner)
```bash
curl -X POST http://localhost:8090/api/v1/properties \
  -H "Authorization: Bearer <OWNER_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Spacious 2BHK near Metro",
    "description": "Well-ventilated apartment with parking",
    "type": "APARTMENT",
    "pricePerMonth": 22000,
    "bedrooms": 2, "bathrooms": 1, "furnished": true,
    "amenities": ["WiFi","AC","Parking"],
    "address": {"fullAddress":"12 MG Road","city":"Bangalore","state":"Karnataka","country":"India","pincode":"560001"},
    "lat": 12.9716, "lng": 77.5946
  }'
# Copy the "id" from the response → PROPERTY_ID
```

### 5. Login as Renter — Save accessToken
```bash
curl -X POST http://localhost:8090/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"bob@example.com","password":"Password123!"}'
# Copy the "accessToken" → RENTER_TOKEN
```

### 6. Add Property to Favorites (as Renter)
```bash
curl -X POST "http://localhost:8090/api/v1/favorites/<PROPERTY_ID>?notes=Love+this+place" \
  -H "Authorization: Bearer <RENTER_TOKEN>"
```

### 7. Send an Enquiry (as Renter)
```bash
curl -X POST http://localhost:8090/api/v1/enquiries \
  -H "Authorization: Bearer <RENTER_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"propertyId":"<PROPERTY_ID>","message":"Is this available from March?"}'
# This triggers a Kafka event → notification-service sends an email to Alice
# Copy the enquiry "id" → ENQUIRY_ID
```

### 8. Accept the Enquiry (as Owner)
```bash
curl -X PUT http://localhost:8090/api/v1/enquiries/<ENQUIRY_ID> \
  -H "Authorization: Bearer <OWNER_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"status":"ACCEPTED"}'
# Triggers ENQUIRY_ACCEPTED Kafka event → Bob gets an email
```

### 9. Send a Message (as Renter)
```bash
curl -X POST http://localhost:8090/api/v1/messages \
  -H "Authorization: Bearer <RENTER_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"recipientId":"<ALICE_USER_ID>","propertyId":"<PROPERTY_ID>","content":"When can I visit?"}'
# Triggers MESSAGE_RECEIVED Kafka event → Alice gets an email
```

### 10. Check Distributed Traces
Visit http://localhost:9411 (Zipkin) → search for service `api-gateway` → see the full request trace across all services.

---

## 📁 Where to Find Each Environment Variable

All environment variables are documented in `.env.example` at the root. Key ones:

| Variable | Used By | Purpose |
|---|---|---|
| `MONGODB_URI` | All services | Database connection |
| `JWT_SECRET` | All services | Must be **identical** across all services |
| `KAFKA_BOOTSTRAP_SERVERS` | enquiry, message, notification | Kafka broker address |
| `MAIL_HOST`, `MAIL_USERNAME`, `MAIL_PASSWORD` | notification-service | SMTP for emails |
| `APP_CORS_ALLOWED_ORIGINS` | api-gateway | Frontend URL |

> ⚠️ **Critical**: `JWT_SECRET` must be the same value in every service's `application.yml`. If they differ, JWTs signed by `user-service` will fail validation in other services.

---

## 🏗️ Common Patterns Across All Services

Every microservice follows the same internal structure:

```
api/          ← REST controllers. Use Principal for current user identity
client/       ← Feign clients to call other services  
config/       ← SecurityConfig (JWT filter chain)
domain/       ← MongoDB @Document + MongoRepository
dto/          ← Request/Response DTOs
event/        ← NotificationEventPublisher (if service produces Kafka events)
security/     ← JwtService + JwtAuthenticationFilter (copied per service)
exception/    ← GlobalExceptionHandler (for consistent error responses)
```

### The Standard Request Lifecycle
```
HTTP Request
  → API Gateway (port 8090) — rate limit check, route
  → Microservice JwtAuthenticationFilter — validates Bearer token
  → SecurityConfig — checks role is allowed
  → Controller — reads Principal.getName() for current user
  → [Optional] FeignClient — fetches data from sibling service
  → Repository — reads/writes MongoDB
  → [Optional] NotificationEventPublisher — sends Kafka event
  → HTTP Response
```

---

## 🐛 Common Gotchas

| Symptom | Cause | Fix |
|---|---|---|
| `401 Unauthorized` on all requests | JWT_SECRET mismatch between services | Make sure all `application.yml` files use the same secret |
| `Connection refused` on Feign calls | Target service not running | Start services in the correct order (user → property → others) |
| Package declaration IDE error | IDE source root wrong | `Ctrl+Shift+P` → Java: Clean Java Language Server Workspace |
| Kafka consumer not receiving events | Zookeeper/Kafka containers not healthy | `docker-compose ps` — restart if exited |
| `getset-common` classes not found | Common library not installed | `cd getset-common && mvn clean install` first |

---

*Happy exploring! Start with `getset-common`, follow the steps in order, and you'll have a complete mental model of the system within a few hours.* 🏠
