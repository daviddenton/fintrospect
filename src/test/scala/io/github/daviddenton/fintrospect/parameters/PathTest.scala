package io.github.daviddenton.fintrospect.parameters

import com.twitter.finagle.http.Request

class PathTest extends ParametersTest[PathParameter](Path) {
  override def from[X](param: PathParameter[X], value: String): Option[X] = {
    param.unapply(value)
  }
}