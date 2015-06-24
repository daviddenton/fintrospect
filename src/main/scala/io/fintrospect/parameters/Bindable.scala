package io.fintrospect.parameters

trait Bindable[T] {
  def of(value: T): Iterable[Binding] = ->(value)

  def ->(value: T): Iterable[Binding]
}
