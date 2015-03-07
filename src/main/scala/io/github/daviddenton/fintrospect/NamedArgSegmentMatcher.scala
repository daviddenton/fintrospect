package io.github.daviddenton.fintrospect

import scala.reflect.ClassTag

class NamedArgSegmentMatcher[T](val parameter: RequestParameter[T])(implicit m: ClassTag[T]) extends SegmentMatcher[T] {
  def unapply(str: String): Option[T] = parameter.mapper(str)
  override def toString = s"{${parameter.name}}"
}


