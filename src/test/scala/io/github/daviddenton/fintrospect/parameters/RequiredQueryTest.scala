package io.github.daviddenton.fintrospect.parameters

import com.twitter.finagle.http.Request

import scala.util.Try

class RequiredQueryTest extends JsonSupportingParametersTest[RequiredRequestParameter](Query.required) {
  override def from[X](method: (String, String) => RequiredRequestParameter[X], value: Option[String]): Option[X] = {
    val request = value.map(s => Request(paramName -> s)).getOrElse(Request())
    Try(method(paramName, null).from(request)).toOption
  }
}
