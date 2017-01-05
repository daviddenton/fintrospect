package examples.customformats

import com.twitter.finagle.http.path.Path
import com.twitter.finagle.http.{Request, Response}
import examples.customformats.HipsterXml.ResponseBuilder._
import io.fintrospect.renderers.ModuleRenderer
import io.fintrospect.util.ExtractionError
import io.fintrospect.{Security, ServerRoute}

/**
  * Hyper-cool, next-gen, markup used by all true rockstar coderzzzz
  */
object HipsterXmlModuleRenderer extends ModuleRenderer {

  override def badRequest(badParameters: Seq[ExtractionError]): Response = BadRequest(badParameters.toString())

  override def notFound(request: Request): Response = NotFound("hey")

  private def renderRoute(basePath: Path, route: ServerRoute[_, _]): HipsterXmlFormat = HipsterXmlFormat(s"<entry>${route.method}:${route.describeFor(basePath)}</entry>")

  private def renderRoutes(basePath: Path, routes: Seq[ServerRoute[_, _]]): String = HipsterXmlFormat(routes.map(renderRoute(basePath, _)): _*).toString()

  override def description(basePath: Path, security: Security, routes: Seq[ServerRoute[_, _]]): Response =
    Ok(HipsterXmlFormat(s"<paths>${renderRoutes(basePath, routes)}</paths>").value)
}

