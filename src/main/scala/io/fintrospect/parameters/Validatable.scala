package io.fintrospect.parameters

import scala.util.Try

/**
  * Provides validation about the presence of a value parameter/entity value in a particular context
  */
trait Validatable[T, -From] {

  /**
    * Attempt to deserialise from the message object. Only use this method instead of <--() if you want to not
    * declare your parameters in the RouteSpec(). Returns Failure(InvalidParameters) on failure.
    */
  def <--?(from: From): Try[Option[T]] = validate(from).asTry

  def validate(from: From): Extraction[T]
}
