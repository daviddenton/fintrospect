package io.fintrospect.parameters

import org.jboss.netty.handler.codec.http.HttpRequest

import scala.util.Try

trait ParseableParameter[T] extends Parameter[T] {
  def attemptToParseFrom(request: HttpRequest): Option[Try[T]]
}
