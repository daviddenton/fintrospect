package io.fintrospect.parameters

object types {

  type ExtParameter[F, T, B <: Binding] = Parameter with Extractable[F, T] with Bindable[T, B]

  trait OptionalParameter[F, T, B <: Binding] extends io.fintrospect.parameters.Optional[F, T]
  with ExtractableParameter[F, T]
  with OptionalRebind[F, T, B]
  with OptionalBindable[T, B] {
    self: ExtParameter[F, T, B] =>
  }

  trait MandatoryParameter[F, T, B <: Binding] extends io.fintrospect.parameters.Mandatory[F, T]
  with ExtractableParameter[F, T]
  with MandatoryRebind[F, T, B] {
    self: ExtParameter[F, T, B] =>
  }
}
