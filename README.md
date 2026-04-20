# Smart Campus Sensor & Room Management API

**Course:** 5COSC022W Client-Server Architectures  
**Student:** Qaaed Usaim  
---

## API Overview

The Smart Campus API is a RESTful web service built using JAX-RS (Jersey 2.35) for managing campus infrastructure. The system models the relationship between physical Rooms and IoT Sensors deployed throughout the university campus, providing real-time monitoring and historical data tracking capabilities.

### Technology Stack

- **Framework:** JAX-RS (Jersey 2.35)
- **Java Version:** Java 8
- **Build Tool:** Apache Maven
- **Server:** Apache Tomcat 9
- **Data Storage:** In-memory ConcurrentHashMap (no database required)
- **Response Format:** JSON

### Core Architecture

The API implements a three-tier resource hierarchy:

1. **Rooms** - Physical locations on campus
2. **Sensors** - IoT devices deployed in rooms
3. **Readings** - Historical measurement data from sensors

All data is stored in-memory using thread-safe ConcurrentHashMap structures to ensure data consistency across concurrent requests.

### Resource Endpoints

```
/api/v1
├── /                    → Discovery endpoint (HATEOAS links)
├── /rooms
│   ├── GET              → List all rooms
│   ├── POST             → Create a new room
│   └── /{roomId}
│       ├── GET          → Get room details
│       └── DELETE       → Delete room (fails if sensors exist)
└── /sensors
    ├── GET              → List all sensors (supports ?type= filter)
    ├── POST             → Register new sensor (validates roomId)
    └── /{sensorId}
        ├── GET          → Get sensor details
        └── /readings
            ├── GET      → Retrieve reading history
            └── POST     → Record new reading (updates currentValue)
```

### Key Features

- **HATEOAS Discovery** - Self-documenting API with hypermedia links
- **Referential Integrity** - Sensors must reference valid rooms
- **Business Rule Enforcement** - Rooms with sensors cannot be deleted
- **State Validation** - Sensors in MAINTENANCE/OFFLINE status reject readings
- **Sub-Resource Pattern** - Readings are scoped to their parent sensor
- **Comprehensive Error Handling** - Custom exception mappers return meaningful JSON errors
- **Request/Response Logging** - Automatic logging via JAX-RS filters
- **Thread-Safe Storage** - ConcurrentHashMap prevents race conditions

---

## Build and Deployment Instructions

### Prerequisites

Ensure you have the following installed:

- **Java Development Kit (JDK) 8** or higher
- **Apache Maven 3.6+**
- **Apache Tomcat 9.x** (NOT Tomcat 10+ - see compatibility notes below)
- **NetBeans IDE** (recommended) or any Maven-compatible IDE

### Step 1: Clone the Repository

```bash
git clone https://github.com/Qaaed/smart-campus-api.git
cd smart-campus-api
```

### Step 2: Build the Project with Maven

Navigate to the project root directory (where `pom.xml` is located) and run:

```bash
cd campus_api
mvn clean package
```

This will:
- Compile all Java source files
- Run any configured tests
- Package the application as `campus_api.war` in the `target/` directory

Expected output:
```
[INFO] BUILD SUCCESS
[INFO] Total time: 5.234 s
```

### Step 3: Deploy to Apache Tomcat

#### NetBeans IDE (Recommended)

1. Open NetBeans IDE
2. Go to `File → Open Project`
3. Navigate to the `campus_api` folder and select it
4. Right-click the project in the Projects panel
5. Select `Clean and Build`
6. Right-click the project again
7. Select `Run`

NetBeans will automatically:
- Build the WAR file
- Start Tomcat
- Deploy the application
- Open a browser to the deployment URL

### Step 4: Verify Deployment

Once deployed, the API will be available at:

```
http://localhost:8080/campus_api/api/v1
```

**Test the discovery endpoint:**

```bash
curl http://localhost:8080/campus_api/api/v1
```

**Expected Response:**
```json
{
  "name": "Sensor and Room API manager",
  "version": "1.0",
  "contact": "admin@smartcampus.ac.uk",
  "description": "RESTful API for managing campus rooms and IoT sensors.",
  "resources": {
    "rooms": "/api/v1/rooms",
    "sensors": "/api/v1/sensors"
  },
  "_links": {
    "self": "/api/v1",
    "rooms": "/api/v1/rooms",
    "sensors": "/api/v1/sensors"
  }
}
```

If you see this response, the API is running successfully!

---

## API Endpoints

The service exposes four resource groups under the versioned base path `/api/v1`:

| Resource | URI Prefix | Implementation |
|----------|-----------|----------------|
| Discovery | `/api/v1` | `DiscoveryResource` |
| Rooms | `/api/v1/rooms` | `RoomResource` |
| Sensors | `/api/v1/sensors` | `SensorResource` |
| Readings (sub resource) | `/api/v1/sensors/{sensorId}/readings` | `ReadingsResource` |

