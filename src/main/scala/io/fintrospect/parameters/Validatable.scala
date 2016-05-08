package io.fintrospect.parameters

import scala.util.{Failure, Success, Try}

/**
  * Provides validation about the presence of a value parameter/entity value in a particular context
  */
trait Validatable[T, -From] {

  /**
    * Attempt to deserialise from the message object. Only use this method instead of <--() if you want to not
    * declare your parameters in the RouteSpec(). Returns Failure(InvalidParameters) on failure.
    */
  def <--?(from: From): Try[Option[T]] = validate(from).fold(s => Failure(new InvalidParameters(s)), o => Success(o))

  def validate(from: From): Either[Seq[Parameter], Option[T]]
}
