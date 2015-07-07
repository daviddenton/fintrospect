package io.fintrospect.parameters

/**
 * Used to transparently copy the value out of an incoming request (or form etc..) and into an outgoing one. Useful when chaining
 * requests together.
 */
trait Rebindable[T, From, B <: Binding] {
  def <->(req: From): Iterable[B]

  def rebind(req: From): Iterable[B]
}

trait MandatoryRebind[T, From, B <: Binding] extends Rebindable[T, From, B] {
  self: Retrieval[T, From] with Bindable[T, B] =>
  override def <->(from: From): Iterable[B] = this --> (this <-- from)

  override def rebind(from: From): Iterable[B] = <->(from)
}

trait OptionalRebind[T, From, B <: Binding] extends Rebindable[T, From, B] {
  self: Retrieval[Option[T], From] with Bindable[T, B] =>
  override def <->(from: From): Iterable[B] = (this <-- from).map(this.-->).getOrElse(Seq())

  override def rebind(from: From): Iterable[B] = <->(from)
}