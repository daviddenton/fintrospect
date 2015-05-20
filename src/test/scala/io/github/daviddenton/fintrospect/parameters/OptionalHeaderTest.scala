package io.github.daviddenton.fintrospect.parameters

import com.twitter.finagle.http.Request

class OptionalHeaderTest extends JsonSupportingParametersTest[OptionalRequestParameter](Header.optional) {
  override def from[X](method: (String, String) => OptionalRequestParameter[X], value: Option[String]): Option[X] = {
    val request = Request()
    value.foreach(request.headers().add(paramName, _))
    method(paramName, null).from(request)
  }
}
