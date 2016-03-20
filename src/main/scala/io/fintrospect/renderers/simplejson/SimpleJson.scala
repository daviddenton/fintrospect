package io.fintrospect.renderers.simplejson

import com.twitter.finagle.http.Status.Ok
import com.twitter.finagle.http.path.Path
import com.twitter.finagle.http.{Request, Response}
import io.fintrospect.ServerRoute
import io.fintrospect.formats.json.Argo
import io.fintrospect.formats.json.Argo.JsonFormat.{Field, obj}
import io.fintrospect.formats.json.Argo.ResponseBuilder.{responseBuilderToResponse, statusToResponseBuilderConfig}
import io.fintrospect.parameters.{Parameter, Security}
import io.fintrospect.renderers.{JsonErrorResponseRenderer, ModuleRenderer}

/**
 * Ultra-basic ModuleRenderer implementation that only supports the route paths and the main descriptions of each.
 */
class SimpleJson extends ModuleRenderer {
  override def badRequest(badParameters: Seq[Parameter]): Response = JsonErrorResponseRenderer.badRequest(badParameters)

  override def notFound(request: Request): Response = JsonErrorResponseRenderer.notFound()

  private def render(basePath: Path, route: ServerRoute[_, _]): Field = {
    route.method.toString() + ":" + route.describeFor(basePath) -> Argo.JsonFormat.string(route.routeSpec.summary)
  }

  override def description(basePath: Path, security: Security, routes: Seq[ServerRoute[_, _]]): Response = Ok(obj("resources" -> obj(routes.map(r => render(basePath, r)))))
}

object SimpleJson {
  def apply() = new SimpleJson()
}