package io.github.daviddenton.fintrospect.renderers

import io.github.daviddenton.fintrospect.ApiInfo

class SwaggerV2dot0JsonTest extends JsonRendererTest("Swagger2dot0Json", Swagger2dot0Json(ApiInfo("title", "1.2", Some("module description"))))
