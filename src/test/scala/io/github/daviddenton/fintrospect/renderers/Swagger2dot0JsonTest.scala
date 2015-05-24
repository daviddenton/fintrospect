package io.github.daviddenton.fintrospect.renderers

import io.github.daviddenton.fintrospect.ApiInfo

class Swagger2dot0JsonTest extends JsonDescriptionRendererTest {
  override def renderer = Swagger2dot0Json(ApiInfo("title", "1.2", Some("module description")))
}

