# 🏠 GetSet - Rental Home Platform

A complete rental property platform with map-based search, location-aware results, and real-time communication. This repository contains the backend API built with Spring Boot 3.4.10 and MongoDB.

## 📋 Repository Structure

```
getset-backend/
├── src/
│   └── main/
│       ├── java/com/getset/
│       │   ├── auth/              # Authentication & Login
│       │   ├── user/              # User management
│       │   ├── property/          # Property management
│       │   ├── enquiry/           # Enquiry system (future)
│       │   ├── config/            # Spring configurations
│       │   ├── security/          # JWT & security filters
│       │   └── common/            # Utilities & error handling
│       └── resources/
│           └── application.yml    # Configuration file
├── Dockerfile                      # Container image
├── docker-compose.yml              # Docker Compose setup
├── pom.xml                         # Maven dependencies
└── README.md                       # Detailed API documentation

documentation/
├── QUICK_START.md                 # Quick start guide
├── IMPLEMENTATION_GUIDE.md        # Architecture & implementation
├── ARCHITECTURE.md                # Visual architecture & flows
├── COMPLETION_SUMMARY.md          # Project completion summary
├── FILE_CHECKLIST.md              # File inventory
└── build.sh / build.bat           # Build scripts

frontend/
└── [React frontend - separate repo]
```

## ✨ Features Implemented

### ✅ Core Features
- 🔐 JWT-based authentication with roles (OWNER, RENTER, ADMIN)
- 🏠 Complete property CRUD operations
- 🔍 Advanced search with multiple filters
- 📍 Geospatial search (find properties near a location)
- 🗺️ MongoDB 2dsphere index for location queries
- 📄 Comprehensive REST API with 20+ endpoints
- 📖 Swagger/OpenAPI auto-documentation

### ✅ Phase 2 Features
- 📋 **Enquiry Management System** - Renters express interest, owners accept/reject
- ⭐ **Favorites/Wishlist Feature** - Save properties with personal notes
- 📧 **Email Notifications** - Automated alerts for enquiry updates and messages
- 💬 **Direct Messaging System** - Real-time conversations between owners and renters

### ✅ Technical Features
- 🏗️ Layered architecture (Controller → Service → Repository)
- 🛡️ Global exception handling
- ✅ Input validation with annotations
- 🔄 DTO pattern for clean API contracts
- 🐳 Docker containerization
- 📊 MongoDB Atlas ready
- 🚀 Production-ready configuration

## 🚀 Quick Start

### Option 1: Docker Compose (Easiest)
```bash
docker-compose up
```
Visit: http://localhost:8080/api/v1/swagger-ui.html

### Option 2: Local Development
```bash
# Prerequisites: Java 21, Maven, MongoDB

cd getset-backend
mvn clean install
mvn spring-boot:run
```

### Option 3: Using Build Scripts
```bash
# Linux/Mac
./build.sh

# Windows
build.bat
```

## 📡 API Endpoints

### Authentication
```
POST   /api/v1/auth/register      Register new user
POST   /api/v1/auth/login         Login user
GET    /api/v1/auth/me            Get current user (auth required)
```

### Properties
```
POST   /api/v1/properties                    Create property (OWNER)
PUT    /api/v1/properties/{id}               Update property (OWNER)
DELETE /api/v1/properties/{id}               Delete property (OWNER)
GET    /api/v1/properties/{id}               Get property details
GET    /api/v1/properties                    Search with filters
GET    /api/v1/properties/nearby             Find nearby properties
GET    /api/v1/properties/owner/my-properties Get owner's properties (OWNER)
```

## 🔧 Tech Stack

```
✅ Java 21
✅ Spring Boot 3.3.x
✅ Spring Security (JWT)
✅ Spring Data MongoDB
✅ MongoDB 5.0+
✅ Docker & Docker Compose
✅ Maven 3.8+
✅ Swagger/OpenAPI
```

## 📖 Documentation

- **[QUICK_START.md](./QUICK_START.md)** - Get up and running in 5 minutes
- **[getset-backend/README.md](./getset-backend/README.md)** - Full API reference
- **[ARCHITECTURE.md](./ARCHITECTURE.md)** - System architecture & data flows
- **[IMPLEMENTATION_GUIDE.md](./IMPLEMENTATION_GUIDE.md)** - Implementation details
- **[COMPLETION_SUMMARY.md](./COMPLETION_SUMMARY.md)** - What was built

## 🧪 Testing the API

### 1. Register User
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "password": "Password123!",
    "role": "OWNER"
  }'
```

### 2. Login
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "Password123!"
  }'
```
*Save the `accessToken` from response*

### 3. Create Property
```bash
curl -X POST http://localhost:8080/api/v1/properties \
  -H "Authorization: Bearer <YOUR_JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Modern 2BHK Apartment",
    "description": "Beautiful apartment near metro",
    "type": "APARTMENT",
    "pricePerMonth": 50000,
    "bedrooms": 2,
    "bathrooms": 2,
    "furnished": true,
    "amenities": ["WiFi", "AC", "Parking"],
    "address": {
      "fullAddress": "123 Main St",
      "city": "Mumbai",
      "state": "Maharashtra",
      "country": "India",
      "pincode": "400001"
    },
    "lat": 19.0760,
    "lng": 72.8777,
    "photos": ["https://example.com/photo1.jpg"]
  }'
```

