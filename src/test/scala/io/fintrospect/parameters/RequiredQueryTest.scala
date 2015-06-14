package io.fintrospect.parameters

import com.twitter.finagle.http.Request

import scala.util.Try

class RequiredQueryTest extends JsonSupportingParametersTest[RequestParameter, Mandatory](Query.required) {

  override def to[X](method: (String, String) => RequestParameter[X] with Mandatory[X], value: X): ParamBinding[X] = method(paramName, null) -> value

  override def from[X](method: (String, String) => RequestParameter[X] with Mandatory[X], value: Option[String])= {
    val request = value.map(s => Request(paramName -> s)).getOrElse(Request())
    Try(method(paramName, null).from(request)).toOption
  }
}
