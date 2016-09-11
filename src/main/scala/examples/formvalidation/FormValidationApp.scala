package examples.formvalidation

import java.net.URL

import com.twitter.finagle.Http
import com.twitter.finagle.http.filter.Cors
import com.twitter.finagle.http.filter.Cors.HttpFilter
import com.twitter.finagle.http.path.Root
import com.twitter.util.Await
import io.fintrospect.ModuleSpec
import io.fintrospect.formats.Html
import io.fintrospect.renderers.SiteMapModuleRenderer
import io.fintrospect.templating.{MustacheTemplates, RenderView}

/**
  * This example shows how to use Body.webform() and a templating engine to construct a validating form, with custom messages
  * for each field.
  */
object FormValidationApp extends App {

  val renderView = new RenderView(Html.ResponseBuilder, MustacheTemplates.HotReload("src/main/resources"))

  val module = ModuleSpec(Root, new SiteMapModuleRenderer(new URL("http://my.cool.app")), renderView)
    .withDescriptionPath(_ / "sitemap.xml")
    .withRoutes(new ReportAge(new GreetingDatabase))


  println("See the validating form at: http://localhost:8181")

  Await.ready(
    Http.serve(":8181", new HttpFilter(Cors.UnsafePermissivePolicy).andThen(module.toService))
  )
}
