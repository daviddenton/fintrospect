package io.fintrospect.parameters

trait Bindable[T] {
  def of(value: T): ParamBinding[T] = ->(value)

  def ->(value: T): ParamBinding[T]
}
