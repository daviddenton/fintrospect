package io.fintrospect.parameters

import io.fintrospect.util.ExtractableParameter

object types {

  trait OptionalParameter[From, T, Bnd <: Binding] extends Optional[From, T]
  with ExtractableParameter[From, T]
  with OptionalRebind[From, T, Bnd] {
    def -->(value: Option[T]): Iterable[Bnd] = value.map(-->).getOrElse(Nil)
  }

  trait MandatoryParameter[From, T, Bnd <: Binding] extends Mandatory[From, T]
  with ExtractableParameter[From, T]
  with MandatoryRebind[From, T, Bnd]
}
