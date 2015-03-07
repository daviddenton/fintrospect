package io.github.daviddenton.fintrospect

import scala.reflect.ClassTag

class NamedArgSegmentMatcher[T](parameter: RequestParameter[T])(implicit m: ClassTag[T]) extends SegmentMatcher[T] {
  def unapply(str: String): Option[T] = parameter.unapply(str)
  override def toString = s"{${parameter.name}}"
}


