package io.github.daviddenton.fintrospect.renderers

import io.github.daviddenton.fintrospect.Renderer

class SimpleJsonTest extends JsonRendererTest {
  override def name: String = "SimpleJson"
  override def renderer: Renderer = SimpleJson()
}
