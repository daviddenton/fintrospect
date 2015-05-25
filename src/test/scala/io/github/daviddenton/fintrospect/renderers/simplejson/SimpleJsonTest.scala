package io.github.daviddenton.fintrospect.renderers.simplejson

import io.github.daviddenton.fintrospect.renderers.ArgoJsonModuleRendererTest

class SimpleJsonTest extends ArgoJsonModuleRendererTest {
  override def renderer = SimpleJson()
}
