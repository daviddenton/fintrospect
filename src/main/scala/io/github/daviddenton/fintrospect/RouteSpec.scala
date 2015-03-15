package io.github.daviddenton.fintrospect

trait RouteSpec {
  def attachTo(module: FintrospectModule): FintrospectModule
}
