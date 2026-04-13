Part 1: Service Architecture & Setup

Question: In your report, explain the default lifecycle of a JAX-RS Resource class. Is a
new instance instantiated for every incoming request, or does the runtime treat it as a
singleton? Elaborate on how this architectural decision impacts the way you manage and
synchronize your in-memory data structures (maps/lists) to prevent data loss or race con
ditions

Answers:
JAX-RS Resource Lifecycle -
By default, the JAX-RS runtime treats resource classes (such aDiscoveryResource) as request-scoped.
This means a completely new instance of the class is instantiated for every single incoming HTTP request, and it is destroyed as soon as the response is sent back to the client. It is not treated as a singleton by default.

Management of data structures and preventing data loss and race conditions -
Because the resource instances are destroyed after every request, we cannot use standard instance variables to store our application data (like lists of rooms). If we did, the data would be wiped clean every time a user made a request. To prevent data loss, our in-memory data structures must be declared as static (so they belong to the server's memory, not the temporary request object). Web servers handle multiple requests at the exact same time. If two users try to add a room at the exact same millisecond, accessing a standard static HashMap could cause data corruption or a server crash. To prevent these race conditions, our shared data structures must be synchronized, ideally by using thread-safe collections like ConcurrentHashMap

Question: Why is the provision of ”Hypermedia” (links and navigation within responses)
considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach
benefit client developers compared to static documentation?

Answers:
Providing Hypermedia is the defining characteristic of the highest level of REST API maturity, known as HATEOAS (Hypermedia As The Engine Of Application State)
HATEOAS makes the API self-descriptive. Instead of a client developer needing to rely heavily on static, external documentation to figure out what URLs to type, the server provides a map of available actions inside the JSON response itself.

This approach significantly benefits developers because it decouples the client application from the server's routing structure. The backend engineers can change the internal URLs in the future, and as long as they update the hypermedia links in the discovery payload, the client applications will continue to navigate the API seamlessly without breaking.


Part 2: Room Management

Question: When returning a list of rooms, what are the implications of returning only
IDs versus returning the full room objects? Consider network bandwidth and client side
processing.

Answers: 



Question: IstheDELETEoperationidempotentinyourimplementation? Provideadetailed
justification by describingwhathappensifaclientmistakenlysendstheexactsameDELETE
request for a room multiple times.