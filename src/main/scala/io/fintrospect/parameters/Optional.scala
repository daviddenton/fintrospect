package io.fintrospect.parameters

import org.jboss.netty.handler.codec.http.HttpRequest


trait Optional[T] extends Requirement[T] {
  self: RequestParameter[T] =>
  override val required = false
  def from(request: HttpRequest): Option[T] = parseFrom(request).flatMap(_.toOption)
}
