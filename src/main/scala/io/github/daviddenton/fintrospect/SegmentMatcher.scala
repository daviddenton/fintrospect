package io.github.daviddenton.fintrospect

trait SegmentMatcher[T] {
  val toParameter: Option[Parameter[_]] = None

  def unapply(str: String): Option[T]
}


