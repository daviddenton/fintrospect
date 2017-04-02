+++
title = "custom security"
tags = ["security", "filter", "contract"]
categories = ["recipe"]
+++

```scala

// fintrospect-core
object Security_Custom_Example extends App {

  import com.twitter.finagle.http.Method.Get
  import com.twitter.finagle.http.path.Root
  import com.twitter.finagle.http.{Request, Response}
  import com.twitter.finagle.{Http, Service}
  import com.twitter.util.Await.ready
  import com.twitter.util.Future
  import io.fintrospect.formats.PlainText.ResponseBuilder._
  import io.fintrospect.parameters.Query
  import io.fintrospect.{ApiKey, Module, RouteModule, RouteSpec, Security, ServerRoute}

  val svc: Service[Request, Response] = Service.mk[Request, Response] { req => Ok(s"hello!") }

  val route: ServerRoute[Request, Response] = RouteSpec().at(Get) bindTo svc

  val securityFilter: Service[String, Boolean] = Service.mk[String, Boolean] { r => Future(r == "secret") }
  val apiKey: Security = ApiKey(Query.required.string("token"), securityFilter)

  val module: Module = RouteModule(Root).securedBy(apiKey).withRoute(route)

  ready(Http.serve(":9999", module.toService))
}

//curl -v http://localhost:9999?token=notTheSecret
//curl -v http://localhost:9999?token=secret
```