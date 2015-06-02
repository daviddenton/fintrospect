package io.fintrospect.renderers.simplejson

import io.fintrospect.renderers.ArgoJsonModuleRendererTest

class SimpleJsonTest extends ArgoJsonModuleRendererTest {
  override def renderer = SimpleJson()
}
