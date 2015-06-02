package io.fintrospect.renderers.simplejson

import com.twitter.finagle.http.path.Path
import io.fintrospect.Route
import io.fintrospect.parameters.RequestParameter
import io.fintrospect.renderers.{JsonBadRequestRenderer, ModuleRenderer}
import io.fintrospect.util.ArgoUtil._
import io.fintrospect.util.JsonResponseBuilder._
import org.jboss.netty.handler.codec.http.HttpResponse

/**
 * Ultra-basic ModuleRenderer implementation that only supports the route paths and the main descriptions of each.
 */
class SimpleJson extends ModuleRenderer {
  override def badRequest(badParameters: List[RequestParameter[_]]): HttpResponse = JsonBadRequestRenderer(badParameters)

  private def render(basePath: Path, route: Route): Field = {
    route.method + ":" + route.describeFor(basePath) -> string(route.describedRoute.summary)
  }

  override def description(basePath: Path, routes: Seq[Route]): HttpResponse = Ok(obj("resources" -> obj(routes.map(r => render(basePath, r)))))
}

object SimpleJson {
  def apply() = new SimpleJson()
}