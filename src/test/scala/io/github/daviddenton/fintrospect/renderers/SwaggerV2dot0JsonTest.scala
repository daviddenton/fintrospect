package io.github.daviddenton.fintrospect.renderers

import argo.jdom.JsonRootNode
import io.github.daviddenton.fintrospect.{ApiInfo, DescriptionRenderer}

class SwaggerV2dot0JsonTest extends JsonDescriptionRendererTest {
  override def name: String = "Swagger2dot0Json"
  override def renderer: DescriptionRenderer[JsonRootNode] = Swagger2dot0Json(ApiInfo("title", "1.2", Some("module description")))
}

