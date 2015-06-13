package io.fintrospect.parameters

import org.jboss.netty.handler.codec.http.HttpRequest

import scala.language.reflectiveCalls
import scala.util.Try


trait Retrieval[T] {
  def from(request: HttpRequest): T
}

trait Mandatory[T] extends Retrieval[T] with ParseableParameter[T] {
  override val required = true
  def from(request: HttpRequest): T = attemptToParseFrom(request).flatMap(_.toOption).get
}

trait Optional[T] extends Retrieval[Option[T]] with ParseableParameter[T]  {
  override val required = false
  def from(request: HttpRequest): Option[T] = Try(attemptToParseFrom(request).get.toOption).toOption.flatMap(identity)
}
