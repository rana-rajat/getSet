# GetSet Backend — Complete Project Structure Reference

> Every package, every class, every file — with a one-line description of what each one does.

---

## High-Level Layout

```
getSet/backend/
├── getset-backend/          ← Main Spring Boot application (port 8080)
│   ├── src/main/java/com/getset/
│   ├── src/test/java/com/getset/
│   └── src/main/resources/
├── api-gateway/             ← Spring Cloud Gateway (port 8090)
│   └── src/main/java/com/getset/gateway/
├── docker/                  ← Observability config files
│   ├── prometheus.yml
│   └── grafana/provisioning/datasources/prometheus.yml
└── docker-compose.yml       ← All 10 services
```

---

## `getset-backend` — Main Application

### Root

| File | Role |
|---|---|
| `GetSetApplication.java` | Spring Boot entry point. Has `@SpringBootApplication` + `@EnableMongoAuditing` |

---

### `auth/` — Authentication & JWT

| File | Role |
|---|---|
| `AuthController.java` | REST controller: `POST /auth/register`, `POST /auth/login`, `POST /auth/refresh`, `GET /auth/me` |
| `AuthService.java` | Business logic: register (BCrypt hash + save), login (AuthenticationManager), refreshToken (validate + new access token), getCurrentUser. Has Resilience4j `@Retry`, `@CircuitBreaker`, `@RateLimiter` |
| `dto/RegisterRequest.java` | Incoming: name, email, password, role, phone. Jakarta `@NotBlank` validation |
| `dto/LoginRequest.java` | Incoming: email, password |
| `dto/RefreshTokenRequest.java` | Incoming: refreshToken string |
| `dto/AuthResponse.java` | Outgoing: accessToken, refreshToken, id, name, email, role |
| `dto/UserResponse.java` | Outgoing: id, name, email, role, phone, createdAt (used by `GET /auth/me`) |

---

### `user/` — User Data Layer

| File | Role |
|---|---|
| `UserDocument.java` | MongoDB document `@Document("users")`. Implements `UserDetails` for Spring Security. Fields: id, name, email, password (BCrypt), role, phone, createdAt, updatedAt |
| `UserRepository.java` | `MongoRepository`. Custom methods: `findByEmail(String)`, `existsByEmail(String)` |
| `UserMapper.java` | Static mapper: `UserDocument → UserResponse` |
| `Role.java` | Enum: `RENTER`, `OWNER`, `ADMIN` |

---

### `property/` — Property Listings

| File | Role |
|---|---|
| `PropertyDocument.java` | MongoDB document `@Document("properties")`. Has `GeoJsonPoint location` for geospatial queries |
| `PropertyRepository.java` | `MongoRepository` + `PropertyRepositoryCustom`. Query: `findByIdAndOwnerId()` |
| `PropertyRepositoryCustom.java` | Interface for custom query methods |
| `PropertyRepositoryImpl.java` | `MongoTemplate`-based queries: advanced search (city, price range, bedrooms, furnished, pagination, sorting) |
| `PropertyService.java` | Service interface |
| `PropertyServiceImpl.java` | Business logic: create, update (ownership check `ForbiddenException`), delete, getById, search (paginated), getOwnerProperties, getNearby (2dsphere query). Has `@Retry`, `@CircuitBreaker` |
| `PropertyController.java` | 7 REST endpoints. Returns `PageResponse<PropertyResponse>` for search |
| `PropertyMapper.java` | Static mapper: `PropertyDocument → PropertyResponse` |
| `Address.java` | Embedded value object: fullAddress, city, state, country, pincode |
| `Location.java` | Embedded value object: GeoJSON Point (lat/lng) |
| `PropertyType.java` | Enum: `APARTMENT`, `HOUSE`, `PG`, `VILLA` |
| `dto/PropertyCreateRequest.java` | Incoming create payload |
| `dto/PropertyUpdateRequest.java` | Incoming update payload |
| `dto/PropertyResponse.java` | Full outgoing response |
| `dto/PropertySummaryResponse.java` | Lightweight list item response |
| `dto/AddressDto.java` | Address sub-DTO |

---

### `enquiry/` — Enquiry Management

| File | Role |
|---|---|
| `EnquiryDocument.java` | MongoDB document: propertyId, ownerId, renterId, message, status, rejectionReason, timestamps |
| `EnquiryRepository.java` | Custom queries: `findByPropertyId()`, `findByRenterId()`, `findByIdAndOwnerId()`, `countByOwnerIdAndStatus()` |
| `EnquiryService.java` | Service interface |
| `EnquiryServiceImpl.java` | Business logic: create (publishes `ENQUIRY_RECEIVED` Kafka event), getById, getByPropertyId, getByRenterId, update/accept/reject (publishes `ENQUIRY_ACCEPTED`/`ENQUIRY_REJECTED` Kafka event), delete, getStats |
| `EnquiryController.java` | 7 REST endpoints |
| `EnquiryStatus.java` | Enum: `PENDING`, `ACCEPTED`, `REJECTED` |
| `dto/EnquiryRequest.java` | Incoming: propertyId, message |
| `dto/EnquiryUpdateRequest.java` | Incoming: status, rejectionReason |
| `dto/EnquiryResponse.java` | Outgoing: full enquiry with renter + owner info |

