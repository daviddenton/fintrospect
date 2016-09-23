package io.fintrospect.renderers.simplejson

import com.twitter.finagle.http.Status.Ok
import com.twitter.finagle.http.path.Path
import com.twitter.finagle.http.{Request, Response}
import io.fintrospect.formats.Argo
import io.fintrospect.formats.Argo.JsonFormat.{Field, obj}
import io.fintrospect.formats.Argo.ResponseBuilder.implicits.{responseBuilderToResponse, statusToResponseBuilderConfig}
import io.fintrospect.renderers.{JsonErrorResponseRenderer, ModuleRenderer}
import io.fintrospect.util.ExtractionError
import io.fintrospect.{Security, ServerRoute}

/**
  * Ultra-basic ModuleRenderer implementation that only supports the route paths and the main descriptions of each.
  */
class SimpleJson extends ModuleRenderer {
  override def badRequest(badParameters: Seq[ExtractionError]): Response = JsonErrorResponseRenderer.badRequest(badParameters)

  override def notFound(request: Request): Response = JsonErrorResponseRenderer.notFound()

  private def render(basePath: Path, route: ServerRoute[_, _]): Field =
    route.method.toString() + ":" + route.describeFor(basePath) -> Argo.JsonFormat.string(route.routeSpec.summary)

  override def description(basePath: Path, security: Security, routes: Seq[ServerRoute[_, _]]): Response = Ok(obj("resources" -> obj(routes.map(r => render(basePath, r)))))
}

object SimpleJson {
  def apply() = new SimpleJson()
}