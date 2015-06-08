package io.fintrospect.parameters

import org.jboss.netty.handler.codec.http.HttpRequest

sealed trait Retrieval[T] {
  def from(request: HttpRequest): T
}

trait Mandatory[T] extends Retrieval[T]

trait Optional[T] extends Retrieval[Option[T]]
