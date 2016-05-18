package io.fintrospect.parameters

import io.fintrospect.parameters.InvalidParameter.{Invalid, Missing}

import scala.util.{Failure, Success, Try}

/**
  * Result of an attempt to extract an object from a target
  */

sealed trait Extraction[+T] {
  def flatMap[O](f: Option[T] => Extraction[O]): Extraction[O]

  def map[O](f: Option[T] => Option[O]): Extraction[O]

  val invalid: Seq[InvalidParameter]
}

object Extraction {
  def apply[T](parameter: Parameter,
               deserialize: Seq[String] => T,
               fromInput: Option[Seq[String]]): Extraction[T] =
    fromInput.map {
      v =>
        Try(deserialize(v)) match {
          case Success(d) => Extracted(d)
          case Failure(_) => ExtractionFailed(Invalid(parameter))
        }
    }.getOrElse(if (parameter.required) ExtractionFailed(Missing(parameter)) else NotProvided)
}

/**
  * Represents a object which was provided and extracted successfully
  */
case class Extracted[T](value: T) extends Extraction[T] {
  def flatMap[O](f: Option[T] => Extraction[O]) = f(Some(value))

  override val invalid = Nil

  override def map[O](f: Option[T] => Option[O]) = f(Some(value)).map(Extracted(_)).getOrElse(NotProvided)
}

object Extracted {
  def from[T](t: Option[T]) = t.map(Extracted(_)).getOrElse(NotProvided)
}

object NotProvided extends Extraction[Nothing] {
  def flatMap[O](f: Option[Nothing] => Extraction[O]) = f(None)

  override val invalid = Nil

  override def map[O](f: Option[Nothing] => Option[O]) = f(None).map(Extracted(_)).getOrElse(NotProvided)
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

