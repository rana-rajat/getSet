# GetSet Backend API

A production-ready Spring Boot 3.4.10 REST API for a rental home search platform — featuring JWT authentication with refresh tokens, map-based property search, enquiry management, direct messaging, in-app notifications, and email alerts.

---

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [API Endpoints](#api-endpoints)
- [Environment Variables](#environment-variables)
- [Setup & Running Locally](#setup--running-locally)
- [Running with Docker](#running-with-docker)
- [Spring Profiles](#spring-profiles)
- [API Documentation](#api-documentation)
- [Database Schema](#database-schema)
- [Resilience & Rate Limiting](#resilience--rate-limiting)
- [Security Model](#security-model)
- [Testing](#testing)
- [Future Enhancements](#future-enhancements)

---

## Features

| Feature | Status |
|---|---|
| JWT Authentication with Refresh Tokens | ✅ |
| Role-based access control (OWNER / RENTER / ADMIN) | ✅ |
| Property CRUD with geo-spatial search | ✅ |
| Paginated property search with filters | ✅ |
| Enquiry management (accept / reject workflow) | ✅ |
| Favorites / Wishlist with personal notes | ✅ |
| Direct messaging with conversation threading | ✅ |
| In-app notifications | ✅ |
| Email notifications via SMTP | ✅ |
| Swagger / OpenAPI interactive documentation | ✅ |
| Rate Limiting on auth endpoints | ✅ |
| Circuit Breaker + Retry via Resilience4j | ✅ |
| Structured JSON logging (Logstash) | ✅ |
| Actuator health & metrics endpoints | ✅ |
| Docker + Docker Compose support | ✅ |
| Spring Profiles (dev / prod) | ✅ |

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.4.10 |
| Database | MongoDB 5.0+ |
| Cache | Redis |
| Search | Elasticsearch |
| Security | Spring Security + JJWT 0.12.3 |
| Resilience | Resilience4j (Circuit Breaker, Retry, Rate Limiter) |
| Documentation | SpringDoc OpenAPI 2.5.0 (Swagger UI) |
| Email | Spring Mail (JavaMailSender) |
| Logging | SLF4J + Logback + Logstash Encoder |
| Build | Maven 3.8+ |
| Containerisation | Docker + Docker Compose |
| Test | JUnit 5 + Mockito + Spring Boot Test |

---

## Project Structure

```
getset-backend/
├── src/
│   ├── main/
│   │   ├── java/com/getset/
│   │   │   ├── GetSetApplication.java          # Entry point, @EnableMongoAuditing
│   │   │   ├── auth/                           # Authentication
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── AuthService.java            # register, login, refresh, getCurrentUser
│   │   │   │   └── dto/                        # AuthResponse, LoginRequest, RegisterRequest,
│   │   │   │                                   #   RefreshTokenRequest, UserResponse
│   │   │   ├── property/                       # Property listings
│   │   │   │   ├── PropertyController.java
│   │   │   │   ├── PropertyService.java
│   │   │   │   ├── PropertyServiceImpl.java
│   │   │   │   ├── PropertyRepository.java
│   │   │   │   ├── PropertyRepositoryImpl.java # Custom Mongo queries
│   │   │   │   ├── PropertyDocument.java
│   │   │   │   ├── PropertyType.java           # APARTMENT, HOUSE, PG, VILLA
│   │   │   │   └── dto/
│   │   │   ├── enquiry/                        # Enquiry management
│   │   │   │   ├── EnquiryController.java
│   │   │   │   ├── EnquiryService.java
│   │   │   │   ├── EnquiryServiceImpl.java
│   │   │   │   ├── EnquiryRepository.java
│   │   │   │   ├── EnquiryDocument.java
│   │   │   │   ├── EnquiryStatus.java          # PENDING, ACCEPTED, REJECTED
│   │   │   │   └── dto/
│   │   │   ├── favorite/                       # Wishlist / Favorites
│   │   │   │   ├── FavoriteController.java
│   │   │   │   ├── FavoriteService.java
│   │   │   │   ├── FavoriteServiceImpl.java
│   │   │   │   ├── FavoriteRepository.java
│   │   │   │   ├── FavoriteDocument.java
│   │   │   │   └── dto/
│   │   │   ├── message/                        # Direct messaging
│   │   │   │   ├── MessageController.java
│   │   │   │   ├── MessageService.java
│   │   │   │   ├── MessageServiceImpl.java
│   │   │   │   ├── MessageRepository.java
│   │   │   │   ├── MessageDocument.java
│   │   │   │   └── dto/
│   │   │   ├── notification/                   # In-app & email notifications
│   │   │   │   ├── NotificationController.java
│   │   │   │   ├── NotificationService.java
│   │   │   │   ├── NotificationServiceImpl.java
│   │   │   │   ├── NotificationRepository.java
│   │   │   │   ├── NotificationDocument.java
│   │   │   │   └── dto/
│   │   │   ├── user/
│   │   │   │   ├── UserDocument.java
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── Role.java                  # RENTER, OWNER, ADMIN
│   │   │   │   └── UserMapper.java
│   │   │   ├── security/
│   │   │   │   ├── JwtService.java             # Token generation, validation, refresh
│   │   │   │   └── JwtAuthenticationFilter.java
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java         # Security filter chain
│   │   │   │   ├── CorsConfig.java             # Configurable via APP_CORS_ALLOWED_ORIGINS
│   │   │   │   ├── OpenApiConfig.java
│   │   │   │   ├── MongoConfig.java
│   │   │   │   ├── RedisConfig.java
│   │   │   │   └── AppConfig.java
│   │   │   ├── exception/
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   ├── GetSetException.java        # Base exception
│   │   │   │   ├── NotFoundException.java
│   │   │   │   ├── ForbiddenException.java
│   │   │   │   └── UnauthorizedException.java
│   │   │   ├── common/
│   │   │   │   ├── NotFoundException.java      # Used by service layer
│   │   │   │   ├── ApiError.java
│   │   │   │   └── PageResponse.java           # Paginated response wrapper
│   │   │   ├── search/                         # Elasticsearch search integration
│   │   │   ├── cache/                          # Redis cache helpers
│   │   │   ├── constant/                       # Shared constants
│   │   │   └── util/
│   │   │       ├── AuditLogger.java
│   │   │       └── ...
│   │   └── resources/
│   │       ├── application.yml                 # Base configuration
│   │       ├── application-dev.yml             # Dev overrides (DEBUG logging)
│   │       ├── application-prod.yml            # Prod overrides (WARN logging)
│   │       └── logback-spring.xml              # Logback + Logstash JSON logging
│   └── test/
│       └── java/com/getset/
│           ├── auth/
│           │   ├── AuthControllerTest.java     # MockMvc controller tests
│           │   └── AuthServiceTest.java        # Unit tests (8 cases)
│           ├── property/
│           │   └── PropertyServiceImplTest.java # Unit tests (5 cases)
│           └── security/
│               └── JwtServiceTest.java         # Unit tests (5 cases)
├── Dockerfile
├── docker-compose.yml
├── .env.example
└── pom.xml
```

---

## API Endpoints

### Authentication
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `POST` | `/api/v1/auth/register` | Register a new user (returns 201) | Public |
| `POST` | `/api/v1/auth/login` | Login, returns JWT access + refresh token | Public |
| `POST` | `/api/v1/auth/refresh` | Refresh access token using refresh token | Public |
| `GET` | `/api/v1/auth/me` | Get current authenticated user | JWT |

### Properties
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `POST` | `/api/v1/properties` | Create a listing | OWNER |
| `PUT` | `/api/v1/properties/{id}` | Update a listing | OWNER |
| `DELETE` | `/api/v1/properties/{id}` | Deactivate a listing | OWNER |
| `GET` | `/api/v1/properties/{id}` | Get property details | Public |
| `GET` | `/api/v1/properties` | Search with filters + pagination | Public |
| `GET` | `/api/v1/properties/nearby` | Geo-spatial search near coordinates | Public |
| `GET` | `/api/v1/properties/owner/my-properties` | Get owner's own listings | OWNER |

**Search query params:** `city`, `minPrice`, `maxPrice`, `minBedrooms`, `furnished`, `type`, `page`, `size`, `sort`  
**Nearby query params:** `lat`, `lng`, `radiusKm` (default: 5)

### Enquiries
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `POST` | `/api/v1/enquiries` | Submit an enquiry | RENTER |
| `GET` | `/api/v1/enquiries/{id}` | Get enquiry details | JWT |
| `GET` | `/api/v1/enquiries/property/{propertyId}` | Get enquiries for a property | OWNER |
| `GET` | `/api/v1/enquiries/renter/my-enquiries` | Get own enquiries | RENTER |
| `PUT` | `/api/v1/enquiries/{id}` | Accept / reject an enquiry | OWNER |
| `DELETE` | `/api/v1/enquiries/{id}` | Cancel an enquiry | RENTER |
| `GET` | `/api/v1/enquiries/owner/stats` | Enquiry statistics | OWNER |

### Favorites
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `POST` | `/api/v1/favorites` | Add to favorites | RENTER |
| `DELETE` | `/api/v1/favorites/{propertyId}` | Remove from favorites | RENTER |
| `GET` | `/api/v1/favorites` | Get all favorites | RENTER |
| `GET` | `/api/v1/favorites/check/{propertyId}` | Check if favorited | RENTER |
| `GET` | `/api/v1/favorites/count` | Get favorite count | RENTER |
| `PUT` | `/api/v1/favorites/{propertyId}` | Update notes on a favorite | RENTER |

### Messages
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `POST` | `/api/v1/messages` | Send a message | JWT |
| `GET` | `/api/v1/messages/thread/{threadId}` | Get messages in a thread | JWT |
| `GET` | `/api/v1/messages/received` | Get received messages | JWT |
| `GET` | `/api/v1/messages/sent` | Get sent messages | JWT |
| `GET` | `/api/v1/messages/unread` | Get unread messages | JWT |
| `GET` | `/api/v1/messages/unread/count` | Get unread count | JWT |
| `PUT` | `/api/v1/messages/{id}/read` | Mark message as read | JWT |
| `PUT` | `/api/v1/messages/read-all` | Mark all messages as read | JWT |
| `GET` | `/api/v1/messages/conversation/{otherUserId}/{propertyId}` | Get conversation | JWT |
| `GET` | `/api/v1/messages/conversations` | List all conversations | JWT |
| `DELETE` | `/api/v1/messages/{id}` | Delete a message | JWT |

### Notifications
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `GET` | `/api/v1/notifications` | Get all notifications (paginated) | JWT |
| `GET` | `/api/v1/notifications/unread` | Get unread notifications | JWT |
| `GET` | `/api/v1/notifications/unread/count` | Get unread count | JWT |
| `PUT` | `/api/v1/notifications/{id}/read` | Mark one as read | JWT |
| `PUT` | `/api/v1/notifications/read-all` | Mark all as read | JWT |
| `DELETE` | `/api/v1/notifications/{id}` | Delete a notification | JWT |

### Actuator
| Endpoint | Description |
|---|---|
| `GET /api/v1/actuator/health` | Health check |
| `GET /api/v1/actuator/info` | App info |
| `GET /api/v1/actuator/metrics` | Metrics |

---

## Environment Variables

Copy `.env.example` to `.env` and fill in the values.

| Variable | Required | Default | Description |
|---|---|---|---|
| `JWT_SECRET` | **Yes** | — | Base64-encoded HMAC-SHA256 key (min 32 chars). App fails to start if unset. |
| `MONGODB_URI` | No | `mongodb://localhost:27017/getset` | MongoDB connection URI |
| `REDIS_HOST` | No | `localhost` | Redis host |
| `REDIS_PORT` | No | `6379` | Redis port |
| `REDIS_PASSWORD` | No | _(empty)_ | Redis password |
| `ELASTICSEARCH_HOST` | No | `http://localhost:9200` | Elasticsearch URI |
| `MAIL_HOST` | No | `smtp.gmail.com` | SMTP host |
| `MAIL_PORT` | No | `587` | SMTP port |
| `MAIL_USERNAME` | No | — | SMTP username |
| `MAIL_PASSWORD` | No | — | SMTP password / app password |
| `APP_CORS_ALLOWED_ORIGINS` | No | `http://localhost:3000,http://localhost:5173` | Comma-separated list of allowed CORS origins |

---

## Setup & Running Locally

### Prerequisites
- Java 21+
- Maven 3.8+
- MongoDB 5.0+ (running locally or via Docker)
- Redis (optional, for caching)

### Install & Build
```bash
git clone <repo-url>
cd getset-backend

# Copy and fill in environment variables
cp .env.example .env

# Build
mvn clean package -DskipTests
```

### Run
```bash
# Set required env var
export JWT_SECRET=<your-base64-encoded-secret>

mvn spring-boot:run
```

Or with the `dev` profile for verbose debug logging:
```bash
mvn spring-boot:run -Dspring.profiles.active=dev
```

Server starts at: `http://localhost:8080`

---

## Running with Docker

```bash
# Build image and start all services (MongoDB, Redis, app)
docker compose up --build

# Stop
docker compose down
```

The `docker-compose.yml` includes:
- `getset-backend` — the Spring Boot application
- `mongodb` — MongoDB 7 with a health check
- `redis` — Redis 7

> **Note**: Set `JWT_SECRET` in your environment or in a `.env` file before running Docker Compose.

---

## Spring Profiles

| Profile | Logging | Use Case |
|---|---|---|
| _(default)_ | INFO / WARN | General use |
| `dev` | DEBUG / TRACE | Local development |
| `prod` | WARN | Production deployment |

Activate a profile with:
```bash
# Maven
mvn spring-boot:run -Dspring.profiles.active=prod

# Docker / System property
SPRING_PROFILES_ACTIVE=prod
```

---

## API Documentation

Once the server is running:

| Resource | URL |
|---|---|
| Swagger UI | http://localhost:8080/api/v1/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/api/v1/api-docs |

All endpoints are annotated with `@Tag` and `@Operation` descriptions.

---

## Database Schema

### Users
```json
{ "_id": ObjectId, "name": "string", "email": "string (unique)", "password": "bcrypt hash",
  "role": "RENTER|OWNER|ADMIN", "phone": "string", "createdAt": ISODate, "updatedAt": ISODate }
```

### Properties
```json
{ "_id": ObjectId, "ownerId": ObjectId, "title": "string", "description": "string",
  "type": "APARTMENT|HOUSE|PG|VILLA", "pricePerMonth": number, "bedrooms": number,
  "bathrooms": number, "furnished": boolean, "amenities": ["string"],
  "address": { "fullAddress": "string", "city": "string", "state": "string",
                "country": "string", "pincode": "string" },
  "location": { "type": "Point", "coordinates": [lng, lat] },
  "photos": ["url"], "isActive": boolean, "createdAt": ISODate, "updatedAt": ISODate }
```

### Enquiries
```json
{ "_id": ObjectId, "propertyId": ObjectId, "renterId": ObjectId, "ownerId": ObjectId,
  "message": "string", "status": "PENDING|ACCEPTED|REJECTED", "rejectionReason": "string",
  "createdAt": ISODate, "updatedAt": ISODate }
```

### Favorites
```json
{ "_id": ObjectId, "renterId": ObjectId, "propertyId": ObjectId,
  "notes": "string", "createdAt": ISODate }
```

### Messages
```json
{ "_id": ObjectId, "senderId": ObjectId, "senderName": "string", "senderEmail": "string",
  "recipientId": ObjectId, "recipientName": "string", "recipientEmail": "string",
  "propertyId": ObjectId, "enquiryId": ObjectId, "content": "string",
  "read": boolean, "threadId": "string", "createdAt": ISODate }
```

### Notifications
```json
{ "_id": ObjectId, "recipientId": ObjectId, "recipientEmail": "string",
  "subject": "string", "body": "string",
  "type": "ENQUIRY_RECEIVED|ENQUIRY_ACCEPTED|ENQUIRY_REJECTED|MESSAGE_RECEIVED",
  "relatedEntityId": "string", "read": boolean,
  "emailSent": boolean, "emailSentError": "string", "createdAt": ISODate }
```

---

## Resilience & Rate Limiting

Configured via `application.yml` using **Resilience4j**:

| Pattern | Scope | Config |
|---|---|---|
| **Rate Limiter** | Auth endpoints | 10 requests / minute |
| **Retry** | DB operations | Max 3 attempts, 100ms wait |
| **Circuit Breaker** | User & Property repository | Opens at 50% failure rate |

---

## Security Model

- JWT access tokens expire in **24 hours**
- Refresh tokens expire in **7 days**
- Passwords hashed with **BCrypt**
- CORS origins configurable via `APP_CORS_ALLOWED_ORIGINS`
- The app **fails to start** if `JWT_SECRET` is not set (no hardcoded fallback)
- Public endpoints: register, login, refresh, GET properties (read-only)
- All other endpoints require a valid Bearer token

---

## Testing

Run the full test suite:
```bash
mvn clean test
```

| Test Class | Coverage |
|---|---|
| `AuthControllerTest` | MockMvc — register, login (2 tests) |
| `AuthServiceTest` | register, login, refresh token, getCurrentUser (8 tests) |
| `PropertyServiceImplTest` | CRUD + auth guard (5 tests) |
| `JwtServiceTest` | Token generation, validation, expiry (5 tests) |
| **Total** | **20 tests** |

---

## Future Enhancements

- [ ] Reviews and ratings for properties
- [ ] Payment integration (Razorpay / Stripe)
- [ ] Kafka event streaming for notifications
- [ ] Admin dashboard and analytics
- [ ] WebSocket support for real-time messaging
- [ ] Property image upload to S3/Cloudinary
- [ ] Expanded test coverage (integration tests with Testcontainers)

---

## License

MIT License — See LICENSE file for details.
