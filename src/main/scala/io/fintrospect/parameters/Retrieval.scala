package io.fintrospect.parameters

import org.jboss.netty.handler.codec.http.HttpRequest

import scala.language.reflectiveCalls


trait Retrieval[T] {
  def from(request: HttpRequest): T
}

trait Mandatory[T] extends Retrieval[T] with ParseableParameter[T] {
  override val required = true
  def from(request: HttpRequest): T = attemptToParseFrom(request).get.get
}

trait Optional[T] extends Retrieval[Option[T]] with ParseableParameter[T]  {
  override val required = false
  def from(request: HttpRequest): Option[T] = attemptToParseFrom(request).map(_.get)
}
