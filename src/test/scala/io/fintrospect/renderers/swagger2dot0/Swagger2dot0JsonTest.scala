package io.fintrospect.renderers.swagger2dot0

import io.fintrospect.ApiInfo
import io.fintrospect.renderers.ArgoJsonModuleRendererTest

class Swagger2dot0JsonTest extends ArgoJsonModuleRendererTest {
  override def renderer = Swagger2dot0Json(ApiInfo("title", "1.2", Some("module description")))
}

