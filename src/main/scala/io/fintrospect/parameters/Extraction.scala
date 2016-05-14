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

  def forMissingParam[T](p: Parameter): Extraction[T] = if (p.required) MissingOrInvalid(p) else NotProvided()
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
  * Represents a parameter which was either required and missing, or provided and in an invalid format
  */
case class MissingOrInvalid[T](missingOrInvalid: Seq[Parameter]) extends Extraction[T] {
  def asRight = Left(missingOrInvalid).right

  override def asTry: Try[Option[T]] = Failure(new InvalidParameters(missingOrInvalid))

  override val invalid = missingOrInvalid
}

object MissingOrInvalid {
  def apply[T](p: Parameter): MissingOrInvalid[T] = MissingOrInvalid(Seq(p))
}