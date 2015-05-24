package io.github.daviddenton.fintrospect.renderers.simplejson

import io.github.daviddenton.fintrospect.renderers.JsonModuleRendererTest

class SimpleJsonTest extends JsonModuleRendererTest {
  override def renderer = SimpleJson()
}
