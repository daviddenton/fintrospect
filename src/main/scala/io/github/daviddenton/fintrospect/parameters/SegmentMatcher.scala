package io.github.daviddenton.fintrospect.parameters

trait SegmentMatcher[T] {
  val toParameter: Option[Parameter[_]]
  def unapply(str: String): Option[T]
}


