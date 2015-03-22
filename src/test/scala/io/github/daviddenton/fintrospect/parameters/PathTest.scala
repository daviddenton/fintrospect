package io.github.daviddenton.fintrospect.parameters

class PathTest extends ParametersTest[PathParameter](Path) {
  override def from[X](param: PathParameter[X], value: String): Option[X] = {
    param.unapply(value)
  }
}