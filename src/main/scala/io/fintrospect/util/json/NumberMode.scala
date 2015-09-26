package io.fintrospect.util.json

/**
 * Defines how to parse JSON decimals. The default is UseBigDecimal for obvious reasons.
 */
object NumberMode extends Enumeration {
  type NumberMode = Value
  val UseBigDecimal, DoubleMode = Value
}
