package io.github.daviddenton.fintrospect.parameters

import com.twitter.finagle.http.Request

import scala.util.Try

class RequiredHeaderTest extends JsonSupportingParametersTest[RequiredRequestParameter](Header.required) {
  override def from[X](method: (String, String) => RequiredRequestParameter[X], value: Option[String]): Option[X] = {
    val request = Request()
    value.foreach(request.headers().add(paramName, _))
    Try(method(paramName, null).from(request)).toOption
  }
}
