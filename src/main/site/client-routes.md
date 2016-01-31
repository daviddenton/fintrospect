# client routes
A RouteSpec can also be bound to a standard Finagle HTTP client and then called as a function, passing in the parameters which are bound to values by using the ```-->()``` or ```of()``` method. The client marshalls the passed parameters into an HTTP request and returns a Twitter Future containing the response. Any required manipulation of the Request (such as adding timeouts or caching headers) can be done in the standard way by chaining Filters to the Finagle HTTP client:
```
val employeeId = Path.integer("employeeId")
val name = Query.required.string("name")
val client: RouteClient = RouteSpec().taking(name).at(Get) / "employee" / employeeId bindToClient Http.newService("localhost:10000")
val response: Future[Response] = client(employeeId --> 1, name --> "")
```

### super-cool feature time: Auto-generation of Fake HTTP contracts
Because the RouteSpec objects can be used to bind to either a Server OR a Client, we can be spectacularly smug and use them on BOTH sides of an HTTP boundary to provide a typesafe remote contract, to either:
1. Auto-generate fake HTTP server implementations for remote HTTP dependencies. In this case, defining the RouteSpecs as part of an HTTP client contract and then simply reusing them as the server-side contract of a testing fake - see the "full" example code for - erm - an example! :)
2. Use a shared library approach to define a contract and the data objects that go across it for reuse in multiple applications, each of which import the shared library. This obviously binary-couples the applications together to a certain degree, so utmost care should be taken, backed up with sufficient CDC-style testing to ensure that the version of the contract deployed is valid on both ends.
