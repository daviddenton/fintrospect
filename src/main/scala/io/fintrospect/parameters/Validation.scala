package io.fintrospect.parameters

/**
  * Specialised ADT fot validation of parameters from a request
  */
sealed trait Validation[+T]

case class Validated[T](value: T) extends Validation[T]

case class ValidationFailed(errors: Seq[InvalidParameter]) extends Validation[Nothing]

object ValidationFailed {
  def apply(p: InvalidParameter): ValidationFailed = ValidationFailed(Seq(p))
}
