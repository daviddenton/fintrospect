package io.fintrospect.parameters

import org.jboss.netty.handler.codec.http.HttpRequest

import scala.util.Try


abstract class RequestParameter[T](override val requirement: Requirement, location: Location, parse: (String => Try[T])) extends Parameter[T] {
  override val where = location.toString

  def parseFrom(request: HttpRequest): Option[Try[T]] = location.from(name, request).map(parse)
}
