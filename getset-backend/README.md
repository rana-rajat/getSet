# GetSet Backend API

A Spring Boot 3.4.10 REST API for a rental home platform with map-based search, location-aware results, messaging, and enquiry management.

## Features

- **User Authentication**: JWT-based auth with roles (OWNER, RENTER, ADMIN)
- **Property Management**: Create, update, and manage rental listings
- **Advanced Search**: Filter by city, price, bedrooms, furnished status
- **Geo-spatial Search**: Find properties near a location using MongoDB geospatial indexes
- **Enquiry Management**: Property inquiries with acceptance/rejection workflow
- **Favorites/Wishlist**: Save favorite properties with personal notes
- **Direct Messaging**: Real-time conversation between owners and renters for properties they're interested in
- **Email Notifications**: Automatic email alerts for enquiries, messages, and status updates
- **Swagger UI**: Interactive API documentation

## Tech Stack

- **Language**: Java 21
- **Framework**: Spring Boot 3.4.10 with Spring Framework 6.2
- **Database**: MongoDB
- **Security**: Spring Security + JWT (JJWT)
- **Build**: Maven
- **Documentation**: SpringDoc OpenAPI (Swagger)
- **Email**: Spring Mail with JavaMailSender

## API Endpoints

### Authentication
- `POST /api/v1/auth/register` - Register new user
- `POST /api/v1/auth/login` - Login user
- `GET /api/v1/auth/me` - Get current user (requires JWT)

### Properties
- `POST /api/v1/properties` - Create property (OWNER only)
- `PUT /api/v1/properties/{id}` - Update property (OWNER only)
- `DELETE /api/v1/properties/{id}` - Deactivate property (OWNER only)
- `GET /api/v1/properties/{id}` - Get property details
- `GET /api/v1/properties` - Search properties with filters
  - Query params: `city`, `minPrice`, `maxPrice`, `minBedrooms`, `furnished`, `type`, `page`, `size`
- `GET /api/v1/properties/nearby` - Find nearby properties
  - Query params: `lat`, `lng`, `radiusKm` (default 5)
- `GET /api/v1/properties/owner/my-properties` - Get owner's properties (OWNER only)

### Enquiries
- `POST /api/v1/enquiries` - Create enquiry for a property (RENTER only)
- `GET /api/v1/enquiries/{id}` - Get enquiry details
- `GET /api/v1/enquiries/property/{propertyId}` - Get all enquiries for a property (OWNER only)
- `GET /api/v1/enquiries/renter/my-enquiries` - Get current renter's enquiries (RENTER only)
- `PUT /api/v1/enquiries/{id}` - Update enquiry status - accept/reject (OWNER only)
- `DELETE /api/v1/enquiries/{id}` - Cancel enquiry
- `GET /api/v1/enquiries/owner/stats` - Get enquiry statistics (OWNER only)

### Favorites/Wishlist
- `POST /api/v1/favorites` - Add property to favorites (RENTER only)
- `DELETE /api/v1/favorites/{propertyId}` - Remove from favorites (RENTER only)
- `GET /api/v1/favorites` - Get all favorites (RENTER only)
- `GET /api/v1/favorites/check/{propertyId}` - Check if property is favorited (RENTER only)
- `GET /api/v1/favorites/count` - Get favorite count (RENTER only)
- `PUT /api/v1/favorites/{propertyId}` - Update favorite notes (RENTER only)

### Messages
- `POST /api/v1/messages` - Send a message
- `GET /api/v1/messages/thread/{threadId}` - Get conversation thread messages
- `GET /api/v1/messages/received` - Get received messages
- `GET /api/v1/messages/sent` - Get sent messages
- `GET /api/v1/messages/unread` - Get unread messages
- `GET /api/v1/messages/unread/count` - Get unread message count
- `PUT /api/v1/messages/{id}/read` - Mark message as read
- `PUT /api/v1/messages/read-all` - Mark all messages as read
- `GET /api/v1/messages/conversation/{otherUserId}/{propertyId}` - Get conversation with specific user
- `GET /api/v1/messages/conversations` - Get all conversations
- `DELETE /api/v1/messages/{id}` - Delete message

### Notifications
- `GET /api/v1/notifications` - Get user notifications
- `GET /api/v1/notifications/unread` - Get unread notifications
- `GET /api/v1/notifications/unread/count` - Get unread notification count
- `PUT /api/v1/notifications/{id}/read` - Mark notification as read
- `PUT /api/v1/notifications/read-all` - Mark all notifications as read
- `DELETE /api/v1/notifications/{id}` - Delete notification

