package io.fintrospect.parameters

import org.jboss.netty.handler.codec.http.HttpRequest

import scala.util.Try

abstract class RequestParameter[T, X](val name: String, parse: (String => X), val required: Boolean) extends Parameter[T] {
  def from(request: HttpRequest): T

  def parseFrom(request: HttpRequest): Option[Try[X]] = location.from(name, request).map(s => Try(parse(s)))
}

class OptionalRequestParameter[T](name: String,
                                  val location: Location,
                                  val description: Option[String],
                                  val paramType: ParamType,
                                  parse: (String => T))
  extends RequestParameter[Option[T], T](name, parse, false) {

  override def from(request: HttpRequest): Option[T] = Try(location.from(name, request).map(parse).get).toOption
}

class MandatoryRequestParameter[T](name: String,
                                   val location: Location,
                                   val description: Option[String],
                                   val paramType: ParamType,
                                   parse: (String => T)) extends RequestParameter[T, T](name, parse, true) {
  override def from(request: HttpRequest): T = parseFrom(request).flatMap(_.toOption).get
}
