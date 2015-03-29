package io.github.daviddenton.fintrospect.parameters

import com.twitter.finagle.http.Request

class OptionalBodyTest extends ParametersTest[OptionalRequestParameter](Body.optional) {
  override def from[X](param: OptionalRequestParameter[X], value: String): Option[X] = {
    val request = Request()
    request.setContentString(value)
    param.from(request)
  }
}
