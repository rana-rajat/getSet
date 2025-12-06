# GetSet Backend - Implementation Guide

## ✅ Completed Components

### 1. **Property Management Module** ✓
- **PropertyDocument.java** - MongoDB domain model with geospatial support
- **PropertyType.java** - Enum for property types (APARTMENT, HOUSE, PG, VILLA)
- **Address.java** - Address embedded document
- **Location.java** - Geospatial location model with Point coordinates

### 2. **Property Repository Layer** ✓
- **PropertyRepository.java** - Spring Data MongoDB repository with custom queries
- **PropertyRepositoryCustom.java** - Custom repository interface for complex searches
- **PropertyRepositoryImpl.java** - Implementation with MongoTemplate for advanced queries
  - Supports filtering by city, price range, bedrooms, furnished status, type
  - Implements geospatial queries for nearby properties

### 3. **Property DTOs** ✓
- **AddressDto.java** - Address data transfer object with validation
- **PropertyCreateRequest.java** - Validated request for creating properties
- **PropertyUpdateRequest.java** - Validated request for updating properties
- **PropertyResponse.java** - Full property response with all details
- **PropertySummaryResponse.java** - Lightweight summary for search results

### 4. **Property Service Layer** ✓
- **PropertyService.java** - Interface defining property operations
- **PropertyServiceImpl.java** - Business logic implementation
  - Create property with ownership tracking
  - Update property with authorization checks
  - Soft delete (deactivate) properties
  - Search with multiple filters
  - Geospatial nearby search
  - Get owner's properties

### 5. **Property Mapper** ✓
- **PropertyMapper.java** - Converts between entities and DTOs
  - Maps create requests to documents
  - Maps documents to responses
  - Maps documents to summaries for list endpoints

### 6. **Property REST API** ✓
- **PropertyController.java** - REST endpoints
  - `POST /api/v1/properties` - Create property (OWNER)
  - `PUT /api/v1/properties/{id}` - Update property (OWNER)
  - `DELETE /api/v1/properties/{id}` - Deactivate property (OWNER)
  - `GET /api/v1/properties/{id}` - Get property details
  - `GET /api/v1/properties` - Search properties
  - `GET /api/v1/properties/nearby` - Find nearby properties
  - `GET /api/v1/properties/owner/my-properties` - Get owner's listings

### 7. **Enquiry Module (Future Phase)** ✓
- **EnquiryDocument.java** - MongoDB enquiry model
- **EnquiryStatus.java** - Enum for enquiry states (PENDING, APPROVED, REJECTED)
- **EnquiryRepository.java** - Repository for enquiry queries

### 8. **Security & Configuration** ✓
- **SecurityConfig.java** - Spring Security configuration
  - JWT token validation filter
  - Role-based access control (@PreAuthorize)
  - CORS integration
- **CorsConfig.java** - CORS configuration for frontend integration
- **OpenApiConfig.java** - Swagger/OpenAPI documentation setup

### 9. **Global Exception Handling** ✓
- **GlobalExceptionHandler.java** - Centralized exception handling
  - NotFoundException handling
  - AccessDeniedException handling
  - Validation error handling
  - Generic error handling
- **ApiError.java** - Standardized error response format
- **NotFoundException.java** - Custom exception for resource not found
- **PageResponse.java** - Paginated response wrapper

### 10. **Docker & Deployment** ✓
- **Dockerfile** - Container image for backend
- **docker-compose.yml** - Docker Compose setup with MongoDB

### 11. **Documentation** ✓
- **README.md** - Complete API documentation
- **application.yml** - Configuration file with environment support

## 🔄 Data Flow Example

### Create Property Flow
```
Client Request (PropertyCreateRequest)
    ↓
PropertyController.createProperty()
    ↓
Authentication Check (JWT)
    ↓
PropertyMapper.toDocument() [DTO → Entity]
    ↓
PropertyServiceImpl.createProperty() [Business Logic]
    ↓
PropertyRepository.save() [MongoDB]
    ↓
PropertyMapper.toResponse() [Entity → DTO]
    ↓
Client Response (PropertyResponse)
```

### Search Properties Flow
```
Client Request (Query Params: city, minPrice, etc.)
    ↓
PropertyController.searchProperties()
    ↓
PropertyRepositoryCustom.searchProperties() [MongoDB Query]
    ↓
MongoTemplate.find() [Filtered Results]
    ↓
PropertyMapper.toSummary() [Convert Multiple Documents]
    ↓
Client Response (List<PropertySummaryResponse>)
```

