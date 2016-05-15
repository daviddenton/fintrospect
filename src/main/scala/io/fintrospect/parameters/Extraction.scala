package io.fintrospect.parameters

import scala.util.Either.RightProjection
import scala.util.{Failure, Success, Try}

class InvalidParameters(invalid: Seq[Parameter]) extends Exception(
  invalid.flatMap(_.description).mkString(", ")
)

/**
  * Result of an attempt to extract a parameter from a target
  */
sealed trait Extraction[T] {
  def asRight: RightProjection[Seq[Parameter], Option[T]]
  def asTry: Try[Option[T]]

  val invalid: Seq[Parameter]
}

object Extraction {
  def forMissingParam[T](p: Parameter): Extraction[T] = if (p.required) Missing(p) else NotProvided()
}

/**
  * Represents an optional parameter which was not provided
  */
case class NotProvided[T]() extends Extraction[T] {
  def asRight = Right(None).right

  override def asTry: Try[Option[T]] = Success(None)

  override val invalid = Nil
}

/**
  * Represents a parameter which was provided and extracted successfully
  */
case class Extracted[T](value: T) extends Extraction[T] {
  def asRight = Right(Some(value)).right

  override def asTry: Try[Option[T]] = Success(Some(value))

  override val invalid = Nil
}

/**
  * Represents a parameter which was required and missing
  */
case class Missing[T](params: Seq[Parameter]) extends Extraction[T] {
  def asRight = Left(params).right

  override def asTry: Try[Option[T]] = Failure(new InvalidParameters(params))

  override val invalid = params
}

object Missing {
  def apply[T](p: Parameter): Missing[T] = Missing(Seq(p))
}

/**
  * Represents a parameter which was provided and in an invalid format
  */
case class Invalid[T](params: Seq[Parameter]) extends Extraction[T] {
  def asRight = Left(params).right

  override def asTry: Try[Option[T]] = Failure(new InvalidParameters(params))

  override val invalid = params
}

object Invalid {
  def apply[T](p: Parameter): Invalid[T] = Invalid(Seq(p))
}
