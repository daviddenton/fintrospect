package io.github.daviddenton.fintrospect.parameters

import com.twitter.finagle.http.Request

class RequiredHeaderTest extends ParametersTest[Request, RequiredRequestParameter](Header.required) {
  override def embed(param: String): Request = {
    val request = Request("somevalue")
    request.headers().add(paramName, param)
    request
  }
}
