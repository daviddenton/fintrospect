package io.fintrospect.parameters

import org.jboss.netty.handler.codec.http.HttpRequest

import scala.util.Try

abstract class RequestParameter[T](val name: String,
                                   val description: Option[String],
                                   val paramType: ParamType,
                                   location: Location,
                                   deserialize: String => T,
                                   serialize: T => String)
  extends Parameter[T] {
  def into(request: HttpRequest, value: String): Unit = location.into(name, value, request)

  override def ->(value: T): ParamBinding[T] = ParamBinding(this, serialize(value))

  val where = location.toString

  def attemptToParseFrom(request: HttpRequest): Option[Try[T]] = location.from(name, request).map(s => Try(deserialize(s)))
}
