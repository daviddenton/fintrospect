+++
title = "mustache"
tags = ["web", "templating", "mustache"]
categories = ["fintrospect-core", "fintrospect-mustache"]
intro = ""
+++

```scala
case class MustacheView(name: String, age: Int) extends io.fintrospect.templating.View

object Mustache_Example extends App {

  import com.twitter.finagle.http.Method.Get
  import com.twitter.finagle.http.path.Root
  import com.twitter.finagle.http.{Request, Response}
  import com.twitter.finagle.{Filter, Http, Service}
  import com.twitter.util.Await.ready
  import io.fintrospect.formats.Html
  import io.fintrospect.parameters.Path
  import io.fintrospect.renderers.ModuleRenderer
  import io.fintrospect.templating.{MustacheTemplates, RenderView, TemplateRenderer, View}
  import io.fintrospect.{Module, RouteModule, RouteSpec, ServerRoute}

  def showAgeIn30(name: String, age: Int): Service[Request, View] =
    Service.mk[Request, View] { req => MustacheView(name, age + 30) }

  val templateRenderer: TemplateRenderer = MustacheTemplates.CachingClasspath(".")

  val convertViewToResponse: Filter[Request, Response, Request, View] = new RenderView(Html.ResponseBuilder, templateRenderer)

  val route: ServerRoute[Request, View] = RouteSpec()
    .at(Get) / Path.string("name") / Path.int("age") bindTo showAgeIn30

  val module: Module = RouteModule(Root, ModuleRenderer.Default, convertViewToResponse).withRoute(route)

  ready(Http.serve(":9999", module.toService))
}

//curl -v http://localhost:9999/david/100```