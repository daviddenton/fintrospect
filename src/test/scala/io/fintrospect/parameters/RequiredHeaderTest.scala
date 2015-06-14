package io.fintrospect.parameters

import com.twitter.finagle.http.Request

class RequiredHeaderTest extends JsonSupportingParametersTest[RequestParameter, Mandatory](Header.required) {

  override def to[X](method: (String, String) => RequestParameter[X] with Mandatory[X], value: X): ParamBinding[X] = method(paramName, null) -> value

  override def attemptFrom[X](method: (String, String) => RequestParameter[X] with Mandatory[X], value: Option[String])= {
    val request = Request()
    value.foreach(request.headers().add(paramName, _))
    method(paramName, null).attemptToParseFrom(request)
  }
}
