package io.fintrospect.parameters

import org.jboss.netty.handler.codec.http.HttpRequest

import scala.util.{Failure, Success, Try}

abstract class RequestParameter[T](spec: ParameterSpec[T], location: Location) extends Validatable[T, HttpRequest] {

  override val name = spec.name
  override val description = spec.description
  override val paramType = spec.paramType

  def into(request: HttpRequest, value: String): Unit = location.into(name, value, request)

  override def ->(value: T): ParamBinding[T] = ParamBinding(this, spec.serialize(value))

  val where = location.toString

  def validate(request: HttpRequest) = {
    val from = location.from(name, request)
    if (from.isEmpty) {
      if (required) Left(this) else Right(None)
    } else {
      Try(spec.deserialize(from.get)) match {
        case Success(v) => Right(Some(v))
        case Failure(_) => Left(this)
      }
    }
  }
}
