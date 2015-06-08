package io.fintrospect.parameters

import org.jboss.netty.handler.codec.http.HttpRequest

import scala.util.Try

abstract class RequestParameter[T](val name: String, location: Location, parse: String => T, val required: Boolean)
  extends Parameter[T] {
  def into(request: HttpRequest, value: String): Unit = location.into(name, value, request)

  val where = location.toString

  def attemptToParseFrom(request: HttpRequest): Option[Try[T]] = location.from(name, request).map(s => Try(parse(s)))
}

class OptionalRequestParameter[T](name: String,
                                  location: Location,
                                  val description: Option[String],
                                  val paramType: ParamType,
                                  parse: String => T)
  extends RequestParameter[T](name, location, parse, false) with Optional[T] {

  def from(request: HttpRequest): Option[T] = Try(location.from(name, request).map(parse).get).toOption
}

class MandatoryRequestParameter[T](name: String,
                                   location: Location,
                                   val description: Option[String],
                                   val paramType: ParamType,
                                   parse: String => T)
  extends RequestParameter[T](name, location, parse, true) with Mandatory[T] {
  def from(request: HttpRequest): T = attemptToParseFrom(request).flatMap(_.toOption).get
}
