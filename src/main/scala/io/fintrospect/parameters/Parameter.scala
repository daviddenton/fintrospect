package io.fintrospect.parameters

import org.jboss.netty.handler.codec.http.HttpRequest

import scala.util.Try

trait Parameter[T] {
  val name: String
  val description: Option[String]
  val where: String
  val paramType: ParamType
  val requirement: Requirement
  def parseFrom(request: HttpRequest): Option[Try[T]]
}

