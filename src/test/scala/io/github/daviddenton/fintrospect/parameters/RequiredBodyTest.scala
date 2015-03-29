package io.github.daviddenton.fintrospect.parameters

import com.twitter.finagle.http.Request

class RequiredBodyTest extends ParametersTest[RequiredRequestParameter](Body.required) {
  override def from[X](param: RequiredRequestParameter[X], value: String): Option[X] = {
    val request = Request()
    request.setContentString(value)
    scala.util.Try(param.from(request)).toOption
  }
}
