package io.github.daviddenton.fintrospect.parameters

import com.twitter.finagle.http.Request

class OptionalHeaderTest extends JsonSupportingParametersTest[OptionalRequestParameter](Header.optional) {
  override def from[X](param: OptionalRequestParameter[X], value: String): Option[X] = {
    val request = Request()
    request.headers().add(paramName, value)
    param.from(request)
  }
}
