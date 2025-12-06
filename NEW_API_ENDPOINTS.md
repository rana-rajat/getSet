# 📡 New API Endpoints Summary

**Updated**: December 6, 2025  
**Framework**: Spring Boot 3.4.10  
**Total Endpoints**: 20+  

---

## 🎯 Endpoint Overview

### Total Endpoints Breakdown

| Category | Count | Status |
|----------|-------|--------|
| Authentication | 3 | ✅ Existing |
| Properties | 7 | ✅ Existing |
| **Enquiries** | **7** | **✅ NEW** |
| **Favorites** | **6** | **✅ NEW** |
| **Messages** | **10** | **✅ NEW** |
| **Notifications** | **6** | **✅ NEW** |
| **Total** | **39** | **✅ COMPLETE** |

---

## 📋 Enquiry Management Endpoints (7)

### Create Enquiry
```
POST /api/v1/enquiries
Authorization: Bearer JWT (RENTER role required)
Content-Type: application/json

Request Body:
{
  "propertyId": "507f1f77bcf86cd799439011",
  "message": "Interested in this property"
}

Response (201 Created):
{
  "id": "507f1f77bcf86cd799439012",
  "propertyId": "507f1f77bcf86cd799439011",
  "renterId": "507f1f77bcf86cd799439010",
  "renterName": "John Renter",
  "renterEmail": "john@example.com",
  "ownerId": "507f1f77bcf86cd799439013",
  "ownerName": "Jane Owner",
  "ownerEmail": "jane@example.com",
  "message": "Interested in this property",
  "status": "PENDING",
  "createdAt": "2025-12-06T10:30:00Z"
}
```

### Get Enquiry Details
```
GET /api/v1/enquiries/{enquiryId}
Authorization: Bearer JWT

Response (200 OK):
{
  "id": "507f1f77bcf86cd799439012",
  "propertyId": "507f1f77bcf86cd799439011",
  "renterId": "507f1f77bcf86cd799439010",
  "renterName": "John Renter",
  "renterEmail": "john@example.com",
  "ownerId": "507f1f77bcf86cd799439013",
  "ownerName": "Jane Owner",
  "ownerEmail": "jane@example.com",
  "message": "Interested in this property",
  "status": "PENDING",
  "rejectionReason": null,
  "createdAt": "2025-12-06T10:30:00Z",
  "updatedAt": "2025-12-06T10:30:00Z"
}
```

### Get Enquiries for Property
```
GET /api/v1/enquiries/property/{propertyId}
Authorization: Bearer JWT (OWNER role required)
Query Params: page=0&size=10

Response (200 OK):
{
  "content": [
    { enquiry objects }
  ],
  "pageNumber": 0,
  "pageSize": 10,
  "totalElements": 5,
  "totalPages": 1,
  "hasNext": false,
  "hasPrevious": false
}
```

### Get My Enquiries (Renter)
```
GET /api/v1/enquiries/renter/my-enquiries
Authorization: Bearer JWT (RENTER role required)
Query Params: page=0&size=10&status=PENDING

Response (200 OK):
{
  "content": [ enquiry objects ],
  "pageNumber": 0,
  "pageSize": 10,
  "totalElements": 3,
  "totalPages": 1,
  "hasNext": false,
  "hasPrevious": false
}
```

### Update Enquiry Status
```
PUT /api/v1/enquiries/{enquiryId}
Authorization: Bearer JWT (OWNER role required)
Content-Type: application/json

Request Body:
{
  "status": "ACCEPTED"  // or "REJECTED"
}

OR (if rejecting):
{
  "status": "REJECTED",
  "rejectionReason": "Property is no longer available"
}

Response (200 OK):
{
  "id": "507f1f77bcf86cd799439012",
  "status": "ACCEPTED",
  "updatedAt": "2025-12-06T11:00:00Z"
}
```

### Cancel Enquiry
```
DELETE /api/v1/enquiries/{enquiryId}
Authorization: Bearer JWT (RENTER role required)

Response (204 No Content)
```

### Get Enquiry Statistics (Owner)
```
GET /api/v1/enquiries/owner/stats
Authorization: Bearer JWT (OWNER role required)

Response (200 OK):
{
  "totalEnquiries": 15,
  "pendingCount": 3,
  "acceptedCount": 8,
  "rejectedCount": 4
}
```

