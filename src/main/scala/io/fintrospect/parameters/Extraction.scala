package io.fintrospect.parameters

/**
  * Result of an attempt to extract an object from a target
  */
sealed trait Extraction[+T] {
  def flatMap[O](f: Option[T] => Extraction[O]): Extraction[O]

  def map[O](f: Option[T] => Option[O]): Extraction[O]

  val invalid: Seq[InvalidParameter]
}

object Extraction {
  /**
    * Wraps in a successful extraction.
    */
  def apply[T](t: Option[T]): Extraction[T] = t.map(Extracted(_)).getOrElse(NotProvided)
}

/**
  * Represents a object which was provided and extracted successfully
  */
case class Extracted[T](value: T) extends Extraction[T] {
  def flatMap[O](f: Option[T] => Extraction[O]) = f(Some(value))

  override val invalid = Nil

  override def map[O](f: Option[T] => Option[O]) = Extraction(f(Some(value)))
}

/**
  * Represents an optional object which was not provided - this is still a non-failing case
  */
object NotProvided extends Extraction[Nothing] {
  def flatMap[O](f: Option[Nothing] => Extraction[O]) = f(None)

  override val invalid = Nil

  override def map[O](f: Option[Nothing] => Option[O]) = Extraction(f(None))
}

/**
  * Represents a object which could not be extracted due to it being invalid or missing when required
  */
case class ExtractionFailed[T](invalid: Seq[InvalidParameter]) extends Extraction[T] {
  def flatMap[O](f: Option[T] => Extraction[O]) = ExtractionFailed(invalid)

  override def map[O](f: Option[T] => Option[O]) = ExtractionFailed(invalid)
}

object ExtractionFailed {
  def apply[T](p: InvalidParameter): ExtractionFailed[T] = ExtractionFailed(Seq(p))
}

