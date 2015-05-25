package io.github.daviddenton.fintrospect.renderers.simplejson

import argo.jdom.JsonRootNode
import io.github.daviddenton.fintrospect.renderers.{ArgoJsonModuleRenderer, ModuleRenderer}

/**
 * Ultra-basic ModuleRenderer implementation that only supports the route paths and the main descriptions of each.
 */
object SimpleJson {
  def apply(): ModuleRenderer[JsonRootNode] = new ArgoJsonModuleRenderer(new SimpleDescriptionRenderer())
}
