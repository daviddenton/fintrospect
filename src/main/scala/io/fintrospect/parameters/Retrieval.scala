package io.fintrospect.parameters

import org.jboss.netty.handler.codec.http.HttpRequest

sealed trait Retrieval[T] {
  val required: Boolean
  def from(request: HttpRequest): T
}

trait Mandatory[T] extends Retrieval[T] {
  override val required = true
}

trait Optional[T] extends Retrieval[Option[T]] {
  override val required = false
}
