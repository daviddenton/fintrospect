package io.fintrospect.parameters

import org.jboss.netty.handler.codec.http.HttpRequest

import scala.util.{Failure, Success, Try}

abstract class QueryParameter[T](spec: ParameterSpec[T], location: Location) extends Parameter[T] with Validatable[T, HttpRequest] {

  override val name = spec.name
  override val description = spec.description
  override val paramType = spec.paramType

  def into(request: HttpRequest, value: String): Unit = location.into(name, value, request)

  override def ->(value: T): ParamBinding[T] = ParamBinding(this, spec.serialize(value))

  val where = location.toString

  def validate(request: HttpRequest) = {
    location.from(name, request).map {
      v => Try(spec.deserialize(v)) match {
        case Success(d) => Right(Option(d))
        case Failure(_) => Left(this)
      }
    }.getOrElse(if (required) Left(this) else Right(None))
  }
}
