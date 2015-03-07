package io.github.daviddenton.fintrospect

import scala.reflect.ClassTag

abstract class NamedArgSegmentMatcher[T](val name: String)(implicit m: ClassTag[T]) extends SegmentMatcher[T] {
  override val toParameter = Some(Parameter.path(name)(m))
  override def toString = s"{$name}"
}
