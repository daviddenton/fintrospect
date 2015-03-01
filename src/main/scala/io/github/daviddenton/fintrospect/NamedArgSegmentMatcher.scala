package io.github.daviddenton.fintrospect

abstract class NamedArgSegmentMatcher[T](val name: String)(implicit m: Manifest[T]) extends SegmentMatcher[T] {
  override val argument = Some((name, m.runtimeClass))

  override def toString = s"{$name}"
}
