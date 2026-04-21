# Smart Campus Sensor & Room Management API

**Course:** 5COSC022W Client-Server Architectures  
**Student:** Qaaed Usaim  
**GitHub:** https://github.com/Qaaed/smart-campus-api

---

## API Design Overview

The Smart Campus API is a RESTful web service built using JAX-RS (Jersey 2.35) for managing campus infrastructure. The system models the relationship between physical Rooms and IoT Sensors deployed throughout the university campus.

### Core Architecture

- **Framework:** JAX-RS (Jersey 2.35)
- **Build Tool:** Apache Maven
- **Server:** Apache Tomcat 9
- **Data Storage:** In-memory ConcurrentHashMap
- **Base URI:** `/api/v1`
- **Response Format:** JSON

### Resource Hierarchy

The API implements three main entities:

1. **Rooms** - Physical campus locations
2. **Sensors** - IoT devices assigned to rooms
3. **Readings** - Historical measurement data from sensors

### Key Design Features

- **HATEOAS Discovery Endpoint** - Self-documenting API with hypermedia links at `/api/v1`
- **Referential Integrity** - Sensors must reference valid rooms
- **Business Rules** - Rooms with active sensors cannot be deleted
- **Sub-Resource Locator Pattern** - Readings accessed via `/sensors/{sensorId}/readings`
- **Custom Exception Mappers** - Return meaningful JSON errors (409, 422, 403, 500)
- **Request/Response Logging** - JAX-RS filter logs all API calls
- **Thread Safety** - ConcurrentHashMap ensures safe concurrent access

### API Resource Groups

The service exposes four resource groups under the versioned base path `/api/v1`:

| Resource | URI Prefix | Implementation |
|----------|-----------|----------------|
| Discovery | `/api/v1` | `DiscoveryResource` |
| Rooms | `/api/v1/rooms` | `RoomResource` |
| Sensors | `/api/v1/sensors` | `SensorResource` |
| Readings (sub-resource) | `/api/v1/sensors/{sensorId}/readings` | `ReadingsResource` |

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

## Build and Deployment Instructions

### Prerequisites

- Java Development Kit (JDK) 8 or higher
- Apache Maven 3.6+
- Apache Tomcat 9.x
- NetBeans IDE (recommended)

### Step 1: Clone the Repository

```bash
git clone https://github.com/Qaaed/smart-campus-api.git
cd smart-campus-api/campus_api
```

### Step 2: Build with Maven

```bash
mvn clean package
```

This compiles the code and creates `campus_api.war` in the `target/` directory.

### Step 3: Deploy to Tomcat

**Using NetBeans:**
1. Open NetBeans IDE
2. `File → Open Project` → select `campus_api` folder
3. Right-click project → `Clean and Build`
4. Right-click project → `Run`

**Manual Deployment:**
```bash
# Copy WAR to Tomcat webapps
cp target/campus_api.war /path/to/tomcat/webapps/

# Start Tomcat
/path/to/tomcat/bin/startup.sh  # Linux/Mac
# OR
C:\path\to\tomcat\bin\startup.bat  # Windows
```

### Step 4: Verify Deployment

The API will be available at:
```
http://localhost:8080/campus_api/api/v1
```

Test with:
```bash
curl http://localhost:8080/campus_api/api/v1
```

---

## Sample cURL Commands

### 1. Discovery Endpoint (HATEOAS)

```bash
curl http://localhost:8080/campus_api/api/v1
```

### 2. Create a Room

```bash
curl -X POST http://localhost:8080/campus_api/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{
    "id": "LIB-301",
    "name": "Library Quiet Study",
    "capacity": 50
  }'
```

### 3. Register a Temperature Sensor

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

### 4. Filter Sensors by Type

```bash
curl "http://localhost:8080/campus_api/api/v1/sensors?type=Temperature"
```

### 5. Record a Sensor Reading

```bash
curl -X POST http://localhost:8080/campus_api/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d '{
    "value": 22.5
  }'
```

### 6. Retrieve Reading History

```bash
curl http://localhost:8080/campus_api/api/v1/sensors/TEMP-001/readings
```

### 7. Attempt to Delete Room with Sensors (Error Demo)

```bash
curl -X DELETE http://localhost:8080/campus_api/api/v1/rooms/LIB-301
```

Expected: 409 Conflict error

### 8. Test Invalid roomId (Error Demo)

```bash
curl -X POST http://localhost:8080/campus_api/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{
    "id": "CO2-999",
    "type": "CO2",
    "status": "ACTIVE",
    "currentValue": 0,
    "roomId": "NONEXISTENT-ROOM"
  }'
```

Expected: 422 Unprocessable Entity error
