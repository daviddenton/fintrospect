package io.github.daviddenton.fintrospect.renderers

import io.github.daviddenton.fintrospect.{ApiInfo, Renderer}

class SwaggerV2dot0JsonTest extends JsonRendererTest {
  var id = 0

  def idGen(): String = {
    val nextId = "definition" + id
    id = id + 1
    nextId
  }
  override def name: String = "Swagger2dot0Json"
  override def renderer: Renderer = Swagger2dot0Json(ApiInfo("title", "1.2", Some("module description")), idGen)
}

