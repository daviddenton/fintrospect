package io.fintrospect.parameters

trait Bindable[T] {
  def of(value: T): Bindings = ->(value)

  def ->(value: T): Bindings
}
