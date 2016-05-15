package io.fintrospect.parameters

import io.fintrospect.parameters.InvalidParameter.{Invalid, Missing}

import scala.util.Either.RightProjection
import scala.util.{Failure, Success, Try}

/**
  * Result of an attempt to extract a parameter from a target
  */
sealed trait Extraction[+T] {
  def map[O](f: T => O): Extraction[O]

  def asRight: RightProjection[Seq[InvalidParameter], Option[T]]

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
    }.getOrElse(if (parameter.required) ExtractionFailed(Missing(parameter)) else NotProvided())
}

/**
  * Represents an optional parameter which was not provided
  */
case class NotProvided[T]() extends Extraction[T] {
  def asRight = Right(None).right

  override val invalid = Nil

  override def map[O](f: (T) => O) = NotProvided()
}

/**
  * Represents a parameter which was provided and extracted successfully
  */
case class Extracted[T](value: T) extends Extraction[T] {
  def asRight = Right(Some(value)).right

  override val invalid = Nil

  override def map[O](f: (T) => O) = Extracted(f(value))
}

/**
  * Represents a parameter which could not be extracted
  */
case class ExtractionFailed[T](invalid: Seq[InvalidParameter]) extends Extraction[T] {
  def asRight = Left(invalid).right
  override def map[O](f: (T) => O) = ExtractionFailed(invalid)

}

object ExtractionFailed {
  def apply[T](p: InvalidParameter): ExtractionFailed[T] = ExtractionFailed(Seq(p))
}
