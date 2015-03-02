package io.github.daviddenton.fintrospect

trait SegmentMatcher[T] {
  val argument: Option[(String, Class[_])] = None

  def unapply(str: String): Option[T]
}


