package io.fintrospect.parameters

import io.fintrospect.parameters.types._

trait FormField[T]
  extends BodyParameter
  with Bindable[T, FormFieldBinding] {
  override val example = None
  override val where = "form"
}

private object FormFieldExtractAndRebind extends ParameterExtractAndBind[Form, FormFieldBinding] {
  def newBinding(parameter: Parameter, value: String) = new FormFieldBinding(parameter, value)

  def valuesFrom(parameter: Parameter, form: Form): Option[Seq[String]] = form.get(parameter.name).map(_.toSeq)
}

abstract class SingleFormField[T](spec: ParameterSpec[T])
  extends SingleParameter(spec, FormFieldExtractAndRebind) with FormField[T] {
}

abstract class MultiFormField[T](spec: ParameterSpec[T])
  extends MultiParameter(spec, FormFieldExtractAndRebind) with FormField[Seq[T]] {
}

object FormField {

  trait Mandatory[T] extends MandatoryParameter[Form, T, FormFieldBinding]

  trait MandatorySeq[T] extends MandatoryParameter[Form, Seq[T], FormFieldBinding]

  trait Optional[T] extends OptionalParameter[Form, T, FormFieldBinding]

  trait OptionalSeq[T] extends OptionalParameter[Form, Seq[T], FormFieldBinding]

  val required = new Parameters[FormField, Mandatory] with MultiParameters[MultiFormField, MandatorySeq] {
    override def apply[T](spec: ParameterSpec[T]) = new SingleFormField(spec) with Mandatory[T]

    override val multi = new Parameters[MultiFormField, MandatorySeq] {
      override def apply[T](spec: ParameterSpec[T]) = new MultiFormField(spec) with MandatorySeq[T]
    }
  }

  val optional = new Parameters[FormField, Optional] with MultiParameters[MultiFormField, OptionalSeq] {
    override def apply[T](spec: ParameterSpec[T]) = new SingleFormField(spec) with Optional[T]

    override val multi = new Parameters[MultiFormField, OptionalSeq] {
      override def apply[T](spec: ParameterSpec[T]) = new MultiFormField(spec) with OptionalSeq[T]
    }
  }
}
