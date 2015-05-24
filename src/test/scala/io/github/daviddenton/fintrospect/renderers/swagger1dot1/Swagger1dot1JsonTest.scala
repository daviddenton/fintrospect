package io.github.daviddenton.fintrospect.renderers.swagger1dot1

import io.github.daviddenton.fintrospect.renderers.JsonModuleRendererTest

class Swagger1dot1JsonTest extends JsonModuleRendererTest {
  override def renderer = Swagger1dot1Json()
}
