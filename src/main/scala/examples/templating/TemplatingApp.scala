package examples.templating

import java.net.URL

import com.twitter.finagle.http.Method.Get
import com.twitter.finagle.http.Request
import com.twitter.finagle.http.filter.Cors
import com.twitter.finagle.http.filter.Cors.HttpFilter
import com.twitter.finagle.http.path.Root
import com.twitter.finagle.{Http, Service}
import com.twitter.util.Await
import io.fintrospect.formats.PlainText
import io.fintrospect.renderers.SiteMapModuleRenderer
import io.fintrospect.templating.{MustacheTemplates, RenderView}
import io.fintrospect.{ModuleSpec, RouteSpec}

object TemplatingApp extends App {

  val devMode = true
  val renderer = if (devMode) MustacheTemplates.HotReload("src/main/resources") else MustacheTemplates.CachingClasspath(".")

  val renderView = new RenderView(PlainText.ResponseBuilder, renderer)

  val module = ModuleSpec(Root, new SiteMapModuleRenderer(new URL("http://my.cool.app")), renderView)
    .withRoute(RouteSpec().at(Get) / "echo" bindTo Service.mk { rq: Request => MustacheView(rq.uri) })

  println("See the Sitemap description at: http://localhost:8181")

  Await.ready(
    Http.serve(":8181", new HttpFilter(Cors.UnsafePermissivePolicy).andThen(module.toService))
  )
}