### Available Endpoints

**Base URL:** `http://localhost:8080/campus_api/api/v1`

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1` | API discovery and metadata |
| GET | `/api/v1/rooms` | Get all rooms |
| POST | `/api/v1/rooms` | Create a new room |
| GET | `/api/v1/rooms/{roomId}` | Get a specific room by ID |
| DELETE | `/api/v1/rooms/{roomId}` | Delete a room (only if no sensors) |
| GET | `/api/v1/sensors` | Get all sensors (supports `?type=` filter) |
| POST | `/api/v1/sensors` | Register a new sensor |
| GET | `/api/v1/sensors/{sensorId}` | Get a specific sensor by ID |
| GET | `/api/v1/sensors/{sensorId}/readings` | Get all readings for a sensor |
| POST | `/api/v1/sensors/{sensorId}/readings` | Add a new reading for a sensor |

---

## Sample cURL Commands

The following cURL commands demonstrate the API's core functionality. Use these for testing or as examples for your Postman collection.

### 1. Discovery Endpoint (HATEOAS)

Retrieve API metadata and available resource links:

```bash
curl http://localhost:8080/campus_api/api/v1
```

### 2. Create a Room

Register a new room on campus:

```bash
curl -X POST http://localhost:8080/campus_api/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{
    "id": "LIB-301",
    "name": "Library Quiet Study",
    "capacity": 50
  }'
```

**Response (201 Created):**
```json
{
  "id": "LIB-301",
  "name": "Library Quiet Study",
  "capacity": 50,
  "sensorIds": []
}
```

### 3. List All Rooms

Retrieve all registered rooms:

```bash
curl http://localhost:8080/campus_api/api/v1/rooms
```

### 4. Register a Temperature Sensor

Create a new sensor and assign it to a room:

```bash
curl -X POST http://localhost:8080/campus_api/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{
    "id": "TEMP-001",
    "type": "Temperature",
    "status": "ACTIVE",
    "currentValue": 0,
    "roomId": "LIB-301"
  }'
```

**Response (201 Created):**
```json
{
  "id": "TEMP-001",
  "type": "Temperature",
  "status": "ACTIVE",
  "currentValue": 0.0,
  "roomId": "LIB-301"
}
```

### 5. Filter Sensors by Type

Retrieve only sensors of a specific type using query parameters:

```bash
curl "http://localhost:8080/campus_api/api/v1/sensors?type=Temperature"
```

### 6. Record a Sensor Reading

Submit a new measurement from a sensor:

```bash
curl -X POST http://localhost:8080/campus_api/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d '{
    "value": 22.5
  }'
```

**Response (201 Created):**
```json
{
  "id": "a3f2c9e8-1234-5678-90ab-cdef12345678",
  "timestamp": 1714392847623,
  "value": 22.5
}
```

*Note: The `id` is auto-generated (UUID) and `timestamp` is set to current server time if not provided.*

### 7. Retrieve Reading History

Get all historical readings for a specific sensor:

```bash
curl http://localhost:8080/campus_api/api/v1/sensors/TEMP-001/readings
```

### 8. Get Specific Room Details

Retrieve detailed information about a single room:

```bash
curl http://localhost:8080/campus_api/api/v1/rooms/LIB-301
```

### 9. Attempt to Delete a Room with Sensors (Error Demo)

Try to delete a room that still has sensors assigned:

```bash
curl -X DELETE http://localhost:8080/campus_api/api/v1/rooms/LIB-301
```

**Response (409 Conflict):**
```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Room 'LIB-301' still has 1 sensor(s) assigned. Remove them first.",
  "hint": "Remove or reassign all sensors before deleting this room."
}
```

### 10. Test Referential Integrity (Invalid roomId)

Attempt to register a sensor with a non-existent room:

```bash
curl -X POST http://localhost:8080/campus_api/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{
    "id": "CO2-999",
    "type": "CO2",
    "status": "ACTIVE",
    "currentValue": 0,
    "roomId": "FAKE-ROOM-123"
  }'
```

**Response (422 Unprocessable Entity):**
```json
{
  "status": 422,
  "error": "Unprocessable Entity",
  "message": "Room 'FAKE-ROOM-123' does not exist. Cannot register sensor.",
  "hint": "Ensure the roomId in your payload refers to an existing room."
}
```

### 11. Test Sensor State Validation

First, create a sensor in MAINTENANCE mode:

```bash
curl -X POST http://localhost:8080/campus_api/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{
    "id": "TEMP-002",
    "type": "Temperature",
    "status": "MAINTENANCE",
    "currentValue": 0,
    "roomId": "LIB-301"
  }'
```

Then try to record a reading:

```bash
curl -X POST http://localhost:8080/campus_api/api/v1/sensors/TEMP-002/readings \
  -H "Content-Type: application/json" \
  -d '{
    "value": 25.0
  }'
