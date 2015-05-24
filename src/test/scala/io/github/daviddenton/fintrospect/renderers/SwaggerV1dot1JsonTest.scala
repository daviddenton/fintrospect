package io.github.daviddenton.fintrospect.renderers

import argo.jdom.JsonRootNode
import io.github.daviddenton.fintrospect.DescriptionRenderer

class SwaggerV1dot1JsonTest extends JsonDescriptionRendererTest {
  override def name: String = "Swagger1dot1Json"
  override def renderer: DescriptionRenderer[JsonRootNode] = Swagger1dot1Json()
}
