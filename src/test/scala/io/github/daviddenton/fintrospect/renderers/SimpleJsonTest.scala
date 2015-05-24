package io.github.daviddenton.fintrospect.renderers

import argo.jdom.JsonRootNode
import io.github.daviddenton.fintrospect.Renderer

class SimpleJsonTest extends JsonRendererTest {
  override def name: String = "SimpleJson"
  override def renderer: Renderer[JsonRootNode] = SimpleJson()
}
