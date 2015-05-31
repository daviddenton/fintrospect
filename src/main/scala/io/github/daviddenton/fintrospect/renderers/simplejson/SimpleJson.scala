package io.github.daviddenton.fintrospect.renderers.simplejson

import com.twitter.finagle.http.path.Path
import io.github.daviddenton.fintrospect.Route
import io.github.daviddenton.fintrospect.parameters.RequestParameter
import io.github.daviddenton.fintrospect.renderers.{JsonBadRequestRenderer, ModuleRenderer}
import io.github.daviddenton.fintrospect.util.ArgoUtil._
import io.github.daviddenton.fintrospect.util.JsonResponseBuilder._
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