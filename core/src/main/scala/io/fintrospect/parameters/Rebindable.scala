package io.fintrospect.parameters

/**
  * Used to transparently copy the value out of an incoming request (or form etc..) and into an outgoing one. Useful when chaining
  * requests together.
  */
trait Rebindable[From, T, B <: Binding] extends Bindable[T, B] {
  def <->(from: From): Iterable[B]

  /**
    * User-friendly synonym for <->(), which is why the method is final.
    */
  final def rebind(from: From): Iterable[B] = <->(from)
}

trait MandatoryRebind[From, T, B <: Binding] extends Rebindable[From, T, B] {
  self: Retrieval[From, T] =>
  override def <->(from: From): Iterable[B] = this --> (this <-- from)
}

trait OptionalRebind[From, T, B <: Binding] extends Rebindable[From, T, B] {
  self: Retrieval[From, Option[T]] =>
  override def <->(from: From): Iterable[B] = (this <-- from).map(this.-->).getOrElse(Nil)
}