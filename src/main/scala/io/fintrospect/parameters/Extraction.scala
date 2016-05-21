package io.fintrospect.parameters

/**
  * Result of an attempt to extract an object from a target
  */
sealed trait Extraction[+T] {
  def flatMap[O](f: Option[T] => Extraction[O]): Extraction[O]

  def map[O](f: Option[T] => O): Extraction[O]

  protected val invalid: Seq[InvalidParameter]
}

object Extraction {
  /**
    * Utility method for combining the results of many Extraction into a single Extraction, simply to get an overall
    * extraction result in the case of failure.
    */
  def combine(extractions: Seq[Extraction[_]]): Extraction[Nothing] = {
    val missingOrFailed = extractions.flatMap(_.invalid)
    if (missingOrFailed.isEmpty) NotProvided else ExtractionFailed(missingOrFailed)
  }

  /**
    * Wraps in a successful extraction - this assumes the item was not mandatory.
    */
  def apply[T](t: Option[T]): Extraction[T] = t.map(Extracted(_)).getOrElse(NotProvided)
}

/**
  * Represents a object which was provided and extracted successfully
  */
case class Extracted[T](value: T) extends Extraction[T] {
  def flatMap[O](f: Option[T] => Extraction[O]) = f(Some(value))

  protected override val invalid = Nil

  override def map[O](f: Option[T] => O) = Extracted(f(Some(value)))
}

/**
  * Represents an optional object which was not provided - this is still a non-failing case
  */
object NotProvided extends Extraction[Nothing] {
  def flatMap[O](f: Option[Nothing] => Extraction[O]) = f(None)

  protected override val invalid = Nil

  override def map[O](f: Option[Nothing] => O) = NotProvided
}

/**
  * Represents a object which could not be extracted due to it being invalid or missing when required
  */
case class ExtractionFailed[T](protected val invalid: Seq[InvalidParameter]) extends Extraction[T] {
  def flatMap[O](f: Option[T] => Extraction[O]) = ExtractionFailed(invalid)

  override def map[O](f: Option[T] => O) = ExtractionFailed(invalid)
}

object ExtractionFailed {
  def apply[T](p: InvalidParameter): ExtractionFailed[T] = ExtractionFailed(Seq(p))
}

