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
          case Success(d) => Extracted(Some(d))
          case Failure(_) => ExtractionFailed(Invalid(parameter))
        }
    }.getOrElse(if (parameter.required) ExtractionFailed(Missing(parameter)) else Extracted(None))
}

/**
  * Represents a object which was provided and extracted successfully
  */
case class Extracted[T](value: Option[T]) extends Extraction[T] {
  def flatMap[O](f: Option[T] => Extraction[O]) = f(value)

  override val invalid = Nil

  override def map[O](f: Option[T] => Option[O]) = Extracted(f(value))
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

