package io.fintrospect.parameters

import org.jboss.netty.handler.codec.http.HttpRequest

import scala.util.Try

abstract class RequestParameter[T](val name: String, location: Location, deserialize: String => T, serialize: T => String)
  extends Parameter[T] {
  def into(request: HttpRequest, value: String): Unit = location.into(name, value, request)

  override def ->(value: T): ParamBinding[T] = ParamBinding(this, serialize(value))

  val where = location.toString

  def attemptToParseFrom(request: HttpRequest): Option[Try[T]] = location.from(name, request).map(s => Try(deserialize(s)))
}

class OptionalRequestParameter[T](name: String,
                                  location: Location,
                                  val description: Option[String],
                                  val paramType: ParamType,
                                  deserialize: String => T, serialize: T => String)
  extends RequestParameter[T](name, location, deserialize, serialize) with Optional[T] {

  def from(request: HttpRequest): Option[T] = Try(attemptToParseFrom(request).get.toOption).toOption.flatMap(identity)
}

class MandatoryRequestParameter[T](name: String,
                                   location: Location,
                                   val description: Option[String],
                                   val paramType: ParamType,
                                   deserialize: String => T, serialize: T => String)
  extends RequestParameter[T](name, location, deserialize, serialize) with Mandatory[T] {
  def from(request: HttpRequest): T = attemptToParseFrom(request).flatMap(_.toOption).get
}
