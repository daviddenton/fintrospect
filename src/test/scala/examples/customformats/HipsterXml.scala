package examples.customformats

import com.twitter.finagle.httpx.path.Path
import com.twitter.finagle.httpx.{Response, Status}
import examples.customformats.HipsterXmlResponseBuilder._
import io.fintrospect.ServerRoute
import io.fintrospect.parameters.Parameter
import io.fintrospect.renderers.ModuleRenderer

/**
 * Hyper-cool, next-gen, markup used by all true rockstar coderzzzz
 */
object HipsterXml extends ModuleRenderer {

  override def badRequest(badParameters: Seq[Parameter]): Response =
    Error(Status.BadRequest, badParameters.toString())

  private def renderRoute(basePath: Path, route: ServerRoute): HipsterXmlFormat = HipsterXmlFormat(s"<entry>${route.method}:${route.describeFor(basePath)}</entry>")

  private def renderRoutes(basePath: Path, routes: Seq[ServerRoute]): String = HipsterXmlFormat(routes.map(renderRoute(basePath, _)): _*).toString()

  override def description(basePath: Path, routes: Seq[ServerRoute]): Response = {
    Ok(HipsterXmlFormat(s"<paths>${renderRoutes(basePath, routes)}</paths>").value)
  }
}
