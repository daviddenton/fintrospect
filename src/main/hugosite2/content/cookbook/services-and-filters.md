+++
title = "services and filters"
tags = ["service", "filter", "finagle api"]
categories = ["recipe"]
+++

At it's core, Finagle relies on 2 concepts for implementing HTTP services, `Service` and `Filter`. 
A Service is effectively just a simple function (although technically it's an abstract class with several utility methods). This type is symmetrically used for both incoming and outgoing HTTP services - which is both neat and advantageous for 
reasons we'll come to later. Service represent the endpoint of a request processing chain and is generic in it's input and output types:
```scala
import com.twitter.util.Future

trait Service[Req, Resp] extends (Req => Future[Resp])
```

Services can easily be created by extending `com.twitter.finagle.Service`, or just using the utility method `mk()`:
```scala
import com.twitter.finagle.Service
import com.twitter.util.Future

val svc = Service.mk[String, Int] { in => Future(in.toInt * 2) }
val futureInt = svc("1234")
```

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
import com.twitter.finagle.Filter
import com.twitter.finagle.Service
import com.twitter.util.Future

val convert = Filter.mk[String, String, Int, Int] { 
  (in, next) => next(in.toInt).map(_.toString) 
}
val square = Filter.mk[Int, Int, Int, Int] { 
  (in, next) => next(in * in) 
}

val futureSquareThenDouble: Service[String, String] = convert.andThen(square).andThen(Service.mk { i: Int => Future(i * 2)})
```

