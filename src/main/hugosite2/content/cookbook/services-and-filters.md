+++
title = "services and filters"
tags = ["server", "client", "service", "filter", "finagle api"]
categories = ["recipe"]
intro = "asd"
+++

At it's core, Finagle relies on 2 concepts for implementing HTTP services, `Service` and `Filter`. These are documented [elsewhere](#reading). but elow is a brief overview with code examples.

## Services
A Service is effectively just a simple function (although technically it's an abstract class with several utility methods) that represents a service boundary (the endpoint of a request processing chain). It is generic in it's input and output types:
```scala
import com.twitter.util.Future

trait Service[Req, Resp] extends (Req => Future[Resp])
```

Services can easily be created by extending `com.twitter.finagle.Service`, or just using the utility method `mk()`:
```scala
import com.twitter.finagle.Service
import com.twitter.util.Future

val svc = Service.mk[String, Int] { in => Future(in.toInt * 2) }
val futureInt = svc("1234") // eventually yields 2468
```
<br/>

#### Usage in Finagle/Fintrospect
The Service API is used symmetrically to provide both incoming HTTP endpoints and HTTP clients.

Here is the only code required to create a simple HTTP server using the Finagle API:

```scala
import com.twitter.finagle.{Http, Service}
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.util.{Await, Future}

val server = Http.serve(":9999", Service.mk { in:Request => Future(Response(Status.Ok)) })

Await.ready(server) // block forever

// curl -v http://localhost:9999
```

And here is the equivalent for creating an HTTP client. See the [Client recipe](../http-clients) for more examples:
```scala
import com.twitter.finagle.{Http, Service}
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.util.Future

val client = Http.newService("pokeapi.co:80")
val futureOk = client(Request("/")) // eventually yields some JSON from the pokemon api
```

## Filters
Filters are also just effectively simple functions. Their purpose is to apply inject transformations or side-effects into the request processing chain and as such have 2 pairs of request/response generics (the first representing the "outer" pair, 
the second representing the "Inner" pair):
```scala
import com.twitter.finagle.Service
import com.twitter.util.Future

trait Filter[ReqIn, RespOut, ReqOut, RespIn] {
  def apply(request: ReqIn, service: Service[ReqOut, RespIn]) : Future[RespOut]
}
```
In an HTTP context, examples of such operations are: 

- checking the queries of an incoming request and immediately returning a BadRequest response if any are missing
- adding caching headers to an outgoing response
- transform the incoming request body into a JSON document to be 
- measuring and reporting the latency of a service call

Filters can easily be created by extending `com.twitter.finagle.Filter`, or just using the utility method `mk()`. Similar to composing functions, Filters can be chained together to provide reusable "blocks" of functionality
and then combined with a Service:
```scala
import com.twitter.finagle.{Filter, Service}
import com.twitter.util.Future

val convert = Filter.mk[String, String, Int, Int] { 
  (in, next) => next(in.toInt).map(_.toString) 
}
val square = Filter.mk[Int, Int, Int, Int] { 
  (in, next) => next(in * in) 
}

val svc: Service[String, String] = convert
                                    .andThen(square)
                                    .andThen(Service.mk { i: Int => Future(i * 2)})
val futureInt = svc("10") // eventually yields 200
```

## Further reading
<a name="reading"></a>

- Finagle Services & Filters [guide](https://twitter.github.io/finagle/guide/ServicesAndFilters.html)
- Paper: ["Your server as a function"](https://monkey.org/~marius/funsrv.pdf)