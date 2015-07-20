package io.fintrospect.parameters

import scala.util.{Failure, Success, Try}

abstract class FormField[T](val name: String, val description: Option[String], val paramType: ParamType)
  extends BodyParameter
  with Validatable[T, Form]
  with Bindable[T, FormFieldBinding] {

  val where = "form"

  override val example = None
}

abstract class SingleFormField[T](spec: ParameterSpec[T])
  extends FormField[T](spec.name, spec.description, spec.paramType) {

  def -->(value: T) = Seq(new FormFieldBinding(this, spec.serialize(value)))

  def validate(form: Form): Either[Parameter, Option[T]] = {
    form.get(name).map {
      v => Try(spec.deserialize(v.head)) match {
        case Success(d) => Right(Option(d))
        case Failure(_) => Left(this)
      }
    }.getOrElse(if (required) Left(this) else Right(None))
  }
}

abstract class MultiFormField[T](spec: ParameterSpec[T])
  extends FormField[Seq[T]](spec.name, spec.description, spec.paramType) {

  def -->(value: Seq[T]) = value.map(v => new FormFieldBinding(this, spec.serialize(v)))

  def validate(form: Form): Either[Parameter, Option[Seq[T]]] = {
    form.get(name).map {
      v => Try(v.map(spec.deserialize)) match {
        case Success(d) => Right(Option(d))
        case Failure(_) => Left(this)
      }
    }.getOrElse(if (required) Left(this) else Right(None))
  }
}

object FormField {

  trait Mandatory[T] extends io.fintrospect.parameters.Mandatory[T, Form] with MandatoryRebind[T, Form, FormFieldBinding] {
    self: Bindable[T, FormFieldBinding] =>
  }

  trait MandatorySeq[T] extends io.fintrospect.parameters.Mandatory[Seq[T], Form] with MandatoryRebind[Seq[T], Form, FormFieldBinding] {
    self: Bindable[Seq[T], FormFieldBinding] =>
  }

  trait Optional[T] extends io.fintrospect.parameters.Optional[T, Form] with OptionalRebind[T, Form, FormFieldBinding] {
    self: Bindable[T, FormFieldBinding] =>
  }

  trait OptionalSeq[T] extends io.fintrospect.parameters.Optional[Seq[T], Form] with OptionalRebind[Seq[T], Form, FormFieldBinding] {
    self: Bindable[Seq[T], FormFieldBinding] =>
  }

  val required = new Parameters[FormField, Mandatory] with MultiParameters[MultiFormField, MandatorySeq] {
    override def apply[T](spec: ParameterSpec[T]) = new SingleFormField[T](spec) with Mandatory[T]

    override val multi = new Parameters[MultiFormField, MandatorySeq] {
      override def apply[T](spec: ParameterSpec[T]) = new MultiFormField[T](spec) with MandatorySeq[T]
    }
  }

  val optional = new Parameters[FormField, Optional] with MultiParameters[MultiFormField, OptionalSeq] {
    override def apply[T](spec: ParameterSpec[T]) = new SingleFormField[T](spec) with Optional[T]

    override val multi = new Parameters[MultiFormField, OptionalSeq] {
      override def apply[T](spec: ParameterSpec[T]) = new MultiFormField[T](spec) with OptionalSeq[T]
    }
  }
}
