package examples.customformats

import com.twitter.finagle.http.path.Path
import examples.customformats.XmlResponseBuilder._
import io.fintrospect.Route
import io.fintrospect.parameters.Parameter
import io.fintrospect.renderers.ModuleRenderer
import org.jboss.netty.handler.codec.http.{HttpResponse, HttpResponseStatus}

/**
 * Hyper-cool, next-gen, markup used by all true rockstar coderzzzz
 */
object Xml extends ModuleRenderer {

  override def badRequest(badParameters: Seq[Parameter]): HttpResponse =
    Error(HttpResponseStatus.BAD_REQUEST, badParameters.toString())

  private def renderRoute(basePath: Path, route: Route): XmlFormat = XmlFormat(s"<entry>${route.method}:${route.describeFor(basePath)}</entry>")

  private def renderRoutes(basePath: Path, routes: Seq[Route]): String = XmlFormat(routes.map(renderRoute(basePath, _)): _*).toString()

  override def description(basePath: Path, routes: Seq[Route]): HttpResponse = {
    Ok(XmlFormat(s"<paths>${renderRoutes(basePath, routes)}</paths>").value)
  }
}
