package io.fintrospect.parameters

import com.twitter.finagle.http.Request

class OptionalQueryTest extends JsonSupportingParametersTest[NonBodyRequestParameter, Optional](Query.optional) {
  override def from[X](method: (String, String) => NonBodyRequestParameter[X] with Optional[X], value: Option[String]): Option[X] = {
    val request = value.map(s => Request(paramName -> s)).getOrElse(Request())
    method(paramName, null).from(request)
  }
}
