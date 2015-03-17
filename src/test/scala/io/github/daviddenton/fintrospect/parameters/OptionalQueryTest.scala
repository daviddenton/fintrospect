package io.github.daviddenton.fintrospect.parameters

import com.twitter.finagle.http.Request

class OptionalQueryTest extends ParametersTest[OptionalRequestParameter](Query.optional) {
  override def from[X](param: OptionalRequestParameter[X], value: String): Option[X] = {
    param.from(Request(paramName -> value))
  }
}