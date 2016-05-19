package io.fintrospect.parameters

trait FormField[T]
  extends BodyParameter
  with Bindable[T, FormFieldBinding] {
  override val example = None
  override val where = "form"
}

private object FormFieldExtractAndRebind extends ParameterExtractAndBind[Form, FormFieldBinding] {
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

  trait Mandatory[T] extends io.fintrospect.parameters.Mandatory[Form, T]
  with ExtractableParameter[Form, T]
  with MandatoryRebind[Form, T, FormFieldBinding] {
    self: Parameter with Extractable[Form, T] with Bindable[T, FormFieldBinding] =>
  }

  trait MandatorySeq[T] extends io.fintrospect.parameters.Mandatory[Form, Seq[T]]
  with ExtractableParameter[Form, Seq[T]]
  with MandatoryRebind[Form, Seq[T], FormFieldBinding] {
    self: Parameter with Extractable[Form, Seq[T]] with Bindable[Seq[T], FormFieldBinding] =>
  }

  trait Optional[T] extends io.fintrospect.parameters.Optional[Form, T]
  with ExtractableParameter[Form, T]
  with OptionalRebind[Form, T, FormFieldBinding]
  with OptionalBindable[T, FormFieldBinding] {
    self: Parameter with Extractable[Form, T] with Bindable[T, FormFieldBinding] =>
  }

  trait OptionalSeq[T] extends io.fintrospect.parameters.Optional[Form, Seq[T]]
  with ExtractableParameter[Form, Seq[T]]
  with OptionalRebind[Form, Seq[T], FormFieldBinding]
  with OptionalBindable[Seq[T], FormFieldBinding] {
    self: Parameter with Extractable[Form, Seq[T]] with Bindable[Seq[T], FormFieldBinding] =>
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
