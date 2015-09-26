package io.fintrospect.util.json

/*
 * Provides pluggable Json formats. Use with ParameterSpec
 */
trait JsonFormat[T, F] {
  type Field = (String, F)

  def parse(in: String): T
  def pretty(in: T): String
  def compact(in: T): String
  def obj(fields: Iterable[Field]):T
}
