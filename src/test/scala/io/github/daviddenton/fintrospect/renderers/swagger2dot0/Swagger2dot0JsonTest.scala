package io.github.daviddenton.fintrospect.renderers.swagger2dot0

import io.github.daviddenton.fintrospect.ApiInfo
import io.github.daviddenton.fintrospect.renderers.JsonModuleRendererTest

class Swagger2dot0JsonTest extends JsonModuleRendererTest {
  override def renderer = Swagger2dot0Json(ApiInfo("title", "1.2", Some("module description")))
}

