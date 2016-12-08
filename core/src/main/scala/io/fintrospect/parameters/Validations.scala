package io.fintrospect.parameters

import scala.language.reflectiveCalls

/**
  * Reusable validation functions for parameter and body types
  */
trait Validations[T <: {def isEmpty() : Boolean}] {
  type Rule = (T => T)
  val EmptyIsValid: Rule = in => if (in == null) throw new IllegalArgumentException("Cannot be null") else in
  val EmptyIsInvalid: Rule = in => if (in.isEmpty()) throw new IllegalArgumentException("Cannot be empty") else in
}

object StringValidations extends Validations[String]

object FileValidations extends Validations[MultiPartFile]
