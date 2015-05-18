package io.github.daviddenton.fintrospect.parameters

import org.jboss.netty.handler.codec.http.HttpRequest


abstract class RequestParameter[T](location: Location, parse: (String => Option[T])) extends Parameter[T] {
  override val where = location.toString

  def unapply(request: HttpRequest): Option[T] = location.from(name, request).flatMap(parse)
}
