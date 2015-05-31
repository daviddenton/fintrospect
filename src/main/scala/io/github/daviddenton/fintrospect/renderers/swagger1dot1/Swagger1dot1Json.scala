package io.github.daviddenton.fintrospect.renderers.swagger1dot1

import io.github.daviddenton.fintrospect.renderers.{ArgoJsonModuleRenderer, ModuleRenderer}

/**
 * ModuleRenderer that provides basic Swagger v1.1 support. No support for bodies or schemas.
 */
object Swagger1dot1Json {
  def apply(): ModuleRenderer = new ArgoJsonModuleRenderer(new Swagger1dot1DescriptionRenderer())
}
