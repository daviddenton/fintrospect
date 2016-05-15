package io.fintrospect.parameters

abstract class FormField[T](spec: ParameterSpec[_], val deserialize: Seq[String] => T)
  extends BodyParameter
  with Deserialisable[T]
  with Bindable[T, FormFieldBinding] {

  override val name = spec.name
  override val description = spec.description
  override val paramType = spec.paramType
  override val example = None
  override val where = "form"

  protected def get[O](form: Form, fn: T => O): Extraction[O] = Extractor.extract(this, form.get(name)).map(fn)
}

abstract class SingleFormField[T](spec: ParameterSpec[T])
  extends FormField[T](spec, xs => spec.deserialize(xs.head)) {

  def -->(value: T) = Seq(new FormFieldBinding(this, spec.serialize(value)))
}

abstract class MultiFormField[T](spec: ParameterSpec[T])
  extends FormField[Seq[T]](spec, xs => xs.map(spec.deserialize)) {

  def -->(value: Seq[T]) = value.map(v => new FormFieldBinding(this, spec.serialize(v)))
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
    override def apply[T](spec: ParameterSpec[T]) = new SingleFormField[T](spec) with Mandatory[T] {
      override def <--?(form: Form) = get(form, identity)
    }

    override val multi = new Parameters[MultiFormField, MandatorySeq] {
      override def apply[T](spec: ParameterSpec[T]) = new MultiFormField[T](spec) with MandatorySeq[T] {
        override def <--?(form: Form) = get(form, identity)
      }
    }
  }

  val optional = new Parameters[FormField, Optional] with MultiParameters[MultiFormField, OptionalSeq] {
    override def apply[T](spec: ParameterSpec[T]) = new SingleFormField[T](spec) with Optional[T] {
      override def <--?(form: Form) = get(form, Some(_))
    }

    override val multi = new Parameters[MultiFormField, OptionalSeq] {
      override def apply[T](spec: ParameterSpec[T]) = new MultiFormField[T](spec) with OptionalSeq[T] {
        override def <--?(form: Form) = get(form, Some(_))
      }
    }
  }
}
