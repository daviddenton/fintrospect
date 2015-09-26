package io.fintrospect.util.json

import java.math.BigInteger

/*
 * Provides pluggable Json formats. Use with ParameterSpec
 */
trait JsonFormat[T, N, F] {
  type Field = (String, N)

  def parse(in: String): T

  def pretty(in: T): String

  def compact(in: T): String

  def obj(fields: Iterable[Field]): T

  def obj(fields: Field*): T

  def array(elements: Iterable[N]): N

  def array(elements: N*): N

  def string(value: String): N

  def number(value: Int): N

  def number(value: BigDecimal): N

  def number(value: Long): N

  def number(value: BigInteger): N

  def boolean(value: Boolean): N

  def nullNode(): N
}
