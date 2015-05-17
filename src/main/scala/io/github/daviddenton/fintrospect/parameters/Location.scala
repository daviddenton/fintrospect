package io.github.daviddenton.fintrospect.parameters

import io.github.daviddenton.fintrospect.FinagleTypeAliases.FTRequest

trait Location {
  def from(name: String, request: FTRequest): Option[String]
}
