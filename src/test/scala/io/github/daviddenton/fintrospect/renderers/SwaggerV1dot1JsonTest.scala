package io.github.daviddenton.fintrospect.renderers

import argo.jdom.JsonRootNode
import io.github.daviddenton.fintrospect.Renderer

class SwaggerV1dot1JsonTest extends JsonRendererTest {
  override def name: String = "Swagger1dot1Json"
  override def renderer: Renderer[JsonRootNode] = Swagger1dot1Json()
}
