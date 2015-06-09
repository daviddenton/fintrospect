package io.fintrospect.parameters

import com.twitter.finagle.http.Request

class OptionalQueryTest extends JsonSupportingParametersTest[OptionalRequestParameter, Optional](Query.optional) {

  override def to[X](method: (String, String) => OptionalRequestParameter[X] with Optional[X], value: X): ParamBinding[X] = {
    method(paramName, null) -> value
  }

  override def from[X](method: (String, String) => OptionalRequestParameter[X] with Optional[X], value: Option[String]): Option[X] = {
    val request = value.map(s => Request(paramName -> s)).getOrElse(Request())
    method(paramName, null).from(request)
  }
}