### Nearby Search Flow (Geospatial)
```
Client Request (lat, lng, radiusKm)
    ↓
PropertyController.findNearby()
    ↓
PropertyRepositoryCustom.findNearby() [Geospatial Query]
    ↓
MongoDB 2dsphere Index $nearSphere Query
    ↓
PropertyMapper.toSummary() [Convert Results]
    ↓
Client Response (List<PropertySummaryResponse>)
```

## 🛠️ MongoDB Indexes

Ensure these indexes exist for optimal performance:

```javascript
// User email index
db.users.createIndex({ "email": 1 })

// Property city index for filtering
db.properties.createIndex({ "address.city": 1, "isActive": 1 })

// Property price range index
db.properties.createIndex({ "pricePerMonth": 1 })

// Geospatial index for nearby search
db.properties.createIndex({ "location": "2dsphere" })

// Owner properties index
db.properties.createIndex({ "ownerId": 1, "isActive": 1 })
```

## 🔑 Key Features Implemented

### Search Capabilities
✓ Filter by multiple criteria (city, price, bedrooms, furnished, type)
✓ Pagination support
✓ Geospatial queries for nearby locations
✓ Soft delete for property lifecycle

### Security
✓ JWT-based authentication
✓ Role-based access control (OWNER, RENTER, ADMIN)
✓ Ownership verification for property updates/deletes
✓ CORS configuration for frontend

### Validation
✓ Input validation with Jakarta validation
✓ Latitude/Longitude bounds checking
✓ Nested object validation (Address)
✓ Required field validation

### Error Handling
✓ Centralized exception handler
✓ Standardized error response format
✓ Proper HTTP status codes
✓ Detailed error messages

## 📝 Configuration

### JWT Configuration
In `application.yml`:
```yaml
spring:
  jwt:
    secret-key: ${JWT_SECRET:...}
    expiration: 86400000  # 24 hours
```

### MongoDB Configuration
```yaml
spring:
  data:
    mongodb:
      uri: ${MONGODB_URI:mongodb://localhost:27017/getset}
      auto-index-creation: true
```

### CORS Configuration
Configured for local development:
- http://localhost:3000
- http://localhost:5173
- http://127.0.0.1:3000
- http://127.0.0.1:5173

## 🚀 Deployment

### Local Development
```bash
# Build
mvn clean package

# Run with Docker Compose
docker-compose up
```

### Production Deployment
1. Set environment variables: `MONGODB_URI`, `JWT_SECRET`
2. Update CORS origins in `CorsConfig.java`
3. Update API endpoint paths if needed
4. Build Docker image: `docker build -t getset-backend:latest .`
5. Push to registry and deploy

## 📚 API Request Examples

### Create Property
```bash
curl -X POST http://localhost:8080/api/v1/properties \
  -H "Authorization: Bearer <JWT_TOKEN>" \
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
      "fullAddress": "123 Main St, Apt 4B",
      "city": "New York",
      "state": "NY",
      "country": "USA",
      "pincode": "10001"
    },
    "lat": 40.7128,
    "lng": -74.0060,
    "photos": ["https://example.com/photo1.jpg"]
  }'
```

### Search Properties
```bash
curl "http://localhost:8080/api/v1/properties?city=New%20York&minPrice=30000&maxPrice=60000&minBedrooms=2&furnished=true&page=0&size=10"
```

### Find Nearby Properties
```bash
curl "http://localhost:8080/api/v1/properties/nearby?lat=40.7128&lng=-74.0060&radiusKm=5"
```

## 📊 Project Structure Summary

```
com.getset/
├── auth/                      # Authentication
├── user/                       # User domain
├── security/                   # JWT & security filters
├── property/                   # Property management
│   ├── dto/                   # DTOs for requests/responses
│   ├── PropertyController     # REST endpoints
│   ├── PropertyService        # Business logic
│   └── PropertyRepository     # Data access
├── enquiry/                    # Enquiry management (future)
├── config/                     # Spring configurations
├── common/                     # Shared utilities & error handling
└── GetSetApplication.java      # Main application class
```

## ✨ Next Steps

### Phase 2: Enquiry Management
- Implement EnquiryService and EnquiryController
- Add endpoints for creating/managing enquiries
- Email notifications for new enquiries

### Phase 3: Advanced Features
- Favorites/Wishlist feature
- Reviews and ratings system
- Search history tracking
- Payment integration

### Phase 4: Optimization
- Redis caching layer
- Elasticsearch for better search
- Kafka event streaming
- Admin dashboard backend

## 🎯 Testing Recommendations

1. **Unit Tests**: Test service layer logic
2. **Integration Tests**: Test repository and database interactions
3. **API Tests**: Test REST endpoints with different inputs
4. **Performance Tests**: Test geospatial queries with large datasets

Happy Building! 🚀
