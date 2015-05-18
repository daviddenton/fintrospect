package io.github.daviddenton.fintrospect.renderers

import argo.jdom.JsonNodeFactories.string
import argo.jdom.JsonRootNode
import com.twitter.finagle.http.path.Path
import io.github.daviddenton.fintrospect.util.ArgoUtil._
import io.github.daviddenton.fintrospect.{Renderer, Route}

class SimpleJson private() extends Renderer {
  private def render(basePath: Path, route: Route): Field = {
    route.method + ":" + route.describeFor(basePath) -> string(route.describedRoute.summary)
  }

  def apply(basePath: Path, routes: Seq[Route]): JsonRootNode = obj("resources" -> obj(routes.map(r => render(basePath, r))))
}

/**
 * Ultra-basic Renderer implementation that only supports the route paths and the main descriptions of each.
 */
object SimpleJson {
  def apply(): Renderer = new SimpleJson()
}
