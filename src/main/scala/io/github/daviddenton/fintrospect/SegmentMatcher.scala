package io.github.daviddenton.fintrospect

trait SegmentMatcher[T] {
  val toParameter: Option[Parameter] = None

  def unapply(str: String): Option[T]
}


