package io.fintrospect.parameters

import com.twitter.finagle.http.Request

import scala.util.Try

class RequiredHeaderTest extends JsonSupportingParametersTest[NonBodyRequestParameter, Mandatory](Header.required) {
  override def from[X](method: (String, String) => NonBodyRequestParameter[X] with Mandatory[X], value: Option[String]): Option[X] = {
    val request = Request()
    value.foreach(request.headers().add(paramName, _))
    Try(method(paramName, null).from(request)).toOption
  }
}
