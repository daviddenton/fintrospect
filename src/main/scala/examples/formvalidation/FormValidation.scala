package examples.formvalidation

import java.net.URL

import com.twitter.finagle.Http
import com.twitter.finagle.http.Request
import com.twitter.finagle.http.filter.Cors
import com.twitter.finagle.http.filter.Cors.HttpFilter
import com.twitter.finagle.http.path.Root
import io.fintrospect.ModuleSpec
import io.fintrospect.formats.Html
import io.fintrospect.renderers.SiteMapModuleRenderer
import io.fintrospect.templating.{MustacheTemplates, RenderView, View}

object FormValidation extends App {

  val renderView = new RenderView(Html.ResponseBuilder, MustacheTemplates.HotReload("src/main/resources"))

  val module = ModuleSpec[Request, View](Root, new SiteMapModuleRenderer(new URL("http://my.cool.app")), renderView)
    .withDescriptionPath(_ / "sitemap.xml")
    .withRoutes(new ReportAge())

  Http.serve(":8181", new HttpFilter(Cors.UnsafePermissivePolicy).andThen(module.toService))

  println("See the Sitemap description at: http://localhost:8181")

  Thread.currentThread().join()
}
