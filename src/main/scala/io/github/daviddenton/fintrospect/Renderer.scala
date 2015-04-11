package io.github.daviddenton.fintrospect

import argo.jdom.JsonRootNode

/**
 * Contract trait for the pluggable Renderers (Swagger etc..)
 */
trait Renderer {
  def apply(routes: Seq[ModuleRoute]): JsonRootNode
}
