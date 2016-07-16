package io.fintrospect.util

/**
  * Result of an attempt to extract an object from a target
  */
sealed trait Extraction[+T] {
  def flatMap[O](f: Option[T] => Extraction[O]): Extraction[O]

  def map[O](f: Option[T] => O): Extraction[O]

  def orDefault[O >: T](f: => O): Extraction[O]
}

object Extraction {
  /**
    * Utility method for combining the results of many Extraction into a single Extraction, simply to get an overall
    * extraction result in the case of failure.
    */
  def combine(extractions: Seq[Extraction[_]]): Extraction[Nothing] = {
    val missingOrFailed = extractions.flatMap {
      case ExtractionFailed(ip) => ip
      case _ => Nil
    }
    if (missingOrFailed.isEmpty) Extraction(None) else ExtractionFailed(missingOrFailed)
  }

  /**
    * Wraps in a successful extraction - this assumes the item was not mandatory.
    */
  def apply[T](t: Option[T]): Extraction[T] = Extracted(t)
}

/**
  * Represents a object which was provided and extracted successfully
  */
case class Extracted[T](value: Option[T]) extends Extraction[T] {
  def flatMap[O](f: Option[T] => Extraction[O]) = f(value)

  override def map[O](f: Option[T] => O) = Extracted(Some(f(value)))

  override def orDefault[O >: T](f: => O): Extraction[O] = Extracted(value.orElse(Some(f)))
}

/**
  * Represents a object which could not be extracted due to it being invalid or missing when required
  */
case class ExtractionFailed(invalid: Seq[ExtractionError]) extends Extraction[Nothing] {
  def flatMap[O](f: Option[Nothing] => Extraction[O]) = ExtractionFailed(invalid)

  override def map[O](f: Option[Nothing] => O) = ExtractionFailed(invalid)

  override def orDefault[T](f: => T): Extraction[T] = this
}

object ExtractionFailed {
  def apply(p: ExtractionError): ExtractionFailed = ExtractionFailed(Seq(p))
}
