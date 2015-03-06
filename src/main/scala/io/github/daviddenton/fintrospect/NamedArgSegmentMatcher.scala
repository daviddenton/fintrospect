package io.github.daviddenton.fintrospect

abstract class NamedArgSegmentMatcher[T](val name: String)(implicit m: Manifest[T]) extends SegmentMatcher[T] {
  override val toParameter = Some(Parameter.path(name, m.runtimeClass))
  override def toString = s"{$name}"
}
