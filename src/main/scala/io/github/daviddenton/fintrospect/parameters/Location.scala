package io.github.daviddenton.fintrospect.parameters

import io.github.daviddenton.fintrospect.FinagleTypeAliases.Request

trait Location {
  def from(name: String, request: Request): Option[String]
}
