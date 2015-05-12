package io.github.daviddenton.fintrospect.parameters

import com.twitter.finagle.http.Request

class RequiredHeaderTest extends JsonSupportingParametersTest[RequiredRequestParameter](Header.required) {
  override def from[X](param: RequiredRequestParameter[X], value: String): Option[X] = {
    val request = Request()
    request.headers().add(paramName, value)
    scala.util.Try(param.from(request)).toOption
  }
}
