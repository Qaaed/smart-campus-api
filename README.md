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

# Conceptual Coursework Report

---
##  PART 1: Setup & Discovery

### 1.1 Architecture & JAX-RS Lifecycle

**Question:**  
Explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this architectural decision impacts the way you manage data.

**Answer:**  
By default, JAX-RS uses a per-request lifecycle. The runtime does not treat resource classes as singletons; a brand-new instance of a resource class (e.g., `RoomResource`) is instantiated for every incoming HTTP request and is immediately destroyed after the response is sent. Because of this, storing state in instance variables would result in immediate data loss.

To prevent this, in-memory data must be delegated to a shared Singleton class. As implemented in the project, the `Storage.java` class utilizes static `ConcurrentHashMap` structures. Because the API processes multiple requests concurrently on separate threads, standard collections would cause race conditions. `ConcurrentHashMap` ensures thread-safe, atomic operations without the need for explicit locking, guaranteeing data integrity.

---

### 1.2 Discovery Endpoint (HATEOAS)

**Question:**  
Why is the provision of "Hypermedia" considered a hallmark of advanced RESTful design? How does this benefit client developers compared to static documentation?

**Answer:**  
Hypermedia (HATEOAS) represents the highest level of REST maturity because it makes the API self-documenting. Unlike static documentation—which forces developers to hardcode exact URLs and frequently drifts out of sync—HATEOAS embeds navigation links directly within the JSON response. This provides a massive benefit to client developers: they are completely decoupled from the server's URL structure. If the backend team changes a route later, the client application will not break because it dynamically follows the relational links provided by the server rather than relying on hardcoded, fragile paths.

---

## PART 2: Room Management

### 2.1 Returning IDs vs. Full Objects

**Question:**  
When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and client-side processing.

**Answer:**  
Returning only IDs saves a minor amount of network bandwidth per payload, but it creates the severe "N+1 Request Problem" for client-side processing. The client is forced to make one initial request to retrieve the IDs, and then establish dozens of subsequent HTTP connections to fetch the actual room details, causing severe network connection overhead and UI lag.

As demonstrated in my `RoomResource.java`, I architected the endpoint to return full objects:

```java
@GET
public Response getAllRooms() {
    List<Room> list = new ArrayList<>(Storage.getRooms().values());
    return Response.ok(list).build(); // Returns full objects, not just IDs
}
```

---

### 2.2 DELETE Idempotency

**Question:**  
Is the DELETE operation idempotent in your implementation? Provide a detailed justification.

**Answer:**  
Yes, the DELETE operation is strictly idempotent. In REST architecture, idempotency dictates that making identical requests multiple times must leave the server's state the exact same as making a single request.

```java
@DELETE 
@Path("/{roomId}") 
public Response deleteRoom(@PathParam("roomId") String roomId) {
    Room room = Storage.getRooms().get(roomId);
    if (room == null) {
        return Response.status(Response.Status.NOT_FOUND).entity(...).build();
    }
    // ... validation ...
    Storage.getRooms().remove(roomId);
    return Response.noContent().build(); // 204 Success
}
```

If a client successfully deletes a room, the server removes it and returns `204 No Content`. If the exact same request is sent again, the `room == null` check triggers a `404 Not Found`. While the HTTP status code changes, the underlying state of the server remains unchanged (the room is still deleted). According to HTTP specifications, returning a `404` on subsequent deletions strictly preserves idempotency.

---

## PART 3: Sensor Operations & Filtering

### 3.1 Content Negotiation & Data Formats

**Question:**  
We explicitly use the `@Consumes(MediaType.APPLICATION_JSON)` annotation on the POST method. Explain the technical consequences if a client attempts to send data in a different format, such as `text/plain`.

**Answer:**  
The technical consequence is an immediate, fail-fast rejection of the request.

```java
@POST
@Consumes(MediaType.APPLICATION_JSON)
public Response createSensor(Sensor sensor) { ... }
```

During the routing phase, JAX-RS intercepts the incoming request and compares the client's `Content-Type` header against the `@Consumes` annotation. When it detects a mismatch (e.g., `text/plain`), JAX-RS automatically blocks the request and returns an HTTP `415 Unsupported Media Type` error. The `createSensor` method is never executed, protecting the server from parsing crashes, unhandled exceptions, and injection attacks.

---

### 3.2 Query Parameters vs. Path-Based Filtering

**Question:**  
Contrast using `@QueryParam` with an alternative design where the type is part of the URL path (e.g., `/api/v1/sensors/type/CO2`). Why is the query parameter approach superior?

**Answer:**  
Using query parameters (`?type=CO2`) is superior because it aligns with strict REST semantics. The URL path dictates the location of a resource, while query parameters act as optional filters. Using a path variable for filtering (e.g., `/type/CO2`) falsely implies that "type" is a permanent structural folder.

Furthermore, query parameters prevent severe code duplication. As seen in `SensorResource.java`, a single method handles both unfiltered and filtered requests seamlessly using Java Streams:

