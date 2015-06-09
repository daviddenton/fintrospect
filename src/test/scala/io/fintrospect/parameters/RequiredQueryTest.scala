package io.fintrospect.parameters

import com.twitter.finagle.http.Request

import scala.util.Try

class RequiredQueryTest extends JsonSupportingParametersTest[MandatoryRequestParameter, Mandatory](Query.required) {

  override def to[X](method: (String, String) => MandatoryRequestParameter[X] with Mandatory[X], value: X): ParamBinding[X] = method(paramName, null) -> value

  override def from[X](method: (String, String) => MandatoryRequestParameter[X] with Mandatory[X], value: Option[String]): Option[X] = {
    val request = value.map(s => Request(paramName -> s)).getOrElse(Request())
    Try(method(paramName, null).from(request)).toOption
  }
}
