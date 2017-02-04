package io.fintrospect.parameters

trait Value[T] {
  self: AnyVal with Product =>
  val value: T
}
