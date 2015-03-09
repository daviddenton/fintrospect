package io.github.daviddenton.fintrospect.renderers

import argo.jdom.JsonNodeFactories.string
import argo.jdom.{JsonNode, JsonRootNode}
import io.github.daviddenton.fintrospect.ModuleRoute
import io.github.daviddenton.fintrospect.util.ArgoUtil._

object SimpleJson {
  private def render(mr: ModuleRoute): (String, JsonNode) = {
    mr.on.method + ":" + mr.toString -> string(mr.description.value)
  }

  def apply(): Seq[ModuleRoute] => JsonRootNode = mr => obj("resources" -> obj(mr.map(render)))
}
