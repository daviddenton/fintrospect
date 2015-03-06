package io.github.daviddenton.fintrospect.simple

import argo.jdom.JsonNodeFactories.string
import argo.jdom.{JsonNode, JsonRootNode}
import io.github.daviddenton.fintrospect.ModuleRoute
import io.github.daviddenton.fintrospect.util.ArgoUtil._

object SimpleJson extends (Seq[ModuleRoute] => JsonRootNode) {
  private def render(mr: ModuleRoute): (String, JsonNode) = {
    mr.description.method + ":" + (mr.description.complete(mr.rootPath).toString :: mr.segmentMatchers.map(_.toString).toList).mkString("/") -> string(mr.description.value)
  }

  def apply(moduleRoutes: Seq[ModuleRoute]): JsonRootNode = obj("resources" -> obj(moduleRoutes.map(render)))
}