---

## ⭐ Favorites/Wishlist Endpoints (6)

### Add to Favorites
```
POST /api/v1/favorites
Authorization: Bearer JWT (RENTER role required)
Content-Type: application/json

Request Body:
{
  "propertyId": "507f1f77bcf86cd799439011",
  "notes": "Great location, near subway"
}

Response (201 Created):
{
  "id": "507f1f77bcf86cd799439014",
  "propertyId": "507f1f77bcf86cd799439011",
  "renterId": "507f1f77bcf86cd799439010",
  "notes": "Great location, near subway",
  "createdAt": "2025-12-06T10:30:00Z"
}
```

### Remove from Favorites
```
DELETE /api/v1/favorites/{propertyId}
Authorization: Bearer JWT (RENTER role required)

Response (204 No Content)
```

### Get All Favorites
```
GET /api/v1/favorites
Authorization: Bearer JWT (RENTER role required)
Query Params: page=0&size=10

Response (200 OK):
{
  "content": [
    {
      "id": "507f1f77bcf86cd799439014",
      "propertyId": "507f1f77bcf86cd799439011",
      "renterId": "507f1f77bcf86cd799439010",
      "property": { property details },
      "notes": "Great location",
      "createdAt": "2025-12-06T10:30:00Z"
    }
  ],
  "pageNumber": 0,
  "pageSize": 10,
  "totalElements": 5,
  "totalPages": 1,
  "hasNext": false,
  "hasPrevious": false
}
```

### Check if Favorited
```
GET /api/v1/favorites/check/{propertyId}
Authorization: Bearer JWT (RENTER role required)

Response (200 OK):
{
  "isFavorite": true,
  "notes": "Great location, near subway"
}
```

### Get Favorite Count
```
GET /api/v1/favorites/count
Authorization: Bearer JWT (RENTER role required)

Response (200 OK):
{
  "count": 5
}
```

### Update Favorite Notes
```
PUT /api/v1/favorites/{propertyId}
Authorization: Bearer JWT (RENTER role required)
Content-Type: application/json

Request Body:
{
  "notes": "Updated notes about this property"
}

Response (200 OK):
{
  "id": "507f1f77bcf86cd799439014",
  "propertyId": "507f1f77bcf86cd799439011",
  "notes": "Updated notes about this property",
  "updatedAt": "2025-12-06T11:00:00Z"
}
```

---

## 💬 Direct Messaging Endpoints (10)

### Send Message
```
POST /api/v1/messages
Authorization: Bearer JWT
Content-Type: application/json

Request Body:
{
  "recipientId": "507f1f77bcf86cd799439013",
  "propertyId": "507f1f77bcf86cd799439011",
  "enquiryId": "507f1f77bcf86cd799439012",
  "content": "Is this property still available?"
}

Response (201 Created):
{
  "id": "507f1f77bcf86cd799439015",
  "threadId": "507f1f77bcf86cd799439010_507f1f77bcf86cd799439013_507f1f77bcf86cd799439011",
  "senderId": "507f1f77bcf86cd799439010",
  "senderName": "John Renter",
  "recipientId": "507f1f77bcf86cd799439013",
  "recipientName": "Jane Owner",
  "content": "Is this property still available?",
  "read": false,
  "createdAt": "2025-12-06T10:30:00Z"
}
```

### Get Thread Messages
```
GET /api/v1/messages/thread/{threadId}
Authorization: Bearer JWT
Query Params: page=0&size=20

Response (200 OK):
{
  "content": [ message objects ],
  "pageNumber": 0,
  "pageSize": 20,
  "totalElements": 8,
  "totalPages": 1,
  "hasNext": false,
  "hasPrevious": false
}
```

### Get Received Messages
```
GET /api/v1/messages/received
Authorization: Bearer JWT
Query Params: page=0&size=20

Response (200 OK):
PageResponse with all received messages
```

### Get Sent Messages
```
GET /api/v1/messages/sent
Authorization: Bearer JWT
Query Params: page=0&size=20

Response (200 OK):
PageResponse with all sent messages
```

### Get Unread Messages
```
GET /api/v1/messages/unread
Authorization: Bearer JWT
Query Params: page=0&size=20

Response (200 OK):
PageResponse with only unread messages for current user
```

