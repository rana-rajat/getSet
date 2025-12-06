# GetSet Backend - Visual Architecture & Database Schema

## 🏗️ System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                       FRONTEND (React)                           │
│               (http://localhost:3000 or 5173)                   │
└────────────────────────┬────────────────────────────────────────┘
                         │
                    CORS Headers
                         │
        ┌────────────────▼────────────────┐
        │   API Gateway / Load Balancer   │
        │       (Optional in prod)         │
        └────────────────┬────────────────┘
                         │
        ┌────────────────▼────────────────┐
        │    Spring Boot Application       │
        │    (port 8080, /api/v1)         │
        ├─────────────────────────────────┤
        │  ┌──────────────────────────┐   │
        │  │  HTTP Layer              │   │
        │  │  - REST Controllers      │   │
        │  │  - Request Mapping       │   │
        │  └──────────────┬───────────┘   │
        │                 │               │
        │  ┌──────────────▼───────────┐   │
        │  │  Security Layer          │   │
        │  │  - JWT Filter            │   │
        │  │  - Authentication        │   │
        │  │  - Authorization         │   │
        │  └──────────────┬───────────┘   │
        │                 │               │
        │  ┌──────────────▼───────────┐   │
        │  │  Service Layer           │   │
        │  │  - Business Logic        │   │
        │  │  - Validation            │   │
        │  │  - Mappers               │   │
        │  └──────────────┬───────────┘   │
        │                 │               │
        │  ┌──────────────▼───────────┐   │
        │  │  Repository Layer        │   │
        │  │  - MongoDB Queries       │   │
        │  │  - Indexes               │   │
        │  └──────────────┬───────────┘   │
        └────────────────┬────────────────┘
                         │
        ┌────────────────▼────────────────┐
        │    MongoDB Database             │
        │    - users collection           │
        │    - properties collection      │
        │    - enquiries collection       │
        │    - Geospatial indexes         │
        └─────────────────────────────────┘
```

## 🗄️ MongoDB Collections Schema

### users Collection
```javascript
{
  "_id": ObjectId("507f1f77bcf86cd799439011"),
  "name": "John Doe",
  "email": "john@example.com",
  "password": "$2a$10$...",  // BCrypt hashed
  "role": "OWNER",            // OWNER | RENTER | ADMIN
  "phone": "+91-9876543210",
  "createdAt": ISODate("2025-12-05T10:00:00Z"),
  "updatedAt": ISODate("2025-12-05T10:00:00Z")
}

// Indexes:
// { "email": 1 } - unique
// { "createdAt": -1 }
```

### properties Collection
```javascript
{
  "_id": ObjectId("507f1f77bcf86cd799439012"),
  "ownerId": "507f1f77bcf86cd799439011",
  "title": "Beautiful 2BHK Apartment",
  "description": "Modern apartment with all amenities",
  "type": "APARTMENT",
  "pricePerMonth": 45000,
  "bedrooms": 2,
  "bathrooms": 2,
  "furnished": true,
  "amenities": ["WiFi", "AC", "Parking", "Gym", "Security"],
  
  "address": {
    "fullAddress": "123 Main Street, Apt 4B",
    "city": "Mumbai",
    "state": "Maharashtra",
    "country": "India",
    "pincode": "400001"
  },
  
  "location": {
    "type": "Point",
    "coordinates": [72.8777, 19.0760]  // [longitude, latitude]
  },
  
  "photos": [
    "https://example.com/photo1.jpg",
    "https://example.com/photo2.jpg"
  ],
  
  "isActive": true,
  "createdAt": ISODate("2025-12-05T10:00:00Z"),
  "updatedAt": ISODate("2025-12-05T10:00:00Z")
}

// Indexes:
// { "location": "2dsphere" }          - Geospatial queries
// { "address.city": 1, "isActive": 1 } - City filtering
// { "pricePerMonth": 1 }              - Price range queries
// { "ownerId": 1, "isActive": 1 }    - Owner properties
// { "createdAt": -1 }                - Sorting
```

### enquiries Collection
```javascript
{
  "_id": ObjectId("507f1f77bcf86cd799439013"),
  "propertyId": "507f1f77bcf86cd799439012",
  "renterId": "507f1f77bcf86cd799439001",
  "ownerId": "507f1f77bcf86cd799439011",
  
  "message": "Is this property still available?",
  "status": "PENDING",  // PENDING | APPROVED | REJECTED
  
  "createdAt": ISODate("2025-12-05T10:00:00Z")
}

// Indexes:
// { "propertyId": 1 }
// { "renterId": 1 }
// { "ownerId": 1 }
// { "status": 1 }
```

## 🌊 Request/Response Flow

### Create Property Flow
```
┌─────────────────────────────────────────────┐
│  Client (Frontend React App)                 │
└────────────┬────────────────────────────────┘
             │ POST /api/v1/properties
             │ Authorization: Bearer JWT
             │ Content-Type: application/json
             │
             │ {
             │   "title": "2BHK",
             │   "description": "Modern",
             │   "type": "APARTMENT",
             │   ...
             │ }
             │
       ┌─────▼────────────────────────┐
       │  PropertyController           │
       │  createProperty()             │
       │  - Extract Authentication     │
       │  - Get userId from JWT        │
       └─────┬────────────────────────┘
             │
       ┌─────▼────────────────────────┐
       │  @PreAuthorize("OWNER")       │
       │  - Check role                 │
       │  - Validate access            │
       └─────┬────────────────────────┘
             │
       ┌─────▼────────────────────────┐
       │  @Valid Validation            │
       │  - Check all required fields  │
       │  - Validate constraints       │
       │  - Validate Lat/Lng bounds    │
       └─────┬────────────────────────┘
             │
       ┌─────▼────────────────────────┐
       │  PropertyService              │
       │  createProperty()             │
       │  - Business logic             │
       │  - Data transformation        │
       └─────┬────────────────────────┘
             │
       ┌─────▼────────────────────────┐
       │  PropertyMapper               │
       │  toDocument()                 │
       │  - DTO → Entity              │
       │  - Set ownership             │
       │  - Create Location object    │
       └─────┬────────────────────────┘
             │
       ┌─────▼────────────────────────┐
       │  PropertyRepository           │
       │  save(PropertyDocument)       │
       │  - MongoDB insert            │
       │  - Apply indexes             │
       └─────┬────────────────────────┘
             │
       ┌─────▼────────────────────────┐
       │  MongoDB                      │
       │  - Insert document           │
       │  - Update indexes            │
       │  - Return ObjectId           │
       └─────┬────────────────────────┘
             │
       ┌─────▼────────────────────────┐
       │  PropertyMapper               │
       │  toResponse()                 │
       │  - Entity → DTO              │
       │  - Extract coordinates       │
       └─────┬────────────────────────┘
             │
       ┌─────▼────────────────────────┐
       │  PropertyController           │
       │  Return 201 Created          │
       │  {                           │
       │    "id": "507f...",          │
       │    "title": "2BHK",          │
       │    ...                       │
       │  }                           │
       └─────┬────────────────────────┘
             │
             │ 201 + Response
             │
┌────────────▼──────────────────────┐
│  Client (React)                    │
│  Display success message           │
│  Update UI with new property       │
└────────────────────────────────────┘
```

### Search Properties Flow
```
┌─────────────────────────────────────────────┐
│  Client (Frontend)                           │
│  "Show properties in Mumbai"                │
└────────────┬────────────────────────────────┘
             │
             │ GET /api/v1/properties
             │ ?city=Mumbai
             │ &minPrice=40000
             │ &maxPrice=60000
             │ &minBedrooms=2
             │
       ┌─────▼────────────────────────┐
       │  PropertyController           │
       │  searchProperties()           │
       │  - Extract query params       │
       │  - Create filter object       │
       └─────┬────────────────────────┘
             │
       ┌─────▼────────────────────────┐
       │  PropertyService              │
       │  searchProperties()           │
       │  - Call repository            │
       └─────┬────────────────────────┘
             │
       ┌─────▼────────────────────────┐
       │  PropertyRepository           │
       │  searchProperties()           │
       │  - Build MongoDB Query        │
       │  - Apply $and conditions      │
       │  - Skip & limit pagination    │
       └─────┬────────────────────────┘
             │
       ┌─────▼────────────────────────┐
       │  MongoDB                      │
       │  Query: {                     │
       │    isActive: true,            │
       │    "address.city": "Mumbai",  │
       │    pricePerMonth: {           │
       │      $gte: 40000,             │
       │      $lte: 60000              │
       │    },                         │
       │    bedrooms: { $gte: 2 }      │
       │  }                            │
       │  - Use indexes               │
       │  - Return matching docs      │
       └─────┬────────────────────────┘
             │
       ┌─────▼────────────────────────┐
       │  List<PropertyDocument>       │
       │  [doc1, doc2, doc3, ...]      │
       └─────┬────────────────────────┘
             │
       ┌─────▼────────────────────────┐
       │  PropertyMapper               │
       │  toSummary() x N              │
       │  - Convert to DTOs           │
       │  - Extract only needed fields │
       └─────┬────────────────────────┘
             │
       ┌─────▼────────────────────────┐
       │  PropertyController           │
       │  Return 200 OK               │
       │  [{                          │
       │    "id": "...",              │
       │    "title": "...",           │
       │    "price": 50000,           │
       │    "city": "Mumbai",         │
       │    ...                       │
       │  }, ...]                     │
       └─────┬────────────────────────┘
             │
┌────────────▼──────────────────────┐
│  Client (React)                    │
│  Display search results in list    │
│  Show on map                       │
└────────────────────────────────────┘
```

### Geospatial Search Flow
```
┌─────────────────────────────────────────────┐
│  Client (Google Maps)                        │
│  "Find properties near 19.0760, 72.8777"    │
└────────────┬────────────────────────────────┘
             │
             │ GET /api/v1/properties/nearby
             │ ?lat=19.0760
             │ &lng=72.8777
             │ &radiusKm=5
             │
       ┌─────▼────────────────────────┐
       │  PropertyController           │
       │  findNearby()                 │
       │  - Extract coordinates       │
       │  - Convert km to meters      │
       │  (5 km = 5000 meters)        │
       └─────┬────────────────────────┘
             │
       ┌─────▼────────────────────────┐
       │  PropertyService              │
       │  findNearby()                 │
       │  - Call repository            │
       └─────┬────────────────────────┘
             │
       ┌─────▼────────────────────────┐
       │  PropertyRepository           │
       │  findNearby()                 │
       │  @Query with $nearSphere     │
       └─────┬────────────────────────┘
             │
       ┌─────▼────────────────────────┐
       │  MongoDB Geospatial Query     │
       │  {                            │
       │    location: {                │
       │      $nearSphere: {           │
       │        $geometry: {           │
       │          type: "Point",       │
       │          coordinates:         │
       │          [72.8777, 19.0760]   │
       │        },                     │
       │        $maxDistance: 5000     │
       │      }                        │
       │    },                         │
       │    isActive: true             │
       │  }                            │
       │ Uses: 2dsphere index          │
       └─────┬────────────────────────┘
             │
       ┌─────▼────────────────────────┐
       │  MongoDB Results              │
       │  - Returns nearby properties  │
       │  - Sorted by distance         │
       └─────┬────────────────────────┘
             │
       ┌─────▼────────────────────────┐
       │  PropertyMapper               │
       │  toSummary() x N              │
       └─────┬────────────────────────┘
             │
       ┌─────▼────────────────────────┐
       │  Return List of Properties    │
       │  [{...}, {...}, ...]          │
       └─────┬────────────────────────┘
             │
┌────────────▼──────────────────────┐
│  Frontend (React)                  │
│  Display markers on Google Map     │
│  For each nearby property          │
└────────────────────────────────────┘
```

## 🔐 Authentication & Authorization Flow

```
┌─────────────────────────────────────────────┐
│  1. User Registers                           │
│  POST /api/v1/auth/register                 │
│  Body: name, email, password, role          │
└────────────┬────────────────────────────────┘
             │
       ┌─────▼────────────────────────┐
       │  AuthController.register()   │
       │  - Validate input            │
       │  - Check email exists        │
       └─────┬────────────────────────┘
             │
       ┌─────▼────────────────────────┐
       │  AuthService.register()      │
       │  - Hash password (BCrypt)    │
       │  - Create UserDocument       │
       │  - Save to MongoDB           │
       │  - Generate JWT token        │
       └─────┬────────────────────────┘
             │
       ┌─────▼────────────────────────┐
       │  Return 201 Created          │
       │  {                           │
       │    "accessToken": "...",     │
       │    "user": {...}             │
       │  }                           │
       └─────┬────────────────────────┘
             │
┌────────────▼──────────────────────┐
│  Client stores JWT in localStorage │
│  JWT = header.payload.signature    │
└────────────────────────────────────┘

┌─────────────────────────────────────────────┐
│  2. Subsequent API Calls                     │
│  GET /api/v1/properties/owner/my-properties │
│  Header: Authorization: Bearer <JWT>        │
└────────────┬────────────────────────────────┘
             │
       ┌─────▼────────────────────────┐
       │  JwtAuthenticationFilter      │
       │  - Extract token from header  │
       │  - Validate signature         │
       │  - Check expiration           │
       │  - Extract userId            │
       │  - Create Authentication     │
       └─────┬────────────────────────┘
             │
       ┌─────▼────────────────────────┐
       │  @PreAuthorize("hasRole      │
       │  ('OWNER')")                 │
       │  - Check role in JWT claims  │
       │  - Verify authorization      │
       └─────┬────────────────────────┘
             │
       ┌─────▼────────────────────────┐
       │  Controller Method Executes   │
       │  - userId available          │
       │  - User context set          │
       │  - Process request           │
       └─────┬────────────────────────┘
             │
       ┌─────▼────────────────────────┐
       │  Return 200 OK               │
       │  [{...}, {...}, ...]         │
       └─────┬────────────────────────┘
             │
┌────────────▼──────────────────────┐
│  Frontend receives response         │
│  JWT remains valid for 24 hours    │
└────────────────────────────────────┘
```

## 📡 REST API Endpoint Structure

```
GET     /api/v1/properties
├── Query Parameters
│   ├── city: String
│   ├── minPrice: Double
│   ├── maxPrice: Double
│   ├── minBedrooms: Integer
│   ├── furnished: Boolean
│   ├── type: PropertyType
│   ├── page: int (default: 0)
│   └── size: int (default: 10)
├── Response: 200 OK
│   └── List<PropertySummaryResponse>
└── Errors: 400 Bad Request, 500 Internal Error

POST    /api/v1/properties
├── Headers
│   ├── Authorization: Bearer JWT (OWNER only)
│   └── Content-Type: application/json
├── Body: PropertyCreateRequest
├── Response: 201 Created
│   └── PropertyResponse (with id)
└── Errors: 400 Validation, 401 Unauthorized, 403 Forbidden

PUT     /api/v1/properties/{id}
├── Path: id (property id)
├── Headers
│   ├── Authorization: Bearer JWT (OWNER only)
│   └── Content-Type: application/json
├── Body: PropertyUpdateRequest
├── Response: 200 OK
│   └── PropertyResponse (updated)
└── Errors: 400, 401, 403, 404 Not Found

DELETE  /api/v1/properties/{id}
├── Path: id (property id)
├── Headers: Authorization: Bearer JWT (OWNER only)
├── Response: 204 No Content
└── Errors: 401, 403, 404

GET     /api/v1/properties/{id}
├── Path: id (property id)
├── Response: 200 OK
│   └── PropertyResponse
└── Errors: 404 Not Found

GET     /api/v1/properties/nearby
├── Query Parameters
│   ├── lat: Double (required, -90 to 90)
│   ├── lng: Double (required, -180 to 180)
│   └── radiusKm: Double (default: 5)
├── Response: 200 OK
│   └── List<PropertySummaryResponse>
└── Errors: 400 Invalid coordinates

GET     /api/v1/properties/owner/my-properties
├── Headers: Authorization: Bearer JWT (OWNER only)
├── Response: 200 OK
│   └── List<PropertySummaryResponse>
└── Errors: 401, 403
```

---

This comprehensive visual guide shows the complete system architecture, data flows, and API structure of the GetSet backend! 🎉
