package io.fintrospect.parameters

import com.twitter.finagle.http.Request

import scala.util.Try

class RequiredHeaderTest extends JsonSupportingParametersTest[RequestParameter, Mandatory](Header.required) {

  override def to[X](method: (String, String) => RequestParameter[X] with Mandatory[X], value: X): ParamBinding[X] = method(paramName, null) -> value

  override def from[X](method: (String, String) => RequestParameter[X] with Mandatory[X], value: Option[String])= {
    val request = Request()
    value.foreach(request.headers().add(paramName, _))
    Try(method(paramName, null).from(request)).toOption
  }
}
