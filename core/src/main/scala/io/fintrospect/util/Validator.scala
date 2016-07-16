package io.fintrospect.util

import scala.language.implicitConversions

class Validator[In <: Product] private(extractors: Product, value: => In) {
  private val errors = extractors
    .productIterator
    .toSeq
    .flatMap {
      case ExtractionFailed(q) => q
      case _ => Nil
    }

  /**
    * The terminating mechanism for a Validation construct. The Function is used to capture logic for dealing with
    * the successfully extracted parameter instances (much like the "yield" call in the cross-validating case
    */
  def apply[Result](fn: Function[In, Result]): Validation[Result] =
    if (errors.isEmpty) Validated(fn(value)) else ValidationFailed(errors)
}

object Validator {

  type Ex[T] = Extraction[T]
  type Opt[T] = Option[T]

  private def <--[T](e: Extraction[T]): Option[T] = e match {
    case Extracted(v) => v
    case ExtractionFailed(_) => None
  }

  /**
    * Construct a Validator which can be used to enforce several non-crossing extraction rules.
    * Uses magnet pattern (see implicit methods (tupleXToValidator()) to convert vararg instances to a Validator instance.
    */
  def mk[In <: Product](validation: Validator[In]) = validation

  implicit def tuple2ToValidator[A, B](in: (Ex[A], Ex[B])):
  Validator[(Opt[A], Opt[B])] =
    new Validator(in, (<--(in._1), <--(in._2)))

  implicit def tuple3ToValidator[A, B, C](in: (Ex[A], Ex[B], Ex[C])):
  Validator[(Opt[A], Opt[B], Opt[C])] =
    new Validator(in, (<--(in._1), <--(in._2), <--(in._3)))

  implicit def tuple4ToValidator[A, B, C, D](in: (Ex[A], Ex[B], Ex[C], Ex[D])):
  Validator[(Opt[A], Opt[B], Opt[C], Opt[D])] =
    new Validator(in, (<--(in._1), <--(in._2), <--(in._3), <--(in._4)))

  implicit def tuple5ToValidator[A, B, C, D, E](in: (Ex[A], Ex[B], Ex[C], Ex[D], Ex[E])):
  Validator[(Opt[A], Opt[B], Opt[C], Opt[D], Opt[E])] =
    new Validator(in, (<--(in._1), <--(in._2), <--(in._3), <--(in._4), <--(in._5)))

  implicit def tuple6ToValidator[A, B, C, D, E, F](in: (Ex[A], Ex[B], Ex[C], Ex[D], Ex[E], Ex[F])):
  Validator[(Opt[A], Opt[B], Opt[C], Opt[D], Opt[E], Opt[F])] =
    new Validator(in, (<--(in._1), <--(in._2), <--(in._3), <--(in._4), <--(in._5), <--(in._6)))

  implicit def tuple7ToValidator[A, B, C, D, E, F, G]
  (in: (Ex[A], Ex[B], Ex[C], Ex[D], Ex[E], Ex[F], Ex[G])):
  Validator[(Opt[A], Opt[B], Opt[C], Opt[D], Opt[E], Opt[F], Opt[G])] =
    new Validator(in, (<--(in._1), <--(in._2), <--(in._3), <--(in._4), <--(in._5), <--(in._6), <--(in._7)))

  implicit def tuple8ToValidator[A, B, C, D, E, F, G, H]
  (in: (Ex[A], Ex[B], Ex[C], Ex[D], Ex[E], Ex[F], Ex[G], Ex[H])):
  Validator[(Opt[A], Opt[B], Opt[C], Opt[D], Opt[E], Opt[F], Opt[G], Opt[H])] =
    new Validator(in, (<--(in._1), <--(in._2), <--(in._3), <--(in._4), <--(in._5), <--(in._6), <--(in._7), <--(in._8)))

  implicit def tuple9ToValidator[A, B, C, D, E, F, G, H, I]
  (in: (Ex[A], Ex[B], Ex[C], Ex[D], Ex[E], Ex[F], Ex[G], Ex[H], Ex[I])):
  Validator[(Opt[A], Opt[B], Opt[C], Opt[D], Opt[E], Opt[F], Opt[G], Opt[H], Opt[I])] =
    new Validator(in, (<--(in._1), <--(in._2), <--(in._3), <--(in._4), <--(in._5), <--(in._6), <--(in._7), <--(in._8), <--(in._9)))

  implicit def tuple10ToValidator[A, B, C, D, E, F, G, H, I, J]
  (in: (Ex[A], Ex[B], Ex[C], Ex[D], Ex[E], Ex[F], Ex[G], Ex[H], Ex[I], Ex[J])):
  Validator[(Opt[A], Opt[B], Opt[C], Opt[D], Opt[E], Opt[F], Opt[G], Opt[H], Opt[I], Opt[J])] =
    new Validator(in, (<--(in._1), <--(in._2), <--(in._3), <--(in._4), <--(in._5), <--(in._6), <--(in._7), <--(in._8), <--(in._9), <--(in._10)))
}

