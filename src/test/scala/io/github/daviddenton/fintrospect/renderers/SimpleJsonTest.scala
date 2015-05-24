package io.github.daviddenton.fintrospect.renderers

import argo.jdom.JsonRootNode
import io.github.daviddenton.fintrospect.DescriptionRenderer

class SimpleJsonTest extends JsonDescriptionRendererTest {
  override def name: String = "SimpleJson"
  override def renderer: DescriptionRenderer[JsonRootNode] = SimpleJson()
}
