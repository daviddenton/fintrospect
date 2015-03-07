package io.github.daviddenton.fintrospect.parameters

import com.twitter.finagle.http.Request

trait Location {
  def from(name: String, request: Request): Option[String]
}