```java
@GET 
public Response getAllSensors(@QueryParam("type") String type) {
    List<Sensor> list = new ArrayList<>(Storage.getSensors().values());
    if (type != null && !type.trim().isEmpty()) {
        list = list.stream()
                .filter(s -> type.equalsIgnoreCase(s.getType()))
                .collect(Collectors.toList());
    }
    return Response.ok(list).build();
}
```

Achieving this with path variables would require writing dozens of repetitive endpoint permutations.

---

## PART 4: Deep Nesting

### 4.1 Sub-Resource Locator Pattern

**Question:**  
Discuss the architectural benefits of the Sub-Resource Locator pattern. How does delegating logic to separate classes help manage complexity?

**Answer:**  
As an API grows, routing deeply nested paths inside a single parent controller turns the file into an unmaintainable massive Object. The Sub-Resource Locator pattern solves this by allowing the parent class to act as a router that delegates the request to a dedicated child class.

```java
// Inside SensorResource.java (Locator)
@Path("/{sensorId}/readings")
public ReadingsResource getReadingResource(@PathParam("sensorId") String sensorId) {
    return new ReadingsResource(sensorId);
}
```

```java
// Inside ReadingsResource.java (Child)
private final String sensorId;

public ReadingsResource(String sensorId) {
    this.sensorId = sensorId;
}
```

Because of this, every method inside `ReadingsResource` automatically operates within that specific sensor's context without needing the ID passed as a parameter.

---

##  PART 5: Error Handling & Logging

### 5.1 Dependency Validation (422 vs 404)

**Question:**  
Why is HTTP 422 often considered more semantically accurate than a standard 404 when the issue is a missing reference inside a valid JSON payload?

**Answer:**  
A standard `404 Not Found` implies that the target URI itself (e.g., `POST /api/v1/sensors`) does not exist on the server. If a client submits a perfectly formatted JSON payload, but the `roomId` inside that payload points to a room that doesn't exist, returning a `404` is highly misleading.

```java
Room room = Storage.getRooms().get(sensor.getRoomId());
if (room == null) {
    throw new LinkedResourceNotFoundException(
        "Room '" + sensor.getRoomId() + "' does not exist.");
}
```

By mapping this exception to a `422 Unprocessable Entity`, the API semantically informs the client that it understands the content type but the instructions inside the payload cannot be processed.

---

### 5.2 The Global Safety Net & Cybersecurity

**Question:**  
From a cybersecurity standpoint, explain the risks associated with exposing internal Java stack traces to external API consumers.

**Answer:**  
Exposing raw Java stack traces via default 500 error pages acts as a critical information disclosure vulnerability. Attackers analyze stack traces to map the system's exact internal file directory paths and uncover the exact version numbers of underlying libraries. Attackers cross-reference these library versions against CVE databases to execute targeted exploits. Implementing a `GlobalExceptionMapper` mitigates this by swallowing the trace, logging it privately to the console, and returning a sanitized, generic JSON error to the external client.

---

### 5.3 API Observability & Cross-Cutting Concerns

**Question:**  
Why is it advantageous to use JAX-RS filters for cross-cutting concerns like logging, rather than manually inserting `Logger.info()` statements inside every single resource method?

**Answer:**  
Logging is a cross-cutting concern—a piece of infrastructure logic that applies to the entire application. Utilizing JAX-RS `ContainerRequestFilter` and `ContainerResponseFilter` interfaces completely decouples this logic from the core business logic. If developers manually inserted `Logger.info()` into every resource method, it would heavily violate the DRY (Don't Repeat Yourself) principle and clutter the codebase. A JAX-RS filter guarantees that every single request and response is automatically and uniformly observed at the container level without relying on human memory.

---

## PART 5: Error Handling & Logging 

### 5.4 Consistent Error Response Structure

**Question:**  
Why is it important to maintain a consistent JSON error response structure across all endpoints?

**Answer:**  
A consistent error response structure ensures that client applications can reliably parse and handle errors without needing custom logic for each endpoint.

For example, a standardized error format might include:

- `status` (HTTP status code)
    
- `message` (human-readable description)
    
- `timestamp` (when the error occurred)
    

This consistency provides:

- ✔ Predictable client-side error handling
    
- ✔ Easier debugging and logging
    
- ✔ Cleaner frontend integration
    

Without a standard format, clients would need to write different parsing logic for different endpoints, increasing complexity and the risk of bugs.

---

### 5.5 Logging Levels & Production Readiness

**Question:**  
Why is it important to use appropriate logging levels (INFO, WARN, ERROR) in an API, especially in production systems?

**Answer:**  
Using proper logging levels is critical for maintaining observability and performance in production environments.

- **INFO** → General application flow (e.g., incoming requests)
    
- **WARN** → Unexpected but non-breaking issues
    
- **ERROR** → Failures and exceptions requiring attention
    

This structured logging allows:

- ✔ Efficient debugging (filter logs by severity)
    
- ✔ Reduced noise in production logs
    
- ✔ Faster issue detection and resolution
    

If all logs were written at the same level, it would be difficult to distinguish critical failures from normal operations, making monitoring systems far less effective.
