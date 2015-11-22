package io.fintrospect.parameters

/**
  * For optional Parameters, adds the ability to bind an Optional value as well as a concrete value
  */
trait OptionalBindable[T, B <: Binding] extends Bindable[T, B] {
  def -->(value: Option[T]): Iterable[B] = value.map(-->).getOrElse(Nil)
}
