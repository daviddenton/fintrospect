+++
title = "client routes"
weight = 4
+++

A ```RouteSpec``` can also be bound to a standard Finagle HTTP client ```Service``` and then called as a function, passing in the parameters which 
are bound to values by using the ```-->()``` or ```of()``` method. The client marshalls the passed parameters into an HTTP request and 
returns a Twitter ```Future``` containing the response. Any required manipulation of the ```Request``` (such as adding timeouts or caching 
headers) can be done in the standard way by chaining a ```Filter``` to the client ```Service```. Note that ```Content-Type``` headers for posted HTTP 
bodies is already handled by the bound ```Body``` instance.:

```
val employeeId = Path.integer("employeeId")
val name = Query.required.string("name")
val client: RouteClient = RouteSpec()
                            .taking(name)
                            .at(Get) / "employee" / employeeId bindToClient Http.newService("localhost:10000")
val response: Future[Response] = client(employeeId --> 1, name --> "")
```

### super-cool feature time: reuse of HTTP contracts
Because the ```RouteSpec``` objects can be used to bind to either a Server or a Client, we can be spectacularly smug and use them on 
both sides of an HTTP boundary to provide a type-safe remote contract, to either:

1. Auto-generate fake HTTP server implementations for remote HTTP dependencies. In this case, defining the ```RouteSpec``` as part of 
an HTTP client contract and then simply reusing them as the server-side contract of a testing fake - see the ```/clients``` example code
2. Use a shared library approach to define a contract and the data objects that go across it for reuse in multiple applications, each 
of which import the shared library. This obviously binary-couples the applications together to a certain degree, so utmost care should 
be taken, backed up with sufficient CDC-style testing to ensure that the version of the contract deployed is valid on both ends.

<a class="next" href="http://fintrospect.io/building-http-responses"><button type="button" class="btn btn-sm btn-default">next: building http responses</button></a>
