package io.github.daviddenton.fintrospect.parameters

class PathTest extends ParametersTest[String, PathParameter](Path) {
  override def embed(param: String): String = param
}