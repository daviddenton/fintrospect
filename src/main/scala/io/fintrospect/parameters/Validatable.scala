package io.fintrospect.parameters

/**
  * Provides validation about the presence of a value parameter/entity value in a particular context
  */
trait Validatable[T, -From] {
  /**
    * Attempt to manually deserialise from the message object.
    */
  def <--?(from: From): Extraction[T]

  /**
    * Attempt to manually deserialise from the message object.
    */
  def validate(from: From): Extraction[T] = <--?(from)

}

trait ValidatableParameter[T, -From] {
  self: Parameter with Validatable[T, From] =>

  /**
    * Attempt to manually deserialise from the message object, adding a validation predicate and reason for failure.
    */
  def <--?(from: From, reason: String, predicate: T => Boolean): Extraction[T] = {
    <--?(from).flatMap[T](v => if (v.map(predicate).getOrElse(true)) Extracted.from(v) else ExtractionFailed(InvalidParameter(this, reason)))
  }

  /**
    * Attempt to manually deserialise from the message object, adding a validation predicate and reason for failure.
    */
  def validate(from: From, reason: String, predicate: T => Boolean): Extraction[T] = <--?(from, reason, predicate)
}