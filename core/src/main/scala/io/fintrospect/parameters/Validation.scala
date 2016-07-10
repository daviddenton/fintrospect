package io.fintrospect.parameters

/**
  * Specialised ADT fot validation of parameters from a request
  */
sealed trait Validation[+T]

case class Validated[T](value: T) extends Validation[T]

case class ValidationFailed(errors: Seq[ExtractionError[String]]) extends Validation[Nothing]

object ValidationFailed {
  def apply(p: ExtractionError[String]): ValidationFailed = ValidationFailed(Seq(p))
}
