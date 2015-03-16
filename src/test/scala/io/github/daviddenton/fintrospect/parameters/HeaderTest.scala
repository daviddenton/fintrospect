package io.github.daviddenton.fintrospect.parameters

import com.twitter.finagle.http.Request

class HeaderTest extends ParametersTest[Request, RequestParameter](Header) {
  override def embed(param: String): Request = {
    val request = Request("somevalue")
    request.headers().add(paramName, param)
    request
  }
}