package io.fintrospect.parameters

import org.jboss.netty.handler.codec.http.HttpRequest

/**
 * Simple wrapper for retrieving the form fields from a request. Alternatively, just get them directly!
 */
class Form(request: HttpRequest) {
  def apply[T](formField: FormField[_] with Retrieval[T]): T = formField.from(request)
}
