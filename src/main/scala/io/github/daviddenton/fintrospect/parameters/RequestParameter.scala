package io.github.daviddenton.fintrospect.parameters

import org.jboss.netty.handler.codec.http.HttpRequest


abstract class RequestParameter[T](name: String,
                                   description: Option[String],
                                   location: Location,
                                   paramType: ParamType,
                                   parse: (String => Option[T]),
                                   requirement: Requirement)
  extends Parameter[T](name, description, location.toString, paramType, requirement) {
  def unapply(request: HttpRequest): Option[T] = location.from(name, request).flatMap(parse)
}
