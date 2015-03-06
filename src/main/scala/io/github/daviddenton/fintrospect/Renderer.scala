package io.github.daviddenton.fintrospect

import argo.jdom.JsonRootNode

trait Renderer {
  def render(mr: Seq[ModuleRoute]): JsonRootNode
}


