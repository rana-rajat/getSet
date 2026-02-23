# GetSet Backend — Complete Project Structure Reference (Microservices)

> Every package, every class, every file — with a one-line description of what each one does.
> For a guided walkthrough, see **[DEVELOPER_WALKTHROUGH.md](./DEVELOPER_WALKTHROUGH.md)**.

---

## High-Level Layout

```
getSet/backend/
├── getset-common/           ← Shared library (DTOs, exceptions, Kafka event types)
├── user-service/            ← Auth + user management (port 8081)
├── property-service/        ← Property CRUD + geospatial search (port 8082)
├── enquiry-service/         ← Enquiry lifecycle (port 8083)
├── favorite-service/        ← Wishlists / saved properties (port 8084)
├── message-service/         ← Threaded direct messaging (port 8085)
├── notification-service/    ← Kafka consumer + email sender (port 8086)
├── api-gateway/             ← Spring Cloud Gateway (port 8090)
├── docker/                  ← Prometheus + Grafana config
├── docker-compose.yml       ← Full infrastructure stack
├── README.md                ← Project overview
├── API_DOCS.md              ← Comprehensive endpoint list with mock data
├── DEVELOPER_WALKTHROUGH.md ← Step-by-step onboarding guide ⭐
└── .env.example             ← All required environment variables
```

---

## `getset-common` — Shared Library

> Must be built first (`mvn clean install`) before any service can compile.

### `src/main/java/com/getset/common/`

#### `dto/`

| File | Role |
|---|---|
| `ApiError.java` | Standard error response: `{status, error, message, timestamp}` |
| `PageResponse<T>.java` | Generic paginated response wrapper: content, pageNumber, pageSize, totalElements, hasNext, hasPrevious |
| `PropertySummaryDto.java` | Lightweight property snapshot used by enquiry-service and favorite-service (avoids circular Feign calls) |
| `UserSummaryDto.java` | Lightweight user snapshot used by message-service and enquiry-service |

#### `event/`

| File | Role |
|---|---|
| `NotificationEvent.java` | Kafka event DTO. Fields: recipientId, recipientEmail, senderName, propertyTitle, message, eventType. Inner enum `EventType`: `ENQUIRY_RECEIVED`, `ENQUIRY_ACCEPTED`, `ENQUIRY_REJECTED`, `MESSAGE_RECEIVED` |

#### `exception/`

| File | Role |
|---|---|
| `GetSetException.java` | Base custom exception — all service-specific exceptions extend this |
| `NotFoundException.java` | HTTP 404 — resource not found |
| `ForbiddenException.java` | HTTP 403 — user doesn't own the resource |
| `UnauthorizedException.java` | HTTP 401 — invalid or expired JWT |

---

## `user-service` — Authentication & User Management (Port 8081)

### `src/main/java/com/getset/user/`

#### Root

| File | Role |
|---|---|
| `UserServiceApplication.java` | Spring Boot entry point. `@SpringBootApplication` + `@EnableMongoAuditing` |

#### `domain/`

| File | Role |
|---|---|
| `UserDocument.java` | MongoDB `@Document("users")`. Implements `UserDetails`. Fields: id, name, email, password (BCrypt), role, phone, createdAt, updatedAt |
| `UserRepository.java` | `MongoRepository`. Methods: `findByEmail(String)`, `existsByEmail(String)` |
| `Role.java` | Enum: `RENTER`, `OWNER`, `ADMIN` |

#### `security/`

| File | Role |
|---|---|
| `JwtService.java` | JWT utility: `generateToken()` (embeds role claim), `generateRefreshToken()`, `extractUsername()`, `isTokenValid()` |
| `JwtAuthenticationFilter.java` | `OncePerRequestFilter`: extracts Bearer token → validates → sets `SecurityContextHolder` |

#### `config/`

| File | Role |
|---|---|
| `SecurityConfig.java` | Public routes: `/api/v1/auth/**`. All others require JWT. Wires in `JwtAuthenticationFilter` |
| `AuthService.java` | Business logic: `register()` (BCrypt hash + uniqueness check), `login()` (AuthenticationManager), `refreshToken()`, `getCurrentUser()` |

#### `api/`

| File | Role |
|---|---|
| `AuthController.java` | `POST /auth/register`, `POST /auth/login`, `POST /auth/refresh`, `GET /auth/me` |
| `InternalUserController.java` | `GET /internal/users/{id}` — returns `UserSummaryDto`. Called by other services via Feign. NOT exposed through gateway |

#### `dto/`

| File | Role |
|---|---|
| `RegisterRequest.java` | name, email, password, role, phone |
| `LoginRequest.java` | email, password |
| `RefreshTokenRequest.java` | refreshToken string |
| `AuthResponse.java` | accessToken, refreshToken, userId, name, email, role |
| `UserResponse.java` | Full user profile (name, email, role, phone, createdAt) |

#### `exception/`

| File | Role |
|---|---|
| `GlobalExceptionHandler.java` | `@RestControllerAdvice`. Returns consistent `ApiError` JSON for all exceptions |

---

## `property-service` — Property Management (Port 8082)

