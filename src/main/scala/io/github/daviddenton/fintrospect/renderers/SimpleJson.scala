package io.github.daviddenton.fintrospect.renderers

import argo.jdom.JsonNodeFactories.string
import argo.jdom.JsonRootNode
import io.github.daviddenton.fintrospect.util.ArgoUtil._
import io.github.daviddenton.fintrospect.{ModuleRoute, Renderer}

/**
 * Ultra-basic renderer implementation that only supports the route paths and the main descriptions of each.
 */
class SimpleJson private() extends Renderer {
  private def render(mr: ModuleRoute): Field = {
    mr.on.method + ":" + mr.toString -> string(mr.description.name)
  }

  def apply(mr: Seq[ModuleRoute]): JsonRootNode = obj("resources" -> obj(mr.map(render)))
}

object SimpleJson {
  def apply(): Renderer = new SimpleJson()
}