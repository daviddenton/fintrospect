package io.fintrospect.parameters

object types {

  type Param[F, T, B <: Binding] = Parameter with Extractable[F, T] with Bindable[T, B]

  trait Opt[F, T, B <: Binding] extends io.fintrospect.parameters.Optional[F, T]
  with ExtractableParameter[F, T]
  with OptionalRebind[F, T, B]
  with OptionalBindable[T, B] {
    self: Param[F, T, B] =>
  }

  trait Mand[F, T, B <: Binding] extends io.fintrospect.parameters.Mandatory[F, T]
  with ExtractableParameter[F, T]
  with MandatoryRebind[F, T, B] {
    self: Param[F, T, B] =>
  }
}
