package io.fintrospect.parameters

/**
 * Provides validation about the presence of a value parameter/entity value in a particular context
 */
trait Validatable[T, -From] {
  def validate(from: From): Either[Seq[Parameter], Option[T]]
}
