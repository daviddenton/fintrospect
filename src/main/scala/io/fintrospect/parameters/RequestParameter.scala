package io.fintrospect.parameters

import org.jboss.netty.handler.codec.http.HttpRequest

import scala.util.{Success, Try}

abstract class RequestParameter[T, X](parse: (String => Try[T])) extends Parameter[T] {
  def from(request: HttpRequest): T

  def parseFrom(request: HttpRequest): Option[Try[X]]
}

class OptionalRequestParameter[T](val name: String,
                                  val location: Location,
                                  val description: Option[String],
                                  val paramType: ParamType,
                                  parse: (String => Try[T]))
  extends RequestParameter[Option[T], T](s => Success(parse(s).toOption)) {

  override val required = false

  override def parseFrom(request: HttpRequest): Option[Try[T]] = location.from(name, request).map(parse)

  override def from(request: HttpRequest): Option[T] = location.from(name, request).map(parse).flatMap(_.toOption)
}

class MandatoryRequestParameter[T](val name: String,
                                   val location: Location,
                                   val description: Option[String],
                                   val paramType: ParamType,
                                   parse: (String => Try[T])) extends RequestParameter[T, T](parse) {
  override val required = true

  override def from(request: HttpRequest): T = parseFrom(request).flatMap(_.toOption).get

  override def parseFrom(request: HttpRequest): Option[Try[T]] = location.from(name, request).map(parse)
}
