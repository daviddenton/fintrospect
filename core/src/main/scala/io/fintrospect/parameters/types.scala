package io.fintrospect.parameters

import io.fintrospect.util.ExtractableParameter

object types {

  trait OptionalParameter[From, T, Bnd <: Binding] extends Optional[From, T]
    with ExtractableParameter[From, T]
    with Rebindable[From, T, Bnd] {
    override def <->(from: From): Iterable[Bnd] = (this <-- from).map(this.-->).getOrElse(Nil)

    def -->(value: Option[T]): Iterable[Bnd] = value.map(-->).getOrElse(Nil)
  }

  trait MandatoryParameter[From, T, Bnd <: Binding] extends Mandatory[From, T]
    with ExtractableParameter[From, T]
    with Rebindable[From, T, Bnd] {
    override def <->(from: From): Iterable[Bnd] = this --> (this <-- from)
  }

}
