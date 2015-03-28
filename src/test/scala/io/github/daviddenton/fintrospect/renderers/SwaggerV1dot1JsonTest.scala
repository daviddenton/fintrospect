package io.github.daviddenton.fintrospect.renderers

import io.github.daviddenton.fintrospect.Renderer

class SwaggerV1dot1JsonTest extends JsonRendererTest {
  override def name: String = "Swagger1dot1Json"
  override def renderer: Renderer = Swagger1dot1Json()
}