---

### `favorite/` — Wishlist / Favorites

| File | Role |
|---|---|
| `FavoriteDocument.java` | MongoDB document: renterId, propertyId, notes, timestamps |
| `FavoriteRepository.java` | Custom queries: `findByRenterIdAndPropertyId()`, `existsByRenterIdAndPropertyId()`, `countByRenterId()` |
| `FavoriteService.java` | Service interface |
| `FavoriteServiceImpl.java` | Business logic: add, remove, getAll, check, count, updateNotes |
| `FavoriteController.java` | 6 REST endpoints |
| `dto/FavoriteResponse.java` | Outgoing: id, renterId, propertyId, notes, createdAt |

---

### `message/` — Direct Messaging

| File | Role |
|---|---|
| `MessageDocument.java` | MongoDB document: threadId, senderId/Name/Email, recipientId/Name/Email, propertyId, enquiryId, content, read, createdAt |
| `MessageRepository.java` | Queries by threadId, senderId, recipientId, unread status |
| `MessageService.java` | Service interface |
| `MessageServiceImpl.java` | Business logic: sendMessage (publishes `MESSAGE_RECEIVED` Kafka event), getConversation, getReceived, getSent, getUnread, getUnreadCount, markRead, markAllRead, getConversations |
| `MessageController.java` | 10 REST endpoints |
| `dto/MessageRequest.java` | Incoming: recipientId, propertyId, enquiryId, content, threadId |
| `dto/MessageResponse.java` | Outgoing: full message with sender/recipient info |
| `dto/ConversationResponse.java` | Outgoing: conversation thread summary |

---

### `notification/` — In-App Notifications + Email

| File | Role |
|---|---|
| `NotificationDocument.java` | MongoDB document: recipientId/Email, subject, body, type, relatedEntityId, read, emailSent, emailSentError, createdAt |
| `NotificationRepository.java` | Queries: `findByRecipientId()`, `findByRecipientIdAndRead()`, `countByRecipientIdAndRead()` |
| `NotificationService.java` | Service interface |
| `NotificationServiceImpl.java` | Persists notification document + sends email. Called by `NotificationEventConsumer` (async) |
| `NotificationController.java` | 6 REST endpoints |
| `EmailService.java` | Wraps `JavaMailSender`. Methods: `sendEnquiryReceivedEmail()`, `sendEnquiryAcceptedEmail()`, `sendEnquiryRejectedEmail()`, `sendEmail()` |
| `dto/NotificationResponse.java` | Outgoing: notification details |

---

### `events/` — Kafka Event Infrastructure ✨ NEW

| File | Role |
|---|---|
| `NotificationEvent.java` | Event DTO. Inner enum `EventType`: `ENQUIRY_RECEIVED`, `ENQUIRY_ACCEPTED`, `ENQUIRY_REJECTED`, `MESSAGE_RECEIVED` |
| `NotificationEventPublisher.java` | `@Component`. Wraps `KafkaTemplate.send()`. Uses `recipientId` as partition key. Has `published`/`failed` Prometheus counters |
| `NotificationEventConsumer.java` | `@KafkaListener` on `notification-events` topic. Routes event to correct `NotificationService.notifyXxx()` method |

---

### `search/` — Full-Text Search (Elasticsearch)

| File | Role |
|---|---|
| `controller/SearchController.java` | 8 REST endpoints: full-text search, suggestions, facets, trending, popular, admin sync, admin trending-by-city, admin statistics |
| `service/PropertySearchService.java` | Search service interface |
| `service/impl/PropertySearchServiceImpl.java` | Elasticsearch queries, Redis caching (5-level strategy), search analytics tracking, scheduled sync tasks |
| `dto/SearchRequestDto.java` | Search parameters DTO |
| `dto/SearchResponseDto.java` | Search results DTO |

---

### `config/` — Spring Configuration

| File | Role |
|---|---|
| `SecurityConfig.java` | JWT filter chain. Public: GET properties, auth endpoints, Swagger. All else requires bearer token |
| `AppConfig.java` | Beans: `BCryptPasswordEncoder`, `ModelMapper`, `AuthenticationManager` |
| `CorsConfig.java` | CORS from `APP_CORS_ALLOWED_ORIGINS` env var |
| `MongoConfig.java` | `MongoTemplate` bean, custom type converters |
| `RedisConfig.java` | Redis connection factory + `RedisTemplate` |
| `OpenApiConfig.java` | Swagger UI with Bearer token support |
| `KafkaConfig.java` | ✨ NEW — Kafka topic (`notification-events`), idempotent producer, concurrent consumer factory |
| `ObservabilityConfig.java` | ✨ NEW — Global metric tags (app, env, version) + 6 business counters for Prometheus/Grafana |

