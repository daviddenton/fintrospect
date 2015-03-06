package io.github.daviddenton.fintrospect.renderers

import argo.jdom.JsonNode
import argo.jdom.JsonNodeFactories.string
import io.github.daviddenton.fintrospect.FintrospectModule._
import io.github.daviddenton.fintrospect.ModuleRoute
import io.github.daviddenton.fintrospect.util.ArgoUtil._

object SimpleJson {
  private def render(mr: ModuleRoute): (String, JsonNode) = {
    mr.description.method + ":" + mr.toString -> string(mr.description.value)
  }

  def apply(): Renderer = mr => obj("resources" -> obj(mr.map(render)))
}
