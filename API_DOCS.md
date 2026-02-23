# GetSet Microservices API Documentation

This document outlines all currently available endpoints through the API Gateway (`http://localhost:8090`). All protected endpoints require an `Authorization` header containing a valid Bearer token (`Bearer <token>`).

## 1. Authentication & Users (`user-service`)

### Register User
- **Method:** `POST`
- **Path:** `/api/v1/auth/register`
- **Description:** Registers a new user.
- **Request Body:**
```json
{
  "name": "Bob Renter",
  "email": "bob@getset.com",
  "password": "password123",
  "role": "RENTER"
}
```

### Login User
- **Method:** `POST`
- **Path:** `/api/v1/auth/login`
- **Description:** Authenticates a user and returns JWT tokens.
- **Request Body:**
```json
{
  "email": "bob@getset.com",
  "password": "password123"
}
```
- **Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUz...",
  "refreshToken": "eyJhbGciOiJIUz...",
  "id": "699bec158e1b1b78dcc4e490",
  "name": "Bob Renter",
  "email": "bob@getset.com",
  "role": "RENTER"
}
```

---

## 2. Properties (`property-service`)

### Create Property
- **Method:** `POST`
- **Path:** `/api/v1/properties`
- **Description:** Creates a new property listing (Owner only).
- **Request Body:**
```json
{
  "title": "Cozy 2BHK Apartment",
  "description": "Spacious apartment in the heart of the city with great views.",
  "address": "123 Main Street, Downtown",
  "city": "Metropolis",
  "price": 1500.00,
  "bedrooms": 2,
  "bathrooms": 2,
  "area": 1200.0,
  "amenities": ["Gym", "Pool", "Parking"]
}
```

### Get All Properties
- **Method:** `GET`
- **Path:** `/api/v1/properties`
- **Description:** Fetches a paginated list of all current properties.
- **Query Params:** `?page=0&size=20&city=Metropolis`

### Get Property by ID
- **Method:** `GET`
- **Path:** `/api/v1/properties/{propertyId}`
- **Description:** Retrieves the details of a single property.

---

## 3. Enquiries (`enquiry-service`)

### Create Enquiry
- **Method:** `POST`
- **Path:** `/api/v1/enquiries`
- **Description:** Submits a new rental enquiry for a property (Renter only).
- **Request Body:**
```json
{
  "propertyId": "699bf17f50478e1679ea7fa9",
  "message": "Hi Alice, I am very interested in this apartment. Is it available for a viewing this weekend?"
}
```

### Get My Enquiries (Renter)
- **Method:** `GET`
- **Path:** `/api/v1/enquiries/my`
- **Description:** Retrieves a list of enquiries made by the authenticated renter.

### Get Received Enquiries (Owner)
- **Method:** `GET`
- **Path:** `/api/v1/enquiries/received`
- **Description:** Retrieves a list of enquiries received on the authenticated owner's properties.

### Update Enquiry Status (Owner)
- **Method:** `PUT`
- **Path:** `/api/v1/enquiries/{enquiryId}/status`
- **Description:** Approves or rejects an enquiry.
- **Request Body:**
```json
{
  "status": "ACCEPTED",
  "rejectionReason": ""
}
```

---

## 4. Messages (`message-service`)

### Send Message
- **Method:** `POST`
- **Path:** `/api/v1/messages`
- **Description:** Sends a message to another user.
- **Request Body:**
```json
{
  "recipientId": "699bec158e1b1b78dcc4e490",
  "content": "Hello Bob, thanks for your enquiry! Let's schedule a viewing for Saturday.",
  "threadId": "optional-uuid-here",
  "propertyId": "699bf17f50478e1679ea7fa9",
  "enquiryId": "699bf63c889f8a7d785f4d20"
}
```

### Get Received Messages
- **Method:** `GET`
- **Path:** `/api/v1/messages/received`
- **Description:** Retrieves a list of messages received by the authenticated user.

### Get Sent Messages
- **Method:** `GET`
- **Path:** `/api/v1/messages/sent`
- **Description:** Retrieves messages sent by the authenticated user.

### Mark Message as Read
- **Method:** `PUT`
- **Path:** `/api/v1/messages/{messageId}/read`
- **Description:** Marks a received message as read.

### Get Unread Count
- **Method:** `GET`
- **Path:** `/api/v1/messages/unread-count`
- **Description:** Returns the count of unread messages for the authenticated user.
- **Response:**
```json
{
  "count": 5
}
```

---

## 5. Favorites (`favorite-service`)

### Add to Favorites
- **Method:** `POST`
- **Path:** `/api/v1/favorites/{propertyId}`
- **Description:** Adds a property to the user's favorites list.

### Remove from Favorites
- **Method:** `DELETE`
- **Path:** `/api/v1/favorites/{propertyId}`
- **Description:** Removes a property from the user's favorites list.

### Get My Favorites
- **Method:** `GET`
- **Path:** `/api/v1/favorites`
- **Description:** Fetches the list of the user's favorite properties.
- **Response:**
```json
{
  "content": [
    {
      "id": "699bf7e19f7b1e4a6d123b3a",
      "userId": "699bec158e1b1b78dcc4e490",
      "propertyId": "699bf17f50478e1679ea7fa9",
      "propertyTitle": "Cozy 2BHK Apartment",
      "price": 1500.00,
      "city": "Metropolis"
    }
  ],
  "pageNumber": 0,
  "pageSize": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

### Check Favorite Status
- **Method:** `GET`
- **Path:** `/api/v1/favorites/check/{propertyId}`
- **Description:** Checks if a specific property is saved in the user's favorites.
- **Response:**
```json
{
  "isFavorite": true
}
```

---

## 6. Notifications (`notification-service`)

*Note: The notification module primarily acts as an asynchronous Kafka consumer and dispatches email alerts based on events emitted by `enquiry-service` and `message-service`. It currently does not expose REST endpoints to users.*
