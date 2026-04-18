# Smart Campus Sensor & Room Management API

**Course:** 5COSC022W Client-Server Architectures  
**Student:** Qaaed Usaim 
**GitHub:** https://github.com/Qaaed/smart-campus-api

---

## API Overview

The Smart Campus API is a RESTful web service built using JAX-RS (Jersey 2.41) for managing campus infrastructure. The system models the relationship between physical Rooms and IoT Sensors deployed throughout the university campus.

### Core Architecture

- **Technology Stack:** JAX-RS (Jersey), Java 11, Maven
- **Data Storage:** In-memory ConcurrentHashMap (no database)
- **Base URI:** `/api/v1`
- **Response Format:** JSON

### Resource Hierarchy

```
/api/v1
├── /                    (Discovery endpoint with HATEOAS links)
├── /rooms               (Room collection)
│   ├── GET              (List all rooms)
│   ├── POST             (Create new room)
│   └── /{roomId}
│       ├── GET          (Get room details)
│       └── DELETE       (Delete room - blocked if sensors exist)
└── /sensors             (Sensor collection)
    ├── GET              (List sensors, optional ?type= filter)
    ├── POST             (Register sensor - validates roomId)
    └── /{sensorId}
        ├── GET          (Get sensor details)
        └── /readings    (Sub-resource for historical data)
            ├── GET      (Retrieve reading history)
            └── POST     (Record new reading - updates currentValue)
```

### Key Design Principles

1. **Resource-Based Design:** Each entity (Room, Sensor, Reading) is modeled as a RESTful resource with appropriate HTTP verbs
2. **Referential Integrity:** Sensors must reference valid rooms; rooms with sensors cannot be deleted
3. **State Validation:** Sensors in MAINTENANCE/OFFLINE status reject new readings
4. **Sub-Resource Locator Pattern:** Reading operations are delegated to a separate resource class for better modularity
5. **Comprehensive Error Handling:** Custom exception mappers provide meaningful JSON error responses (no stack trace leaks)

---

## Build and Deployment Instructions

### Prerequisites

- **Java JDK 11** or higher
- **Apache Maven 3.6+**
- **Apache Tomcat 9** or **GlassFish 5** (NetBeans includes both)
- **NetBeans IDE** (recommended) or any IDE supporting Maven projects

### Step 1: Clone the Repository

```bash
git clone https://github.com/Qaaed/smart-campus-api.git
cd smart-campus-api
```

### Step 2: Build with Maven

```bash
mvn clean package
```

This generates `smart-campus-api.war` in the `target/` directory.

### Step 3: Deploy to Server

#### Option A: NetBeans (Recommended)

1. Open the project in NetBeans (`File → Open Project`)
2. Right-click the project → `Clean and Build`
3. Right-click the project → `Run`
4. NetBeans will auto-deploy to the bundled GlassFish/Tomcat

#### Option B: Manual Deployment (Tomcat)

```bash
# Copy WAR to Tomcat webapps
cp target/smart-campus-api.war /path/to/tomcat/webapps/

# Start Tomcat
/path/to/tomcat/bin/catalina.sh run
```

#### Option C: Manual Deployment (GlassFish)

```bash
asadmin deploy target/smart-campus-api.war
```

### Step 4: Verify Deployment

The API will be available at:
```
http://localhost:8080/smart-campus-api/api/v1
```

Test the discovery endpoint:
```bash
curl http://localhost:8080/smart-campus-api/api/v1
```

Expected response:
```json
{
  "name": "Smart Campus Sensor & Room Management API",
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

---

## Sample cURL Commands

### 1. Discovery Endpoint (HATEOAS)

```bash
curl http://localhost:8080/smart-campus-api/api/v1
```

### 2. Create a Room

```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{
    "id": "LIB-301",
    "name": "Library Quiet Study",
    "capacity": 50
  }'
```

### 3. Register a Temperature Sensor

```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{
    "id": "TEMP-001",
    "type": "Temperature",
    "status": "ACTIVE",
    "currentValue": 0,
    "roomId": "LIB-301"
  }'
```

### 4. Filter Sensors by Type

```bash
curl "http://localhost:8080/smart-campus-api/api/v1/sensors?type=Temperature"
```

### 5. Record a Sensor Reading

```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d '{
    "value": 22.5
  }'
```

### 6. Retrieve Reading History

```bash
curl http://localhost:8080/smart-campus-api/api/v1/sensors/TEMP-001/readings
```

### 7. Attempt to Delete a Room with Sensors (Error Handling Demo)

```bash
curl -X DELETE http://localhost:8080/smart-campus-api/api/v1/rooms/LIB-301
```

Expected response (HTTP 409):
```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Room 'LIB-301' still has 1 sensor(s) assigned. Remove them first.",
  "hint": "Remove or reassign all sensors before deleting this room."
}
```

### 8. Test Referential Integrity (Invalid roomId)

```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{
    "id": "CO2-999",
    "type": "CO2",
    "status": "ACTIVE",
    "currentValue": 0,
    "roomId": "NONEXISTENT-ROOM"
  }'
```

Expected response (HTTP 422):
```json
{
  "status": 422,
  "error": "Unprocessable Entity",
  "message": "Room 'NONEXISTENT-ROOM' does not exist. Cannot register sensor.",
  "hint": "Ensure the roomId in your payload refers to an existing room."
}
```

---

## Testing Checklist

Use these test scenarios for your Postman video demonstration:

- [ ] **Discovery endpoint** returns HATEOAS links
- [ ] **Create room** succeeds with 201 Created
- [ ] **Create sensor** with valid roomId succeeds
- [ ] **Create sensor** with invalid roomId returns 422
- [ ] **Filter sensors** by type using `?type=CO2`
- [ ] **Post reading** to ACTIVE sensor succeeds, updates currentValue
- [ ] **Post reading** to MAINTENANCE sensor returns 403
- [ ] **Delete room** with sensors returns 409 Conflict
- [ ] **Delete room** without sensors succeeds with 204
- [ ] **Retrieve reading history** for a sensor
- [ ] **Send wrong Content-Type** (text/plain) returns 415
- [ ] **Send malformed JSON** returns 400 Bad Request

---

## Notes

- **No Database Required:** All data stored in ConcurrentHashMap (thread-safe)
- **JAX-RS Only:** No Spring Boot, Spring MVC, or other frameworks used
- **Jersey 2.41:** Uses `javax.ws.rs.*` namespace (not Jakarta `jakarta.ws.rs.*`)
- **Tomcat Compatibility:** Use Tomcat 9.x (not 10+) for `javax.*` support

---
