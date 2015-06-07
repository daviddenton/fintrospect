package io.fintrospect.parameters

import org.jboss.netty.handler.codec.http.HttpRequest


trait Optional[T] extends Require[T] {
  self: RequestParameter[T] =>
  override val requirement = Requirement.Optional
  def from(request: HttpRequest): Option[T] = parseFrom(request).flatMap(_.toOption)
}
