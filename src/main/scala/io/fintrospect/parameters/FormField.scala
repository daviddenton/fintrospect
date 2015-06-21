package io.fintrospect.parameters

import org.jboss.netty.handler.codec.http.HttpRequest

import scala.util.{Failure, Success, Try}

abstract class FormField[T](spec: ParameterSpec[T]) extends ParseableParameter[T, NewForm] with BodyParameter[T] {

  override val example = None
  override val name = spec.name
  override val description = spec.description
  override val paramType = spec.paramType

  def into(request: HttpRequest, value: String): Unit = ???

  override def ->(value: T): ParamBinding[T] = ParamBinding(this, spec.serialize(value))

  val where = "form"

  def validate(form: NewForm):Either[Parameter[_], Option[T]] = {
    val from = form.get(name)
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

object FormField {
  def required[T](spec: ParameterSpec[T]) = new FormField[T](spec) with Mandatory[T, NewForm] {}

  def optional[T](spec: ParameterSpec[T]) = new FormField[T](spec) with Optional[T, NewForm] {}
}
