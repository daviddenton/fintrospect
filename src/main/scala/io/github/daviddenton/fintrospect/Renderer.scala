package io.github.daviddenton.fintrospect

import argo.jdom.JsonRootNode

trait Renderer {
  def apply(routes: Seq[ModuleRoute]): JsonRootNode
}