### `src/main/java/com/getset/property/`

#### Root

| File | Role |
|---|---|
| `PropertyServiceApplication.java` | Spring Boot entry point |

#### `domain/`

| File | Role |
|---|---|
| `PropertyDocument.java` | MongoDB `@Document("properties")`. Key fields: ownerId, title, type, pricePerMonth, bedrooms, bathrooms, furnished, amenities, address (embedded), location (GeoJSON Point for `2dsphere` geospatial queries), photos, isActive |
| `PropertyRepository.java` | `MongoRepository`. `findByIdAndOwnerId()` — used for ownership validation before updates/deletes |

#### `api/`

| File | Role |
|---|---|
| `PropertyController.java` | `POST /properties` (create), `GET /properties/{id}`, `GET /properties` (search with filters), `GET /properties/nearby` (geospatial), `PUT /properties/{id}` (owner-only update), `DELETE /properties/{id}` (owner-only delete), `GET /properties/owner/my-properties` |
| `InternalPropertyController.java` | `GET /internal/properties/{id}` — returns `PropertySummaryDto`. Called by enquiry-service and favorite-service via Feign. NOT exposed through gateway |

#### `client/`

| File | Role |
|---|---|
| `UserServiceClient.java` | `@FeignClient(name="user-service")`. Calls `GET /internal/users/{id}` to fetch owner info for property responses |

#### `security/`

| File | Role |
|---|---|
| `JwtService.java` | Same JWT utility as user-service (copied for service independence) |
| `JwtAuthenticationFilter.java` | Same filter pattern — validates Bearer token on each request |

#### `config/`

| File | Role |
|---|---|
| `SecurityConfig.java` | Public: `GET /properties/**`. Protected: all mutations (create/update/delete) |

---

## `enquiry-service` — Enquiry Lifecycle (Port 8083)

### `src/main/java/com/getset/enquiry/`

#### Root

| File | Role |
|---|---|
| `EnquiryServiceApplication.java` | Spring Boot entry point |

#### `domain/`

| File | Role |
|---|---|
| `EnquiryStatus.java` | Enum: `PENDING`, `ACCEPTED`, `REJECTED` |
| `EnquiryDocument.java` | MongoDB `@Document("enquiries")`. Fields: renterId, ownerId, propertyId, message, status, rejectionReason, createdAt, updatedAt |
| `EnquiryRepository.java` | `findByPropertyId()` (owner views), `findByRenterId()` (renter views), `findByIdAndOwnerId()` (ownership check for updates) |

#### `client/`

| File | Role |
|---|---|
| `PropertyServiceClient.java` | `@FeignClient`. Calls `GET /internal/properties/{id}` → validates property exists, gets ownerId + title |
| `UserServiceClient.java` | `@FeignClient`. Calls `GET /internal/users/{id}` → gets renter/owner email for notification payloads |

#### `event/`

| File | Role |
|---|---|
| `NotificationEventPublisher.java` | Wraps `KafkaTemplate.send("notification-events", recipientId, event)`. Uses `recipientId` as partition key for ordered delivery per user |

#### `api/`

| File | Role |
|---|---|
| `EnquiryController.java` | `POST /enquiries` (create + publish `ENQUIRY_RECEIVED`), `GET /enquiries/{id}`, `GET /enquiries/property/{propertyId}`, `GET /enquiries/renter/my-enquiries`, `PUT /enquiries/{id}` (accept/reject + publish `ENQUIRY_ACCEPTED`/`ENQUIRY_REJECTED`), `DELETE /enquiries/{id}` |

#### `dto/`

| File | Role |
|---|---|
| `EnquiryRequest.java` | propertyId, message |
| `EnquiryUpdateRequest.java` | status (`ACCEPTED`/`REJECTED`), rejectionReason |

#### `security/` + `config/`

| File | Role |
|---|---|
| `JwtService.java` | Same JWT utility (copied) |
| `JwtAuthenticationFilter.java` | Same filter pattern |
| `SecurityConfig.java` | All endpoints require auth |

---

## `favorite-service` — Wishlists (Port 8084)

### `src/main/java/com/getset/favorite/`

#### Root

| File | Role |
|---|---|
| `FavoriteServiceApplication.java` | Spring Boot entry point |

#### `domain/`

| File | Role |
|---|---|
| `FavoriteDocument.java` | MongoDB `@Document("favorites")`. Fields: renterId, propertyId, **propertyTitle, propertyCity, pricePerMonth** (denormalized snapshot), notes, createdAt |
| `FavoriteRepository.java` | `findByRenterId()`, `existsByRenterIdAndPropertyId()` (prevents duplicates), `deleteByRenterIdAndPropertyId()`, `countByRenterId()` |

#### `client/`

| File | Role |
|---|---|
| `PropertyServiceClient.java` | `@FeignClient`. Calls `GET /internal/properties/{id}` — fetches property data when adding a new favorite (snapshot is stored, not re-fetched on reads) |

#### `api/`

| File | Role |
|---|---|
| `FavoriteController.java` | `POST /favorites/{propertyId}` (add + Feign call), `DELETE /favorites/{propertyId}` (remove), `GET /favorites` (list), `GET /favorites/check/{propertyId}` (boolean check), `GET /favorites/count` |

