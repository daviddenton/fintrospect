package io.github.daviddenton.fintrospect.renderers.swagger2dot0

import io.github.daviddenton.fintrospect.ApiInfo
import io.github.daviddenton.fintrospect.renderers.ArgoJsonModuleRendererTest

class Swagger2dot0JsonTest extends ArgoJsonModuleRendererTest {
  override def renderer = Swagger2dot0Json(ApiInfo("title", "1.2", Some("module description")))
}

