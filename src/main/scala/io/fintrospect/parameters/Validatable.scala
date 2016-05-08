package io.fintrospect.parameters

/**
  * Provides validation about the presence of a value parameter/entity value in a particular context
  */
trait Validatable[T, -From] {
  /**
    * Attempt to deserialise from the message object. Only use this method instead of <--() if you want to not
    * declare your parameters in the RouteSpec().
    */
  def <--?(from: From): Extraction[T]

  /**
    * Attempt to deserialise from the message object. Only use this method instead of <--() if you want to not
    * declare your parameters in the RouteSpec().
    */
  def validate(from: From): Extraction[T] = <--?(from)
}
