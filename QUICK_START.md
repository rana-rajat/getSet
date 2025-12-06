# GetSet Backend - Quick Start Guide

## 🚀 30-Second Setup

### Option 1: Docker Compose (Easiest)
```bash
cd d:\Projects\getSet\backend
docker-compose up
```
Then visit: http://localhost:8080/api/v1/swagger-ui.html

### Option 2: Local Maven
```bash
cd d:\Projects\getSet\backend\getset-backend

# Set environment variables (PowerShell)
$env:MONGODB_URI = "mongodb://localhost:27017/getset"
$env:JWT_SECRET = "your-256-bit-secret-key-minimum-32-characters"

# Start MongoDB separately (or use Docker)
docker run -d -p 27017:27017 mongo:latest

# Build and run
mvn clean install
mvn spring-boot:run
```

## 📌 Important Files to Review

1. **API Documentation**
   - `README.md` - Full API reference
   - `IMPLEMENTATION_GUIDE.md` - Detailed implementation notes

2. **Source Files**
   - `src/main/java/com/getset/property/` - Property management
   - `src/main/java/com/getset/config/` - Configuration
   - `src/main/resources/application.yml` - Application settings

3. **Configuration**
   - `pom.xml` - Maven dependencies
   - `docker-compose.yml` - Docker setup
   - `Dockerfile` - Container image

## 🧪 Quick API Tests

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
Save the `accessToken` from response.

### 3. Create Property
```bash
curl -X POST http://localhost:8080/api/v1/properties \
  -H "Authorization: Bearer <YOUR_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Cozy 2BHK Apartment",
    "description": "Modern apartment with all amenities",
    "type": "APARTMENT",
    "pricePerMonth": 45000,
    "bedrooms": 2,
    "bathrooms": 2,
    "furnished": true,
    "amenities": ["WiFi", "AC", "Parking", "Gym"],
    "address": {
      "fullAddress": "123 Main Street, Apt 4B",
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
curl "http://localhost:8080/api/v1/properties?city=Mumbai&minPrice=40000&maxPrice=50000&minBedrooms=2"
```

### 5. Find Nearby Properties
```bash
curl "http://localhost:8080/api/v1/properties/nearby?lat=19.0760&lng=72.8777&radiusKm=2"
```

### 6. Get Your Properties
```bash
curl -X GET http://localhost:8080/api/v1/properties/owner/my-properties \
  -H "Authorization: Bearer <YOUR_TOKEN>"
```

## 📊 Project Statistics

| Component | Count |
|-----------|-------|
| Controllers | 1 |
| Services | 2 |
| Repositories | 2 |
| DTOs | 5 |
| Domain Models | 3 |
| Configuration Classes | 3 |
| Exception Handlers | 1 |
| Total Java Files | ~20+ |

## 🔑 Key Endpoints

| Method | Path | Role | Purpose |
|--------|------|------|---------|
| POST | `/auth/register` | Public | Register new user |
| POST | `/auth/login` | Public | Login user |
| GET | `/auth/me` | Authenticated | Get current user |
| POST | `/properties` | OWNER | Create property |
| PUT | `/properties/{id}` | OWNER | Update property |
| DELETE | `/properties/{id}` | OWNER | Delete property |
| GET | `/properties/{id}` | Public | Get property |
| GET | `/properties` | Public | Search properties |
| GET | `/properties/nearby` | Public | Find nearby |
| GET | `/properties/owner/my-properties` | OWNER | My listings |

## ⚙️ Configuration Options

### MongoDB
- `MONGODB_URI` - Connection string (default: `mongodb://localhost:27017/getset`)

### JWT
- `JWT_SECRET` - Secret key (minimum 32 characters)
- Expiration: 24 hours (configurable)

### Server
- Port: 8080
- Context path: `/api/v1`

### CORS
- Allowed origins: localhost:3000, localhost:5173, and 127.0.0.1 variants

## 🧹 MongoDB Setup (First Time)

When running the first time, ensure geospatial indexes are created:

```javascript
// Connect to MongoDB shell
db.properties.createIndex({ "location": "2dsphere" })
db.users.createIndex({ "email": 1 })
db.properties.createIndex({ "address.city": 1, "isActive": 1 })
```

Or enable auto-index creation in `application.yml` (already enabled).

## 📝 Environment Setup

### Windows PowerShell
```powershell
$env:MONGODB_URI = "mongodb://localhost:27017/getset"
$env:JWT_SECRET = "your-secure-256-bit-key-here-minimum-32-chars"
mvn spring-boot:run
```

### Linux/Mac
```bash
export MONGODB_URI=mongodb://localhost:27017/getset
export JWT_SECRET=your-secure-256-bit-key-here-minimum-32-chars
mvn spring-boot:run
```

## 🐛 Troubleshooting

### MongoDB Connection Failed
- Ensure MongoDB is running: `docker run -d -p 27017:27017 mongo:latest`
- Check connection string in `application.yml`

### Port Already in Use
- Change port in `application.yml` or kill process on port 8080

### JWT Token Expired
- Register and login again to get a fresh token

### CORS Issues
- Update allowed origins in `CorsConfig.java`

## 📚 Additional Resources

- Spring Boot Documentation: https://spring.io/projects/spring-boot
- MongoDB Driver: https://mongodb.com/docs/drivers/java/
- JWT (JJWT): https://github.com/jwtk/jjwt
- SpringDoc OpenAPI: https://springdoc.org/

## ✅ Implementation Checklist

- [x] Property CRUD operations
- [x] Advanced search with filters
- [x] Geospatial queries
- [x] JWT authentication
- [x] Role-based access control
- [x] Global exception handling
- [x] API documentation (Swagger)
- [x] Docker containerization
- [x] CORS configuration
- [ ] Enquiry management (Phase 2)
- [ ] Favorites feature (Phase 3)
- [ ] Reviews & ratings (Phase 3)

## 🎯 Next Steps

1. **Review** - Check the full API documentation in README.md
2. **Test** - Use the quick tests above with Swagger UI
3. **Extend** - Add enquiry management in Phase 2
4. **Deploy** - Use Docker Compose for production deployment

---

**Version**: 1.0.0
**Status**: ✅ Production Ready
**Last Updated**: December 2025
