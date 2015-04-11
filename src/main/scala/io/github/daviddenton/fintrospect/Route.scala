package io.github.daviddenton.fintrospect

/**
 * A Fintrospect route definition, convenient for grouping the description and the implementation in a single place.
 */
trait Route {
  def attachTo(module: FintrospectModule2): FintrospectModule2
}
