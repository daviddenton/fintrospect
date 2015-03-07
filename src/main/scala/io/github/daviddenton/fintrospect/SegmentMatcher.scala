package io.github.daviddenton.fintrospect

import io.github.daviddenton.fintrospect.parameters.Parameter

trait SegmentMatcher[T] {
  val toParameter: Option[Parameter[_]]

  def unapply(str: String): Option[T]
}


