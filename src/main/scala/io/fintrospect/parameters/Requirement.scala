package io.fintrospect.parameters

import org.jboss.netty.handler.codec.http.HttpRequest

trait Requirement[T] {
  val required: Boolean
  def from(request: HttpRequest): T
}
