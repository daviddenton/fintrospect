package io.github.daviddenton.fintrospect.parameters

import com.twitter.finagle.http.Request

class QueryTest extends ParametersTest[Request, OptionalRequestParameter](Query.optional) {
  override def embed(param: String): Request = Request(paramName -> param)
}