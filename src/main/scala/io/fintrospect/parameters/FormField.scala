package io.fintrospect.parameters

trait FormField[T]
  extends BodyParameter
  with Bindable[T, FormFieldBinding] {
  override val example = None
  override val where = "form"
}

object FormFieldExtractAndRebind extends ExtractAndRebind[Form, FormFieldBinding] {
  def newBinding(parameter: Parameter, value: String) = new FormFieldBinding(parameter, value)

  def valuesFrom(parameter: Parameter, form: Form): Option[Seq[String]] = form.get(parameter.name)
}

abstract class SingleFormField[T](spec: ParameterSpec[T])
  extends SingleParameter(spec, FormFieldExtractAndRebind) with FormField[T] {
}

abstract class MultiFormField[T](spec: ParameterSpec[T])
  extends MultiParameter(spec, FormFieldExtractAndRebind) with FormField[Seq[T]] {
}

object FormField {

  trait Mandatory[T] extends io.fintrospect.parameters.Mandatory[T, Form] with MandatoryRebind[T, Form, FormFieldBinding] {
    self: Bindable[T, FormFieldBinding] =>
  }

  trait MandatorySeq[T] extends io.fintrospect.parameters.Mandatory[Seq[T], Form] with MandatoryRebind[Seq[T], Form, FormFieldBinding] {
    self: Bindable[Seq[T], FormFieldBinding] =>
  }

  trait Optional[T] extends io.fintrospect.parameters.Optional[T, Form]
  with OptionalRebind[T, Form, FormFieldBinding]
  with OptionalBindable[T, FormFieldBinding] {
    self: Bindable[T, FormFieldBinding] =>
  }

  trait OptionalSeq[T] extends io.fintrospect.parameters.Optional[Seq[T], Form]
  with OptionalRebind[Seq[T], Form, FormFieldBinding]
  with OptionalBindable[Seq[T], FormFieldBinding] {
    self: Bindable[Seq[T], FormFieldBinding] =>
  }

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
