package io.github.daviddenton.fintrospect

import com.twitter.finagle.http.{path => fp}

trait SegmentMatcher[T] {
  val argument: Option[(String, Class[_])] = None

  def unapply(str: String): Option[T]
}


