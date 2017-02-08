package io.fintrospect

/**
  * This exception is thrown at runtime when not all of the required parameters have been passed to the RouteClient
  * function
  */
class BrokenContract(message: String) extends Exception(message)
