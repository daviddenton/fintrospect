package io.fintrospect.parameters

/**
  * Used to transparently copy the value out of an incoming request (or form etc..) and into an outgoing one. Useful when chaining
  * requests together.
  */
trait Rebindable[-From, T, +B <: Binding] extends Bindable[T, B] {
  def <->(from: From): Iterable[B]

  /**
    * User-friendly synonym for <->(), which is why the method is final.
    */
  final def rebind(from: From): Iterable[B] = <->(from)
}
