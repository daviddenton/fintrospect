package io.github.daviddenton.fintrospect

import io.github.daviddenton.fintrospect.parameters.Parameter

object SegmentMatchers {

  def fixed(value: String): SegmentMatcher[String] = new SegmentMatcher[String] {
    val toParameter: Option[Parameter[_]] = None
    override def unapply(str: String): Option[String] = if (str == value) Some(str) else None
    override def toString: String = value
  }
}
