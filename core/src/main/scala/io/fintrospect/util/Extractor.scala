package io.fintrospect.util

/**
  * Mechanism to extract (or fail to extract) an entity value from a particular 'From' context
  */
trait Extractor[-From, +T] {
  /**
    * Attempt to manually deserialize from the message object.
    */
  def <--?(from: From): Extraction[T]

  /**
    * Attempt to manually deserialize from the message object.
    * User-friendly synonym for <--?(), which is why the method is final.
    */
  final def extract(from: From): Extraction[T] = <--?(from)
}

object Extractor {
  /**
    * Create an extractor from a simple function. This is stylistic, similar to Service.mk and Filter.mk
    */
  def mk[From, T](fn: From => Extraction[T]): Extractor[From, T] = new Extractor[From, T] {
    override def <--?(from: From): Extraction[T] = fn(from)
  }
}
