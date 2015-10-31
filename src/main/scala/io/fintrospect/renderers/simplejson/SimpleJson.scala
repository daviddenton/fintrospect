package io.fintrospect.renderers.simplejson

import com.twitter.finagle.http.Response
import com.twitter.finagle.http.path.Path
import io.fintrospect.ServerRoute
import io.fintrospect.formats.json.Argo
import io.fintrospect.formats.json.Argo.JsonFormat._
import io.fintrospect.formats.json.Argo.ResponseBuilder._
import io.fintrospect.parameters.Parameter
import io.fintrospect.renderers.{JsonBadRequestRenderer, ModuleRenderer}

/**
 * Ultra-basic ModuleRenderer implementation that only supports the route paths and the main descriptions of each.
 */
class SimpleJson extends ModuleRenderer {
  override def badRequest(badParameters: Seq[Parameter]): Response = JsonBadRequestRenderer(badParameters)

  private def render(basePath: Path, route: ServerRoute): Field = {
    route.method.toString() + ":" + route.describeFor(basePath) -> Argo.JsonFormat.string(route.routeSpec.summary)
  }

  override def description(basePath: Path, routes: Seq[ServerRoute]): Response = OK(obj("resources" -> obj(routes.map(r => render(basePath, r)))))
}

object SimpleJson {
  def apply() = new SimpleJson()
}