# 🏠 GetSet — Rental Home Platform (Microservices Backend)

A production-grade rental property platform built with **Spring Boot 3 microservices**, MongoDB, Kafka, and Spring Cloud Gateway.

---

## 📖 Documentation Hub

| Document | Purpose |
|---|---|
| **[DEVELOPER_WALKTHROUGH.md](./DEVELOPER_WALKTHROUGH.md)** ⭐ | **Start here if you're new** — step-by-step guide through every service and class |
| **[PROJECT_DETAILS.md](./PROJECT_DETAILS.md)** | Complete class-level reference for every file in the project |
| **[.env.example](./.env.example)** | All required environment variables with descriptions |

---

## 🏗️ Architecture

```
Client (Browser/App)
        │
        ▼  Port 8090
┌──────────────────┐
│   API Gateway    │   Rate limiting · Circuit breakers · Routing
└────────┬─────────┘
         │
    ┌────┴──────────────────────────────────────────┐
    │                                               │
    ▼                                               ▼
user-service :8081           property-service :8082
enquiry-service :8083        favorite-service :8084
message-service :8085        notification-service :8086

Async (Kafka):  enquiry + message ──► notification-service ──► Email
Sync (Feign):   services call each other via /internal/** endpoints
```

### Services at a Glance

| Service | Port | Responsibility |
|---|---|---|
| `user-service` | 8081 | JWT auth, user registration/login, user data |
| `property-service` | 8082 | Property CRUD, geospatial search |
| `enquiry-service` | 8083 | Enquiry lifecycle (PENDING→ACCEPTED/REJECTED) |
| `favorite-service` | 8084 | Renter wishlist / saved properties |
| `message-service` | 8085 | Threaded conversations between owner and renter |
| `notification-service` | 8086 | Kafka consumer → email notifications |
| `api-gateway` | 8090 | Single entry point for all clients |
| `getset-common` | library | Shared DTOs, exceptions, Kafka event types |

---

## ✨ Features

### Core Platform
- 🔐 JWT auth with roles — `OWNER`, `RENTER`, `ADMIN`
- 🏠 Property CRUD with geospatial search (`2dsphere` MongoDB index)
- 📋 Enquiry workflow: submit → accept/reject (Kafka-driven notifications)
- ⭐ Favorites/Wishlist with personal notes
- 💬 Threaded direct messaging between owners and renters
- 📧 Email notifications for all key events (via SMTP + Kafka)

### Infrastructure
- 🌐 API Gateway with Redis rate limiting (20/min auth, 60/min APIs)
- 🔄 Circuit breakers per route (Resilience4j)
- 📨 Kafka async event pipeline (3-partition `notification-events` topic)
- 🔍 Distributed tracing with Zipkin
- 📊 Prometheus metrics + Grafana dashboards
- 🐳 Full Docker Compose stack (10 services)

---

## 🚀 Quick Start

### Prerequisites
- Java 21, Maven 3.8+, Docker Desktop

### 1. Start Infrastructure
```bash
docker-compose up -d
```

### 2. Build Shared Library (Required First!)
```bash
cd getset-common
mvn clean install
```

### 3. Start Services (in order)
```bash
# Start each in a separate terminal
cd user-service        && mvn spring-boot:run
cd property-service    && mvn spring-boot:run
cd enquiry-service     && mvn spring-boot:run
cd favorite-service    && mvn spring-boot:run
cd message-service     && mvn spring-boot:run
cd notification-service && mvn spring-boot:run
cd api-gateway         && mvn spring-boot:run   # last
```

### 4. Verify
Open: http://localhost:8090/actuator/health — should return `{"status":"UP"}`

---

## 📡 API Reference (All via Gateway — Port 8090)

### Authentication
```
POST   /api/v1/auth/register     Register (OWNER or RENTER)
POST   /api/v1/auth/login        Login → returns accessToken + refreshToken
POST   /api/v1/auth/refresh      Refresh access token
GET    /api/v1/auth/me           Current user profile
```

### Properties
```
POST   /api/v1/properties                      Create property (OWNER)
GET    /api/v1/properties/{id}                 Get property details
GET    /api/v1/properties                      Search with filters (city, price, bedrooms…)
GET    /api/v1/properties/nearby               Geospatial: ?lat=&lng=&radiusKm=
PUT    /api/v1/properties/{id}                 Update property (OWNER, ownership checked)
DELETE /api/v1/properties/{id}                 Delete property (OWNER, ownership checked)
GET    /api/v1/properties/owner/my-properties  Owner's listings
```

