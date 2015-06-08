package io.fintrospect.parameters

import org.jboss.netty.handler.codec.http.HttpRequest

import scala.util.Try

trait Parameter[T] {
  val name: String
  val description: Option[String]
  val location: Location
  val paramType: ParamType
  val required: Boolean
  def parseFrom(request: HttpRequest): Option[Try[T]]
}

