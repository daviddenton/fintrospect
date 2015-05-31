package io.github.daviddenton.fintrospect.renderers.simplejson

import io.github.daviddenton.fintrospect.renderers.{ArgoJsonModuleRenderer, ModuleRenderer}

/**
 * Ultra-basic ModuleRenderer implementation that only supports the route paths and the main descriptions of each.
 */
object SimpleJson {
  def apply(): ModuleRenderer = new ArgoJsonModuleRenderer(new SimpleDescriptionRenderer())
}
