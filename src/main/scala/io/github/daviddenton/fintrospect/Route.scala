package io.github.daviddenton.fintrospect

trait Route {
  def attachTo(module: FintrospectModule): FintrospectModule
}
