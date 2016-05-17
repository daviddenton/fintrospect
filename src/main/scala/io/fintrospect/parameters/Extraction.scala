package io.fintrospect.parameters

import com.twitter.finagle.Filter
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Future
import io.fintrospect.parameters.InvalidParameter.{Invalid, Missing}
import io.fintrospect.renderers.ModuleRenderer

import scala.util.{Failure, Success, Try}

/**
  * Result of an attempt to extract an object from a target
  */
sealed trait Extraction[+T] {
  def flatMap[O](f: T => Extraction[O]): Extraction[O]

  def map[O](f: T => O): Extraction[O]

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
  * Represents an optional object which was not provided
  */
case class NotProvided[T]() extends Extraction[T] {
  def flatMap[O](f: T => Extraction[O]) = NotProvided()

  override val invalid = Nil

  override def map[O](f: (T) => O) = NotProvided()
}

/**
  * Represents a object which was provided and extracted successfully
  */
case class Extracted[T](value: T) extends Extraction[T] {
  def flatMap[O](f: T => Extraction[O]) = f(value)

  override val invalid = Nil

  override def map[O](f: (T) => O) = Extracted(f(value))
}

/**
  * Represents a object which could not be extracted due to it being invalid or missing when required
  */
case class ExtractionFailed[T](invalid: Seq[InvalidParameter]) extends Extraction[T] {
  def flatMap[O](f: T => Extraction[O]) = ExtractionFailed(invalid)

  override def map[O](f: (T) => O) = ExtractionFailed(invalid)
}

object ExtractionFailed {
  def apply[T](p: InvalidParameter): ExtractionFailed[T] = ExtractionFailed(Seq(p))
}

