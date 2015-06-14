package io.fintrospect.parameters

import com.twitter.finagle.http.Request

class RequiredQueryTest extends JsonSupportingParametersTest[RequestParameter, Mandatory](Query.required) {

  override def to[X](method: (String, String) => RequestParameter[X] with Mandatory[X], value: X): ParamBinding[X] = method(paramName, null) -> value

  override def attemptFrom[X](method: (String, String) => RequestParameter[X] with Mandatory[X], value: Option[String])= {
    val request = value.map(s => Request(paramName -> s)).getOrElse(Request())
    method(paramName, null).attemptToParseFrom(request)
  }
}
