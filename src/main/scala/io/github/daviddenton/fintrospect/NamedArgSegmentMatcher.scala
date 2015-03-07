package io.github.daviddenton.fintrospect

import scala.reflect.ClassTag

class NamedArgSegmentMatcher[T](parameter: PathParameter[T])(implicit m: ClassTag[T]) extends SegmentMatcher[T] {
  override val toParameter: Option[Parameter[_]] = Some(parameter)
  def unapply(str: String): Option[T] = parameter.unapply(str)
  override def toString = parameter.toString
}