---

### `security/` — JWT Filter

| File | Role |
|---|---|
| `JwtService.java` | JWT utility: `generateToken()`, `generateRefreshToken()`, `extractUsername()`, `isTokenValid()` |
| `JwtAuthenticationFilter.java` | `OncePerRequestFilter`: extracts token → validates → sets `SecurityContextHolder` |

---

### `exception/` — Error Handling

| File | Role |
|---|---|
| `GlobalExceptionHandler.java` | `@RestControllerAdvice`. Catches all exceptions, returns consistent JSON `{status, error, message, timestamp}` |
| `GetSetException.java` | Base custom exception class |
| `NotFoundException.java` | 404 Not Found |
| `ForbiddenException.java` | 403 Forbidden (used when user doesn't own the resource) |
| `UnauthorizedException.java` | 401 Unauthorized (used for invalid/expired tokens) |

---

### `common/` — Shared Utilities

| File | Role |
|---|---|
| `ApiError.java` | Standard error response body DTO |
| `PageResponse<T>.java` | Generic paginated response wrapper: content, page, size, totalElements, totalPages, hasNext, hasPrevious |
| `NotFoundException.java` | Duplicate of `exception/NotFoundException` (kept for compatibility — consolidation recommended) |

---

### `cache/`

| File | Role |
|---|---|
| `CacheConstants.java` | String constants for all Redis cache keys / TTL values |

---

### `util/` — Utilities

| File | Role |
|---|---|
| `AuditLogger.java` | Structured security audit logger: `logAuthenticationAttempt()`, `logRegistrationAttempt()`, `logResourceAccess()`, `logAuthorizationFailure()` |
| `DateUtil.java` | Date/time formatting helpers |
| `GeoUtil.java` | Geospatial calculation helpers (distance, bounding box) |
| `StringUtil.java` | String manipulation helpers |
| `ValidationUtil.java` | Input validation helpers |

---

### `src/main/resources/` — Configuration Files

| File | Role |
|---|---|
| `application.yml` | Base config: MongoDB, Redis, Elasticsearch, Kafka, JWT, Mail, Actuator, Zipkin, Prometheus, Resilience4j |
| `application-dev.yml` | Dev overrides: DEBUG logging |
| `application-prod.yml` | Prod overrides: WARN logging, 10% trace sampling |
| `logback-spring.xml` | Log format: JSON (prod), pretty-print (dev) |

---

## Tests — `src/test/java/com/getset/`

| File | Type | Tests |
|---|---|---|
| `security/JwtServiceTest.java` | Unit (Mockito) | 5 tests — token gen, expiry, validation, username extraction |
| `auth/AuthServiceTest.java` | Unit (Mockito) | 8 tests — register, login, refresh, getCurrentUser, error paths |
| `auth/AuthControllerTest.java` | MockMvc (web layer) | 2 tests — HTTP status codes, JSON response structure |
| `property/PropertyServiceImplTest.java` | Unit (Mockito) | 5 tests — CRUD, ownership guard (ForbiddenException), not-found |

---

## `api-gateway/` — Spring Cloud Gateway (Port 8090)

| File | Role |
|---|---|
| `ApiGatewayApplication.java` | Entry point |
| `config/GatewayConfig.java` | 4 routes (auth, properties, search, default). Rate limiters via Redis (20/min auth, 60/min API). Circuit breakers per route. CORS handled here |
| `controller/FallbackController.java` | Returns clean `503 Service Unavailable` JSON when backend circuit breaker opens |
| `src/main/resources/application.yml` | Port 8090, Redis config, Resilience4j circuit breaker params, Zipkin tracing |
| `Dockerfile` | Alpine JRE 21 — minimal container image |
| `pom.xml` | Spring Cloud Gateway, Redis reactive, Resilience4j, Actuator, Micrometer Tracing |

---

## Infrastructure Files

| File | Role |
|---|---|
| `docker-compose.yml` | 10 services: MongoDB, Redis, Elasticsearch, Zookeeper, Kafka, Zipkin, Prometheus, Grafana, api-gateway, getset-backend |
| `docker/prometheus.yml` | Scrapes `/api/v1/actuator/prometheus` every 15s |
| `docker/grafana/provisioning/datasources/prometheus.yml` | Auto-connects Grafana to Prometheus |
| `getset-backend/Dockerfile` | Multi-stage build → runs `getset-backend-1.0.0.jar` |
| `.env.example` | Template for all required environment variables |

---

## Quick Stats

| Metric | Count |
|---|---|
| Java source files (monolith) | **93** |
| Java source files (gateway) | **3** |
| REST Endpoints | **35+** (monolith) + **4 routes** (gateway) |
| Kafka Topics | **1** (`notification-events`, 3 partitions) |
| MongoDB Collections | **6** (users, properties, enquiries, favorites, messages, notifications) |
| Docker Services | **10** |
| Unit Tests | **20** (4 test classes) |
| Custom Prometheus Metrics | **8** business counters |
