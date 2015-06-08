package io.fintrospect.parameters

import org.jboss.netty.handler.codec.http.HttpRequest

trait Mandatory[T] extends Requirement[T] {
  self: Parameter[T] =>
  override val required = true
  override def from(request: HttpRequest): T = parseFrom(request).flatMap(_.toOption).get
}
