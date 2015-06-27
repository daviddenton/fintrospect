package io.fintrospect.parameters

trait Bindable[T, B <: Binding] {
  def of(value: T): Iterable[B] = -->(value)

  def -->(value: T): Iterable[B]
}
