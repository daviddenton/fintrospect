package io.fintrospect.parameters

import org.jboss.netty.handler.codec.http.HttpRequest

import scala.util.Try

abstract class RequestParameter[T, X](val name: String, location: Location, parse: (String => X), val required: Boolean) extends Parameter[T] {
  val where = location.toString

  def from(request: HttpRequest): T

  def attemptToParseFrom(request: HttpRequest): Option[Try[X]] = location.from(name, request).map(s => Try(parse(s)))
}

class OptionalRequestParameter[T](name: String,
                                  location: Location,
                                  val description: Option[String],
                                  val paramType: ParamType,
                                  parse: (String => T))
  extends RequestParameter[Option[T], T](name, location, parse, false) {

  override def from(request: HttpRequest): Option[T] = Try(location.from(name, request).map(parse).get).toOption
}

class MandatoryRequestParameter[T](name: String,
                                   location: Location,
                                   val description: Option[String],
                                   val paramType: ParamType,
                                   parse: (String => T)) extends RequestParameter[T, T](name, location, parse, true) {
  override def from(request: HttpRequest): T = attemptToParseFrom(request).flatMap(_.toOption).get
}
