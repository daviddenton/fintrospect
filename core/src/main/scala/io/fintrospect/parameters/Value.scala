package io.fintrospect.parameters

trait Value[T] extends Any {
  self: AnyVal with Product =>
  def value: T
}
