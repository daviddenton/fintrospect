package io.github.daviddenton.fintrospect.renderers

import io.github.daviddenton.fintrospect.{ApiInfo, Renderer}

class SwaggerV2dot0JsonTest extends JsonRendererTest {
  override def name: String = "Swagger2dot0Json"
  override def renderer: Renderer = Swagger2dot0Json(ApiInfo("title", "1.2", Some("module description")))
}

