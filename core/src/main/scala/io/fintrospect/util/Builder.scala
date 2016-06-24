package io.fintrospect.util

trait Builder[T] {
  def build(): T
}
