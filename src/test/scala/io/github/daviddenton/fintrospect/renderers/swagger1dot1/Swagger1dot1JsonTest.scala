package io.github.daviddenton.fintrospect.renderers.swagger1dot1

import io.github.daviddenton.fintrospect.renderers.ArgoJsonModuleRendererTest

class Swagger1dot1JsonTest extends ArgoJsonModuleRendererTest {
  override def renderer = Swagger1dot1Json()
}
