package io.github.daviddenton.fintrospect

import argo.jdom.{JsonNode, JsonRootNode}
import io.github.daviddenton.fintrospect.swagger.{SwParameter, SwPathMethod, SwResponse}

trait Renderer {

  def render(mr: Seq[ModuleRoute]): JsonRootNode

  def render(mr: ModuleRoute): (String, JsonNode)

  def render(p: SwParameter): JsonNode

  def render(pm: SwPathMethod): (String, JsonNode)

  def render(r: SwResponse): (Int, JsonNode)
}


