package io.github.daviddenton.fintrospect

trait SegmentMatcher[T] {
  val argument: Option[Parameter] = None

  def unapply(str: String): Option[T]
}