```

**Response (403 Forbidden):**
```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "Sensor 'TEMP-002' is under MAINTENANCE and cannot accept readings.",
  "hint": "Sensor must be ACTIVE to accept new readings."
}
```

### 12. Test Content-Type Validation

Send data with incorrect Content-Type header:

```bash
curl -X POST http://localhost:8080/campus_api/api/v1/sensors \
  -H "Content-Type: text/plain" \
  -d 'plain text data'
```

**Response (415 Unsupported Media Type):**
```json
{
  "message": "HTTP 415 Unsupported Media Type"
}
```

---

## Project Structure

```
campus_api/
├── pom.xml                                    # Maven configuration
├── src/main/java/com/smartcampus/campus_api/
│   ├── SmartCampusApp.java                    # JAX-RS application entry point
│   ├── Storage.java                           # In-memory data store (singleton)
│   ├── LoggingFilter.java                     # Request/response logging filter
│   │
│   ├── models/
│   │   ├── Room.java                          # Room entity
│   │   ├── Sensor.java                        # Sensor entity
│   │   └── Readings.java                      # Reading entity
│   │
│   ├── resources/
│   │   ├── DiscoveryResource.java             # Discovery endpoint
│   │   ├── RoomResource.java                  # Room CRUD operations
│   │   ├── SensorResource.java                # Sensor CRUD + sub-resource locator
│   │   └── ReadingsResource.java              # Readings sub-resource
│   │
│   ├── exceptions/
│   │   ├── RoomNotEmptyException.java         # Custom exception for room deletion
│   │   ├── LinkedResourceNotFoundException.java  # Custom exception for invalid roomId
│   │   └── SensorUnavailableException.java    # Custom exception for sensor state
│   │
│   └── mappers/
│       ├── RoomNotEmptyExceptionMapper.java   # Maps to 409 Conflict
│       ├── LinkedResourceNotFoundExceptionMapper.java  # Maps to 422 Unprocessable Entity
│       ├── SensorUnavailableExceptionMapper.java      # Maps to 403 Forbidden
│       └── GlobalExceptionMapper.java         # Catches all uncaught exceptions (500)
│
└── src/main/webapp/
    ├── WEB-INF/
    │   ├── web.xml                            # Servlet configuration
    │   └── beans.xml                          # CDI configuration
    └── index.html                             # Default welcome page
```

---

## API Design Decisions

### In-Memory Storage

The API uses `ConcurrentHashMap` for thread-safe data storage without requiring a database. This design choice:

- Simplifies deployment (no database setup required)
- Provides excellent performance for small-to-medium datasets
- Ensures thread safety across concurrent requests
- Meets coursework requirements (no database allowed)

**Trade-off:** Data is lost on server restart. For production use, this would be replaced with persistent storage.

### Sub-Resource Locator Pattern

Readings are accessed via `/sensors/{sensorId}/readings` using the sub-resource locator pattern. This:

- Creates clear parent-child relationships in the URL structure
- Delegates reading operations to a separate `ReadingsResource` class
- Improves code organization and maintainability
- Automatically scopes all operations to the parent sensor

### Exception Mapping Strategy

The API implements a layered exception handling approach:

1. **Specific Exception Mappers** - Handle known business rule violations with appropriate HTTP status codes
2. **Global Exception Mapper** - Catches all other exceptions to prevent stack trace leakage
3. **Meaningful JSON Responses** - All errors return structured JSON with hints for resolution

This ensures clients receive actionable error messages while protecting internal implementation details.

### Filter-Based Logging

Request/response logging is implemented as a JAX-RS filter rather than in individual resource methods. This:

- Guarantees logging for all endpoints (no chance of forgetting)
- Separates cross-cutting concerns from business logic
- Provides complete observability even when exceptions occur
- Centralizes logging format and configuration

---

## Important Notes

### Tomcat Version Compatibility

**This project requires Apache Tomcat 9.x**

- ✅ **Compatible:** Tomcat 9.0.x
- ❌ **NOT Compatible:** Tomcat 10.0+ or Jakarta EE 9+

**Reason:** This project uses Jersey 2.35, which implements the `javax.ws.rs.*` namespace. Tomcat 10+ requires the new `jakarta.ws.rs.*` namespace.

If you must use Tomcat 10+, upgrade to Jersey 3.x and update all imports from `javax.*` to `jakarta.*`.

### Data Persistence

All data is stored in-memory and **will be lost** when the server restarts. To reset the API to a clean state, simply restart Tomcat.

### Technology Restrictions

Per coursework requirements:

- ✅ **Allowed:** JAX-RS (Jersey), Java 8+, Maven
- ❌ **NOT Allowed:** Spring Boot, Spring MVC, databases (SQL/NoSQL)

This implementation strictly adheres to these constraints.

---

## License

MIT License - Academic coursework project for University of Westminster.

Copyright (c) 2026 Qaaed Usaim
