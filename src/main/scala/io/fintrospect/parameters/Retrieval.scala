package io.fintrospect.parameters

import org.jboss.netty.handler.codec.http.HttpRequest

import scala.language.reflectiveCalls
import scala.util.Try

sealed trait Retrieval[T] {
  val required: Boolean
  def from(request: HttpRequest): T
}

trait Mandatory[T] extends Retrieval[T] {
  override val required = true
  def attemptToParseFrom(request: HttpRequest): Option[Try[T]]
  def from(request: HttpRequest): T = attemptToParseFrom(request).flatMap(_.toOption).get
}

trait Optional[T] extends Retrieval[Option[T]] {
  override val required = false
  def attemptToParseFrom(request: HttpRequest): Option[Try[T]]
  def from(request: HttpRequest): Option[T] = Try(attemptToParseFrom(request).get.toOption).toOption.flatMap(identity)
}
