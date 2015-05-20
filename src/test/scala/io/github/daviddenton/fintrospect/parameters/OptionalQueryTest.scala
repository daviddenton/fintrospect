package io.github.daviddenton.fintrospect.parameters

import com.twitter.finagle.http.Request

class OptionalQueryTest extends JsonSupportingParametersTest[OptionalRequestParameter](Query.optional) {
  override def from[X](method: (String, String) => OptionalRequestParameter[X], value: Option[String]): Option[X] = {
    val request = value.map(s => Request(paramName -> s)).getOrElse(Request())
    method(paramName, null).from(request)
  }
}
