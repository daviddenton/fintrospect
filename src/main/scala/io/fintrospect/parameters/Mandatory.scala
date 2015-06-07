package io.fintrospect.parameters

import org.jboss.netty.handler.codec.http.HttpRequest

trait Mandatory[T] extends Require[T] {
  self: Parameter[T] =>
  override val requirement = Requirement.Mandatory
  def from(request: HttpRequest): T = parseFrom(request).flatMap(_.toOption).get
}
