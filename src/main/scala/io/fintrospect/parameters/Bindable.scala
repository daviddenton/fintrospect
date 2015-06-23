package io.fintrospect.parameters

trait Bindable[T] {
  def of(value: T): Binding = ->(value)

  def ->(value: T): Binding
}