## Setup & Installation

### Prerequisites
- Java 21+
- Maven 3.8+
- MongoDB 5.0+

### Environment Variables
```bash
export MONGODB_URI=mongodb://localhost:27017/getset
export JWT_SECRET=your-256-bit-secret-key-minimum-32-characters
```

### Build
```bash
mvn clean package
```

### Run
```bash
mvn spring-boot:run
```

Server will start on `http://localhost:8080`

## API Documentation

Once running, visit:
- **Swagger UI**: http://localhost:8080/api/v1/swagger-ui.html
- **API Docs JSON**: http://localhost:8080/api/v1/api-docs

## Authentication

All protected endpoints require JWT token in Authorization header:
```
Authorization: Bearer <your_jwt_token>
```

## Database Schema

### Users Collection
```json
{
  "_id": ObjectId,
  "name": "string",
  "email": "string",
  "password": "string (hashed)",
  "role": "OWNER|RENTER|ADMIN",
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
  "type": "APARTMENT|HOUSE|PG|VILLA",
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

### Enquiries Collection
```json
{
  "_id": ObjectId,
  "propertyId": ObjectId,
  "renterId": ObjectId,
  "ownerId": ObjectId,
  "message": "string",
  "status": "PENDING|ACCEPTED|REJECTED",
  "rejectionReason": "string",
  "createdAt": ISODate,
  "updatedAt": ISODate
}
```

### Favorites Collection
```json
{
  "_id": ObjectId,
  "renterId": ObjectId,
  "propertyId": ObjectId,
  "notes": "string",
  "createdAt": ISODate
}
```

### Messages Collection
```json
{
  "_id": ObjectId,
  "senderId": ObjectId,
  "senderName": "string",
  "senderEmail": "string",
  "recipientId": ObjectId,
  "recipientName": "string",
  "recipientEmail": "string",
  "propertyId": ObjectId,
  "enquiryId": ObjectId,
  "content": "string",
  "read": boolean,
  "threadId": "string",
  "createdAt": ISODate
}
```

### Notifications Collection
```json
{
  "_id": ObjectId,
  "recipientId": ObjectId,
  "recipientEmail": "string",
  "subject": "string",
  "body": "string",
  "type": "ENQUIRY_RECEIVED|ENQUIRY_ACCEPTED|ENQUIRY_REJECTED|MESSAGE_RECEIVED",
  "relatedEntityId": "string",
  "read": boolean,
  "emailSent": boolean,
  "emailSentError": "string",
  "createdAt": ISODate
}
```

## Project Structure

```
src/main/java/com/getset/
├── GetSetApplication.java
├── config/
│   ├── SecurityConfig.java
│   ├── CorsConfig.java
│   └── OpenApiConfig.java
├── security/
│   ├── JwtService.java
│   └── JwtAuthenticationFilter.java
├── auth/
│   ├── AuthController.java
│   ├── AuthService.java
│   └── dto/
├── user/
│   ├── UserDocument.java
│   ├── Role.java
│   ├── UserRepository.java
│   └── UserMapper.java
├── property/
│   ├── PropertyDocument.java
│   ├── PropertyType.java
│   ├── PropertyController.java
│   ├── PropertyService.java
│   ├── PropertyServiceImpl.java
│   ├── PropertyRepository.java
│   └── dto/
├── enquiry/
│   ├── EnquiryDocument.java
│   ├── EnquiryRepository.java
│   └── EnquiryStatus.java
├── common/
│   ├── ApiError.java
│   ├── GlobalExceptionHandler.java
│   ├── NotFoundException.java
│   └── PageResponse.java
```

## Future Enhancements

- [ ] ~~Enquiry management system~~ ✅ Completed
- [ ] ~~Favorites/Wishlist feature~~ ✅ Completed
- [ ] Reviews and ratings
- [ ] Payment integration (Razorpay/Stripe)
- [ ] ~~Email notifications~~ ✅ Completed
- [ ] Redis caching
- [ ] Kafka event streaming
- [ ] Admin dashboard
- [ ] Advanced search filters
- [ ] Property listing analytics

## Contributing

1. Create a feature branch
2. Make your changes
3. Submit a pull request

## License

MIT License - See LICENSE file for details