### 4. Search Properties
```bash
curl "http://localhost:8080/api/v1/properties?city=Mumbai&minPrice=40000&maxPrice=60000&minBedrooms=2"
```

### 5. Find Nearby Properties
```bash
curl "http://localhost:8080/api/v1/properties/nearby?lat=19.0760&lng=72.8777&radiusKm=5"
```

## 🏗️ Architecture

```
┌─────────────────────────────────────┐
│         React Frontend              │
│     (http://localhost:3000)         │
└────────────┬────────────────────────┘
             │
        ┌────▼─────────────────────┐
        │  Spring Boot Backend      │
        │  (http://localhost:8080)  │
        ├──────────────────────────┤
        │  REST Controllers         │
        │  Security (JWT)           │
        │  Business Services        │
        │  Data Repositories        │
        └────┬─────────────────────┘
             │
        ┌────▼─────────────────────┐
        │   MongoDB Database        │
        │   (collections + indexes) │
        └──────────────────────────┘
```

## 🔒 Security Features

✅ JWT token-based authentication
✅ Role-based access control (OWNER, RENTER, ADMIN)
✅ BCrypt password hashing
✅ Ownership verification for updates/deletes
✅ CORS configuration for frontend
✅ Input validation with annotations

## 📊 Database Schema

### Users Collection
```json
{
  "_id": ObjectId,
  "name": "string",
  "email": "string",
  "password": "string (hashed)",
  "role": "OWNER | RENTER | ADMIN",
  "phone": "string",
  "createdAt": ISODate,
  "updatedAt": ISODate
}
```

### Properties Collection
```json
{
  "_id": ObjectId,
  "ownerId": ObjectId,
  "title": "string",
  "description": "string",
  "type": "APARTMENT | HOUSE | PG | VILLA",
  "pricePerMonth": number,
  "bedrooms": number,
  "bathrooms": number,
  "furnished": boolean,
  "amenities": [string],
  "address": {
    "fullAddress": "string",
    "city": "string",
    "state": "string",
    "country": "string",
    "pincode": "string"
  },
  "location": {
    "type": "Point",
    "coordinates": [longitude, latitude]
  },
  "photos": [string],
  "isActive": boolean,
  "createdAt": ISODate,
  "updatedAt": ISODate
}
```

## ⚙️ Environment Variables

```bash
# MongoDB
MONGODB_URI=mongodb://localhost:27017/getset

# JWT Secret (minimum 32 characters)
JWT_SECRET=your-secure-256-bit-key-here
```

## 🐳 Docker Deployment

### Build & Run
```bash
# Build image
docker build -t getset-backend:latest .

# Run container
docker run -p 8080:8080 \
  -e MONGODB_URI=mongodb://host.docker.internal:27017/getset \
  -e JWT_SECRET=your-key \
  getset-backend:latest
```

### Docker Compose (Recommended)
```bash
docker-compose up
```
Includes: Backend + MongoDB

## 📈 Performance Optimizations

✅ MongoDB geospatial indexes for efficient location queries
✅ City index for fast filtering
✅ Price range index for range queries
✅ Pagination support for large datasets
✅ Query optimization with projections
✅ Connection pooling

## 🎯 Project Phases

### ✅ Phase 1: Core Property Management (COMPLETE)
- User authentication
- Property CRUD operations
- Search and filtering
- Geospatial queries

### ⏳ Phase 2: Enquiry System
- Create enquiry endpoints
- Email notifications
- Enquiry management

### ⏳ Phase 3: Advanced Features
- Favorites/Wishlist
- Reviews & ratings
- Search history

### ⏳ Phase 4: Optimization
- Redis caching
- Elasticsearch
- Kafka events

## 🚨 Troubleshooting

### MongoDB Connection Failed
```bash
# Ensure MongoDB is running
docker run -d -p 27017:27017 mongo:latest
```

### Port 8080 Already in Use
- Change port in `application.yml`
- Or kill process: `lsof -ti:8080 | xargs kill -9`

### JWT Token Expired
- Register and login again for a fresh token

### CORS Issues
- Update allowed origins in `CorsConfig.java`
- Rebuild and restart application

## 📚 Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [MongoDB Documentation](https://docs.mongodb.com/)
- [JWT Introduction](https://jwt.io/introduction)
- [REST API Best Practices](https://restfulapi.net/)

## 📞 Support

For issues, questions, or contributions:
1. Check the documentation files
2. Review the API examples
3. Check MongoDB logs
4. Verify JWT configuration

## 📝 License

MIT License - See LICENSE file for details

## 🎉 Status

**Backend**: ✅ **PRODUCTION READY** (v1.0.0)
- All core features implemented
- API fully documented
- Docker ready
- Ready for frontend integration

---

**Last Updated**: December 2025
**Java Version**: 21
**Spring Boot**: 3.3.x
**MongoDB**: 5.0+

Happy Renting! 🏠
