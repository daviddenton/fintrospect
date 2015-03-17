package io.github.daviddenton.fintrospect.renderers

import argo.jdom.JsonNodeFactories.string
import argo.jdom.{JsonNode, JsonRootNode}
import io.github.daviddenton.fintrospect.{Renderer, ModuleRoute}
import io.github.daviddenton.fintrospect.util.ArgoUtil._

class SimpleJson private() extends Renderer {
  private def render(mr: ModuleRoute): (String, JsonNode) = {
    mr.on.method + ":" + mr.toString -> string(mr.description.name)
  }

  def apply(mr: Seq[ModuleRoute]): JsonRootNode = obj("resources" -> obj(mr.map(render)))
}

object SimpleJson {
  def apply(): Renderer = new SimpleJson()
}