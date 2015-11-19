package io.fintrospect.parameters

trait Builder[T] {
  def build(): T
}
