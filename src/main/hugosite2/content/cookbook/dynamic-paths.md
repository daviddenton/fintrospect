+++
title = "dynamic path elements"
tags = ["path", "basics", "contract", "route"]
categories = ["recipe"]
intro = "asd"
+++

```scala

// fintrospect-core
object Dynamic_Paths_Example extends App {

  import com.twitter.finagle.http.Method.Get
  import com.twitter.finagle.http.path.Root
  import com.twitter.finagle.http.{Request, Response}
  import com.twitter.finagle.{Http, Service}
  import com.twitter.util.Await.ready
  import io.fintrospect.formats.PlainText.ResponseBuilder._
  import io.fintrospect.parameters.Path
  import io.fintrospect.{Module, RouteModule, RouteSpec, ServerRoute}

  def sayHello(name: String): Service[Request, Response] = Service.mk[Request, Response] { req => Ok(s"hello $name!") }

  val route: ServerRoute[Request, Response] = RouteSpec().at(Get) / Path.string("name") bindTo say```scala
```  val module: Module = RouteModule(Root).withRoute(route)

  ready(Http.serve(":9999", module.toService))
}

// curl -v http://localhost:9999/david
```