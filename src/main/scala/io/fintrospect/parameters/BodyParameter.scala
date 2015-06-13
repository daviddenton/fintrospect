package io.fintrospect.parameters

import argo.jdom.JsonRootNode
import org.jboss.netty.handler.codec.http.HttpRequest

import scala.util.Try

abstract class BodyParameter[T](spec: ParameterSpec[T], location: Location, val example: Option[JsonRootNode]) extends ParseableParameter[T] {

  override val name = spec.name
  override val description = spec.description
  override val paramType = spec.paramType

  def into(request: HttpRequest, value: String): Unit = location.into(name, value, request)

  override def ->(value: T): ParamBinding[T] = ParamBinding(this, spec.serialize(value))

  val where = location.toString

  def attemptToParseFrom(request: HttpRequest): Option[Try[T]] = location.from(name, request).map(s => Try(spec.deserialize(s)))
}
