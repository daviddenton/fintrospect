package io.fintrospect.parameters

import io.fintrospect.util.{Extracted, Extractor}

/**
  * Represents the ability to retrieve a value from an enclosing object (request/form etc..)
  */
trait Retrieval[-From, T] {

  /**
    * Extract the value from the target object. Throws on failure, but that shouldn't be a problem as the pre-validation
    * stage for declared parameters and bodies handles the failure before user code is entered.
    */
  def <--(from: From): T

  /**
    * Extract the value from the target object. Throws on failure, but that shouldn't be a problem as the pre-validation
    * stage for declared parameters and bodies handles the failure before user code is entered.
    * User-friendly synonym for <--(), which is why the method is final.
    */
  final def from(from: From): T = <--(from)
}

trait Mandatory[-From, T] extends Retrieval[From, T] with Extractor[From, T] {
  val required = true

  override def <--(from: From): T = extract(from) match {
    case Extracted(Some(t)) => t
    case Extracted(None) => throw new IllegalStateException("Extraction failed: Missing")
    case _ => throw new IllegalStateException("Extraction failed: Invalid")
  }
}

trait Optional[-From, T] extends Retrieval[From, Option[T]] with Extractor[From, T] {
  val required = false

  def <--(from: From): Option[T] = extract(from) match {
    case Extracted(value) => value
    case _ => throw new IllegalStateException("Extraction failed: Invalid")
  }
}
