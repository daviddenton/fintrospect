package io.github.daviddenton.fintrospect.parameters

import com.twitter.finagle.http.Request

class OptionalHeaderTest extends ParametersTest[Request, OptionalRequestParameter](Header.optional) {
  override def embed(param: String): Request = {
    val request = Request("somevalue")
    request.headers().add(paramName, param)
    request
  }
}
