package io.fintrospect.parameters

/**
  * Reusable validation functions for parameter and body types
  */
object StringValidations {
  type Rule = (String => String)
  val EmptyIsValid: Rule = in => if (in == null) throw new IllegalArgumentException("Cannot be null") else in
  val EmptyIsInvalid: Rule = in => if (in.isEmpty) throw new IllegalArgumentException("Cannot be empty") else in
}

