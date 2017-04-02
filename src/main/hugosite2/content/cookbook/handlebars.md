+++
title = "handlebars"
tags = ["web", "templating", "handlebars"]
categories = ["fintrospect-core", "fintrospect-handlebars"]
intro = ""
+++

```scala
case class HandlebarsView(name: String, age: Int) extends io.fintrospect.templating.View

object Handlebars_Example extends App {

  import com.twitter.finagle.http.Method.Get
  import com.twitter.finagle.http.path.Root
  import com.twitter.finagle.http.{Request, Response}
  import com.twitter.finagle.{Http, Service}
  import com.twitter.util.Await.ready
  import io.fintrospect.formats.Html
  import io.fintrospect.parameters.Path
  import io.fintrospect.templating.{HandlebarsTemplates, RenderView, View}
  import io.fintrospect.{Module, RouteModule, RouteSpec, ServerRoute}

  def showAgeIn30(name: String, age: Int): Service[Request, Response] = {
    val svc = Service.mk[Request, View] { req => HandlebarsView(name, age + 30) }

    new RenderView(Html.ResponseBuilder, HandlebarsTemplates.CachingClasspath(".")).andThen(svc)
  }

  val route: ServerRoute[Request, Response] = RouteSpec()
    .at(Get) / Path.string("name") / Path.int("age") bindTo showAgeIn30

  val module: Module = RouteModule(Root).withRoute(route)

  ready(Http.serve(":9999", module.toService))
}

//curl -v http://localhost:9999/david/100
```
