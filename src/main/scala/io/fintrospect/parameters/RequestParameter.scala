package io.fintrospect.parameters

import org.jboss.netty.handler.codec.http.HttpRequest

import scala.util.Try

abstract class RequestParameter[T](val name: String,
                                   val description: Option[String],
                                   val paramType: ParamType,
                                   val location: Location,
                                   parse: (String => Try[T])) extends Parameter[T] {
  override def parseFrom(request: HttpRequest): Option[Try[T]] = location.from(name, request).map(parse)
}

