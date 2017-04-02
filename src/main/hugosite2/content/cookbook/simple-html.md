+++
title = "html"
tags = ["web", "html"]
categories = ["recipe"]
+++

```scala
// fintrospect-core
object Simple_HTML_Example extends App {

  import com.twitter.finagle.http.Method.Get
  import com.twitter.finagle.http.path.Root
  import com.twitter.finagle.http.{Request, Response}
  import com.twitter.finagle.{Http, Service}
  import com.twitter.util.Await.ready
  import io.fintrospect.formats.Html.ResponseBuilder._
  import io.fintrospect.{Module, RouteModule, RouteSpec, ServerRoute}

  val serve: Service[Request, Response] = Service.mk[Request, Response] {
    req => {
      Ok(
        <html>
          <head>
            <title>The Title</title>
          </head>
          <body>Some content goes here</body>
        </html>.toString()
      )
    }
  }

  val route: ServerRoute[Request, Response] = RouteSpec().at(Get) bindTo serve

  val module: Module = RouteModule(Root).withRoute(route)

  ready(Http.serve(":9999", module.toService))
}

//curl -v http://localhost:9999/'
```