### Enquiries
```
POST   /api/v1/enquiries                       Submit enquiry (RENTER)
GET    /api/v1/enquiries/{id}                  Get enquiry
GET    /api/v1/enquiries/property/{propertyId} All enquiries for a property (OWNER)
GET    /api/v1/enquiries/renter/my-enquiries   Renter's enquiry history
PUT    /api/v1/enquiries/{id}                  Accept or reject (OWNER)
DELETE /api/v1/enquiries/{id}                  Cancel enquiry (RENTER)
```

### Favorites
```
POST   /api/v1/favorites/{propertyId}          Add to favorites (with optional ?notes=)
DELETE /api/v1/favorites/{propertyId}          Remove from favorites
GET    /api/v1/favorites                       Get all favorites
GET    /api/v1/favorites/check/{propertyId}    Is this property favorited?
GET    /api/v1/favorites/count                 Total favorite count
```

### Messages
```
POST   /api/v1/messages                        Send a message
GET    /api/v1/messages/thread/{threadId}      Full conversation thread
GET    /api/v1/messages/conversations          All conversation threads
GET    /api/v1/messages/unread                 Unread messages
GET    /api/v1/messages/unread/count           Unread count (for badge)
PUT    /api/v1/messages/{id}/read              Mark as read
PUT    /api/v1/messages/read-all               Mark all as read
```

### Notifications
```
GET    /api/v1/notifications                   All notifications
GET    /api/v1/notifications/unread            Unread only
GET    /api/v1/notifications/unread/count      Unread count
PUT    /api/v1/notifications/{id}/read         Mark as read
PUT    /api/v1/notifications/read-all          Mark all as read
DELETE /api/v1/notifications/{id}              Delete notification
```

---

## 🔧 Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.4.x / Spring Framework 6.2 |
| Security | Spring Security + JWT (HS256) |
| Database | MongoDB 5.0+ |
| Messaging | Apache Kafka (Zookeeper-managed) |
| Service Discovery | Spring Cloud OpenFeign (direct URL config) |
| API Gateway | Spring Cloud Gateway |
| Resilience | Resilience4j (circuit breakers, rate limiters) |
| Tracing | Micrometer + Zipkin |
| Metrics | Prometheus + Grafana |
| Email | Spring Mail (JavaMailSender) |
| Containerization | Docker + Docker Compose |

---

## ⚙️ Environment Variables

See `.env.example` for the full list. The most critical ones:

```bash
MONGODB_URI=mongodb://localhost:27017/getset
JWT_SECRET=your-256-bit-secret-same-across-all-services  # MUST match in every service!
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
MAIL_HOST=smtp.gmail.com
MAIL_USERNAME=your@gmail.com
MAIL_PASSWORD=your-app-password
```

> ⚠️ `JWT_SECRET` must be **identical** in every service's config. If mismatched, tokens signed by `user-service` will be rejected by all other services.

---

## 🐳 Infrastructure Services (Docker Compose)

| Service | Port | Purpose |
|---|---|---|
| MongoDB | 27017 | Primary database for all services |
| Zookeeper | 2181 | Kafka coordination |
| Kafka | 9092 | Async event bus |
| Zipkin | 9411 | Distributed request tracing |
| Prometheus | 9090 | Metrics scraping |
| Grafana | 3000 | Metrics dashboards |

---

## 📊 MongoDB Collections

| Collection | Owner Service | Key Fields |
|---|---|---|
| `users` | user-service | id, email, password (BCrypt), role, phone |
| `properties` | property-service | ownerId, title, type, price, address, location (GeoJSON) |
| `enquiries` | enquiry-service | renterId, ownerId, propertyId, status, message |
| `favorites` | favorite-service | renterId, propertyId, notes (denormalized property snapshot) |
| `messages` | message-service | threadId, senderId, recipientId, content, read |
| `notifications` | notification-service | recipientId, type, body, emailSent, read |

---

## 🚨 Troubleshooting

| Problem | Cause | Fix |
|---|---|---|
| `401` on all requests | JWT_SECRET mismatch | Ensure same secret in all `application.yml` |
| Feign `Connection refused` | Sibling service not started | Start `user-service` and `property-service` first |
| `getset-common` class not found | Library not installed | `cd getset-common && mvn clean install` |
| Kafka events not consumed | Kafka/Zookeeper unhealthy | `docker-compose restart kafka zookeeper` |
| IDE package declaration error | Wrong source root | VS Code: `Ctrl+Shift+P` → Java: Clean Workspace |

---

## 🔮 Future Roadmap

- Reviews & ratings system
- Payment integration (Razorpay/Stripe)
- Real-time WebSocket chat
- ML-based property recommendations
- Full-text Elasticsearch search service

---

**Status:** ✅ Microservices Architecture — Production Ready  
**Java:** 21 | **Spring Boot:** 3.4.x | **MongoDB:** 5.0+ | **Kafka:** 3.x  
**Last updated:** February 2026