### Get Unread Count
```
GET /api/v1/messages/unread/count
Authorization: Bearer JWT

Response (200 OK):
{
  "unreadCount": 3
}
```

### Mark Message as Read
```
PUT /api/v1/messages/{messageId}/read
Authorization: Bearer JWT

Response (200 OK):
{
  "id": "507f1f77bcf86cd799439015",
  "read": true,
  "updatedAt": "2025-12-06T11:00:00Z"
}
```

### Mark All as Read
```
PUT /api/v1/messages/read-all
Authorization: Bearer JWT

Response (200 OK):
{
  "markedAsReadCount": 3
}
```

### Get Conversation with Specific User
```
GET /api/v1/messages/conversation/{otherUserId}/{propertyId}
Authorization: Bearer JWT
Query Params: page=0&size=20

Response (200 OK):
{
  "content": [ message objects in thread ],
  "pageNumber": 0,
  "pageSize": 20,
  "totalElements": 5,
  "totalPages": 1,
  "hasNext": false,
  "hasPrevious": false
}
```

### Get All Conversations
```
GET /api/v1/messages/conversations
Authorization: Bearer JWT

Response (200 OK):
{
  "content": [
    {
      "threadId": "507f1f77bcf86cd799439010_507f1f77bcf86cd799439013_507f1f77bcf86cd799439011",
      "otherUserId": "507f1f77bcf86cd799439013",
      "otherUserName": "Jane Owner",
      "otherUserEmail": "jane@example.com",
      "propertyId": "507f1f77bcf86cd799439011",
      "lastMessage": "Yes, it's still available",
      "lastMessageTime": "2025-12-06T11:00:00Z",
      "unreadCount": 2
    }
  ]
}
```

---

## 📧 Notification Endpoints (6)

### Get All Notifications
```
GET /api/v1/notifications
Authorization: Bearer JWT
Query Params: page=0&size=20

Response (200 OK):
{
  "content": [
    {
      "id": "507f1f77bcf86cd799439016",
      "subject": "New Enquiry Received",
      "body": "Someone is interested in your property",
      "type": "ENQUIRY_RECEIVED",
      "read": false,
      "emailSent": true,
      "createdAt": "2025-12-06T10:30:00Z"
    }
  ],
  "pageNumber": 0,
  "pageSize": 20,
  "totalElements": 10,
  "totalPages": 1,
  "hasNext": false,
  "hasPrevious": false
}
```

### Get Unread Notifications
```
GET /api/v1/notifications/unread
Authorization: Bearer JWT
Query Params: page=0&size=20

Response (200 OK):
PageResponse with only unread notifications
```

### Get Unread Count
```
GET /api/v1/notifications/unread/count
Authorization: Bearer JWT

Response (200 OK):
{
  "unreadCount": 3
}
```

### Mark as Read
```
PUT /api/v1/notifications/{notificationId}/read
Authorization: Bearer JWT

Response (200 OK):
{
  "id": "507f1f77bcf86cd799439016",
  "read": true
}
```

### Mark All as Read
```
PUT /api/v1/notifications/read-all
Authorization: Bearer JWT

Response (200 OK):
{
  "markedAsReadCount": 5
}
```

### Delete Notification
```
DELETE /api/v1/notifications/{notificationId}
Authorization: Bearer JWT

Response (204 No Content)
```

---

## 🔐 Authentication Headers

All endpoints (except auth) require:
```
Authorization: Bearer {JWT_TOKEN}
X-User-Id: {USER_ID}
X-User-Name: {USER_NAME}
X-User-Email: {USER_EMAIL}
```

These headers are automatically extracted from the JWT token by the `RequestContextFilter`.

---

## 📊 Error Responses

All endpoints return standardized error responses:

```json
{
  "timestamp": "2025-12-06T10:30:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Property not found",
  "path": "/api/v1/properties/507f1f77bcf86cd799439011"
}
```

### Common Status Codes
- `200 OK` - Success
- `201 Created` - Resource created
- `204 No Content` - Success with no response body
- `400 Bad Request` - Invalid input
- `401 Unauthorized` - Missing/invalid JWT
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Server error

---

**Generated**: December 6, 2025  
**Framework**: Spring Boot 3.4.10  
**Status**: ✅ Production Ready