#### `security/` + `config/`

| File | Role |
|---|---|
| `JwtService.java` | Same JWT utility (copied) |
| `JwtAuthenticationFilter.java` | Same filter pattern |
| `SecurityConfig.java` | All endpoints require auth |

---

## `message-service` — Direct Messaging (Port 8085)

### `src/main/java/com/getset/message/`

#### Root

| File | Role |
|---|---|
| `MessageServiceApplication.java` | Spring Boot entry point |

#### `domain/`

| File | Role |
|---|---|
| `MessageDocument.java` | MongoDB `@Document("messages")`. Key fields: `threadId` (format: `userId1_userId2_propertyId`), senderId, recipientId, propertyId, content, `read` (boolean), createdAt |
| `MessageRepository.java` | `findByThreadId()` (full thread), `findByRecipientId()`, `findBySenderId()`, `countByRecipientIdAndReadFalse()` |

#### `client/`

| File | Role |
|---|---|
| `UserServiceClient.java` | `@FeignClient`. Fetches sender/recipient display names from user-service |

#### `event/`

| File | Role |
|---|---|
| `NotificationEventPublisher.java` | Publishes `MESSAGE_RECEIVED` event to Kafka `notification-events` topic |

#### `api/`

| File | Role |
|---|---|
| `MessageController.java` | `POST /messages` (send + publish event), `GET /messages/thread/{threadId}`, `GET /messages/conversations` (grouped threads), `GET /messages/unread`, `GET /messages/unread/count`, `PUT /messages/{id}/read`, `PUT /messages/read-all` |

#### `security/` + `config/`

| File | Role |
|---|---|
| `JwtService.java` | Same JWT utility (copied) |
| `JwtAuthenticationFilter.java` | Same filter pattern |
| `SecurityConfig.java` | All endpoints require auth |

---

## `notification-service` — Kafka Consumer + Email (Port 8086)

### `src/main/java/com/getset/notification/`

#### Root

| File | Role |
|---|---|
| `NotificationServiceApplication.java` | Spring Boot entry point |

#### `consumer/`

| File | Role |
|---|---|
| `NotificationEventConsumer.java` | `@KafkaListener(topics="notification-events", groupId="notification-group")`. Deserializes `NotificationEvent` JSON → routes to `EmailService` based on `event.getType()` |

#### `service/`

| File | Role |
|---|---|
| `EmailService.java` | Wraps `JavaMailSender`. Methods: `sendEnquiryReceivedEmail()`, `sendEnquiryAcceptedEmail()`, `sendEnquiryRejectedEmail()`, `sendMessageReceivedEmail()` — each builds a formatted email body |

---

## `api-gateway` — Entry Point (Port 8090)

### `src/main/java/com/getset/gateway/`

#### Root

| File | Role |
|---|---|
| `ApiGatewayApplication.java` | Spring Boot entry point |

#### `config/`

| File | Role |
|---|---|
| `GatewayConfig.java` | Route definitions: `/api/v1/auth/**` → user-service, `/api/v1/properties/**` → property-service, `/api/v1/enquiries/**` → enquiry-service, `/api/v1/favorites/**` → favorite-service, `/api/v1/messages/**` → message-service. Redis rate limiters per route. Resilience4j circuit breakers per route. CORS headers |

#### `controller/`

| File | Role |
|---|---|
| `FallbackController.java` | `GET /fallback` — returns `503 Service Unavailable` JSON when a circuit breaker opens |

#### `resources/`

| File | Role |
|---|---|
| `application.yml` | Port 8090, service URLs, Redis config, Resilience4j thresholds, Zipkin config |

---

## Infrastructure Files

| File | Role |
|---|---|
| `docker-compose.yml` | Spins up: MongoDB, Zookeeper, Kafka, Zipkin, Prometheus, Grafana |
| `docker/prometheus.yml` | Scrapes each service's `/actuator/prometheus` every 15s |
| `docker/grafana/provisioning/datasources/prometheus.yml` | Auto-connects Grafana to Prometheus datasource |
| `.env.example` | Template with all required environment variables and descriptions |

---

## Quick Stats

| Metric | Count |
|---|---|
| Microservices | **7** (user, property, enquiry, favorite, message, notification + api-gateway) |
| Shared library | **1** (getset-common) |
| Java source files (total) | **~70** across all services |
| REST Endpoints | **35+** |
| Kafka Topics | **1** (`notification-events`, 3 partitions) |
| MongoDB Collections | **6** (users, properties, enquiries, favorites, messages, notifications) |
| Docker Compose Services | **6** infrastructure services |
| Feign Client calls | **6** inter-service call paths |

---

## Inter-Service Dependency Map

```
notification-service
        ▲ Kafka
        │
enquiry-service ──Feign──► property-service ──Feign──► user-service
message-service ──Feign─────────────────────────────► user-service
favorite-service ─Feign──► property-service
enquiry-service ──Feign──► user-service

api-gateway routes to: all 6 business services
getset-common: imported by all services (compile-time dependency)
```
