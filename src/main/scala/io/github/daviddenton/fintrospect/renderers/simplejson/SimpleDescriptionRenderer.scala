package io.github.daviddenton.fintrospect.renderers.simplejson

import argo.jdom.JsonNodeFactories.string
import argo.jdom.JsonRootNode
import com.twitter.finagle.http.path.Path
import io.github.daviddenton.fintrospect.renderers.DescriptionRenderer
import io.github.daviddenton.fintrospect.util.ArgoUtil._
import io.github.daviddenton.fintrospect.Route

/**
 * Ultra-basic DescriptionRenderer (Argo JSON) implementation that only supports the route paths and the main descriptions of each.
 */
class SimpleDescriptionRenderer extends DescriptionRenderer[JsonRootNode] {
  private def render(basePath: Path, route: Route): Field = {
    route.method + ":" + route.describeFor(basePath) -> string(route.describedRoute.summary)
  }

  def apply(basePath: Path, routes: Seq[Route]): JsonRootNode = obj("resources" -> obj(routes.map(r => render(basePath, r))))
}
