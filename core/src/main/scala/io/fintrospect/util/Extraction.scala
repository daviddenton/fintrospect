package io.fintrospect.util

/**
  * Result of an attempt to extract an object from a target
  */
sealed trait Extraction[+T] {
  def flatMap[O](f: T => Extraction[O]): Extraction[O]

  def map[O](f: T => O): Extraction[O]
}

object Extraction {
  /**
    * Utility method for combining the results of many Extraction into a single Extraction, simply to get an overall
    * extraction result in the case of failure.
    */
  def combine(extractions: Seq[Extraction[_]]): Extraction[Unit] = {
    val missingOrFailed = extractions.flatMap {
      case ExtractionFailed(ip) => ip
      case _ => Nil
    }
    if (missingOrFailed.isEmpty) Extraction(()) else ExtractionFailed(missingOrFailed)
  }

  /**
    * Wraps in a successful extraction - this assumes the item was not mandatory.
    */
  def apply[T](t: T): Extraction[T] = Extracted(t)
}

/**
  * Represents a object which was provided and extracted successfully
  */
case class Extracted[T](value: T) extends Extraction[T] {
  def flatMap[O](f: T => Extraction[O]): Extraction[O] = f(value)

  override def map[O](f: T => O) = Extracted(f(value))
}

/**
  * Represents a object which could not be extracted due to it being invalid or missing when required
  */
case class ExtractionFailed(invalid: Seq[ExtractionError]) extends Extraction[Nothing] {
  def flatMap[O](f: Nothing => Extraction[O]) = ExtractionFailed(invalid)

  override def map[O](f: Nothing => O) = ExtractionFailed(invalid)
}

object ExtractionFailed {
  def apply(p: ExtractionError): ExtractionFailed = ExtractionFailed(Seq(p))
}
