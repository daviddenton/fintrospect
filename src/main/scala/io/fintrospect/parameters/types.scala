package io.fintrospect.parameters

object types {

  type ExtParameter[From, T, Bnd <: Binding] = Parameter with Extractable[From, T] with Bindable[T, Bnd]

  trait OptionalParameter[From, T, Bnd <: Binding] extends io.fintrospect.parameters.Optional[From, T]
  with ExtractableParameter[From, T]
  with OptionalRebind[From, T, Bnd]
  with OptionalBindable[T, Bnd] {
    self: ExtParameter[From, T, Bnd] =>
  }

  trait MandatoryParameter[From, T, Bnd <: Binding] extends io.fintrospect.parameters.Mandatory[From, T]
  with ExtractableParameter[From, T]
  with MandatoryRebind[From, T, Bnd] {
    self: ExtParameter[From, T, Bnd] =>
  }
}
