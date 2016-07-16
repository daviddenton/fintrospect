package io.fintrospect.util

/**
  * Specialised ADT fot validation of parameters from a request
  */
sealed trait Validation[+T]

case class Validated[T](value: T) extends Validation[T]

case class ValidationFailed(errors: Seq[ExtractionError]) extends Validation[Nothing]

object ValidationFailed {
  def apply(p: ExtractionError): ValidationFailed = ValidationFailed(Seq(p))
}
