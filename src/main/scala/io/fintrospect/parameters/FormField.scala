package io.fintrospect.parameters

import org.jboss.netty.handler.codec.http.HttpRequest

import scala.util.{Failure, Success, Try}

abstract class FormField[T](spec: ParameterSpec[T]) extends Validatable[T, Form] with BodyParameter[T] {

  override val example = None
  override val name = spec.name
  override val description = spec.description
  override val paramType = spec.paramType

  def into(request: HttpRequest, value: String): Unit = ???

  override def ->(value: T): ParamBinding[T] = ParamBinding(this, spec.serialize(value))

  val where = "form"

  def validate(form: Form): Either[Parameter[_], Option[T]] = {
    form.get(name).map {
      v => Try(spec.deserialize(v)) match {
        case Success(v) => Right(Some(v))
        case Failure(_) => Left(this)
      }
    }.getOrElse(if (required) Left(this) else Right(None))
  }
}

object FormField {

  trait Mandatory[T] extends io.fintrospect.parameters.Mandatory[T, Form]

  trait Optional[T] extends io.fintrospect.parameters.Optional[T, Form]

  val required = new Parameters[FormField, Mandatory] {
    override def apply[T](spec: ParameterSpec[T]) = new FormField[T](spec) with Mandatory[T]
  }

  val optional = new Parameters[FormField, Optional] {
    override def apply[T](spec: ParameterSpec[T]) = new FormField[T](spec) with Optional[T]
  }
}
