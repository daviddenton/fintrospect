package io.fintrospect

import io.fintrospect.parameters.Extraction

/**
  * Mechanism to extract (or fail to extract) an entity value from a particular 'From' context
  */
trait Extractable[-From, T] {
  /**
    * Attempt to manually deserialise from the message object.
    */
  def <--?(from: From): Extraction[T]

  /**
    * Attempt to manually deserialise from the message object.
    * User-friendly synonym for <--?(), which is why the method is final.
    */
  final def extract(from: From): Extraction[T] = <--?(from)
}

object Extractable {
  /**
    * Create an extractable from a simple function. This is stylistic, similar to Service.mk and Filter.mk
    */
  def mk[From, T](fn:From => Extraction[T]): Extractable[From, T] = new Extractable[From, T] {
    override def <--?(from: From): Extraction[T] = fn(from)
  }
}