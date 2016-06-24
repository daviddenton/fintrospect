package io.fintrospect.parameters


import scala.language.implicitConversions

class Validator[In <: Product] private(extractors: Product, value: => In) {
  private val errors = extractors
    .productIterator
    .filter(_.isInstanceOf[Extraction[_]])
    .map(_.asInstanceOf[Extraction[_]]).toList
    .flatMap {
      case ExtractionFailed(q) => q
      case _ => Nil
    }

  /**
    * The terminating mechanism for a Validation construct. The PartialFunction is used to capture logic for dealing with
    * the successfully extracted parameter instances (much like the "yield" call in the cross-validating case
    */
  def apply[Result](pf: Function[In, Result]): Validation[Result] =
    if (errors.isEmpty) Validated(pf(value)) else ValidationFailed(errors)
}

object Validator {

  type Ex[T] = Extraction[T]
  type Op[T] = Option[T]

  private def <--[T](e: Extraction[T]): Option[T] = e match {
    case Extracted(v) => Some(v)
    case NotProvided => None
    case ExtractionFailed(_) => None
  }

  /**
    * Construct a Validator which can be used to enforce several non-crossing extraction rules.
    * Uses magnet pattern (see implicit methods (tupleXToValidator()) to convert vararg instances to a Validator instance.
    */
  def mk[In <: Product](validation: Validator[In]) = validation

  implicit def tuple2ToValidator[A, B](in: (Ex[A], Ex[B])):
  Validator[(Op[A], Op[B])] =
    new Validator(in, (<--(in._1), <--(in._2)))

  implicit def tuple3ToValidator[A, B, C](in: (Ex[A], Ex[B], Ex[C])):
  Validator[(Op[A], Op[B], Op[C])] =
    new Validator(in, (<--(in._1), <--(in._2), <--(in._3)))

  implicit def tuple4ToValidator[A, B, C, D](in: (Ex[A], Ex[B], Ex[C], Ex[D])):
  Validator[(Op[A], Op[B], Op[C], Op[D])] =
    new Validator(in, (<--(in._1), <--(in._2), <--(in._3), <--(in._4)))

  implicit def tuple5ToValidator[A, B, C, D, E](in: (Ex[A], Ex[B], Ex[C], Ex[D], Ex[E])):
  Validator[(Op[A], Op[B], Op[C], Op[D], Op[E])] =
    new Validator(in, (<--(in._1), <--(in._2), <--(in._3), <--(in._4), <--(in._5)))

  implicit def tuple6ToValidator[A, B, C, D, E, F](in: (Ex[A], Ex[B], Ex[C], Ex[D], Ex[E], Ex[F])):
  Validator[(Op[A], Op[B], Op[C], Op[D], Op[E], Op[F])] =
    new Validator(in, (<--(in._1), <--(in._2), <--(in._3), <--(in._4), <--(in._5), <--(in._6)))

  implicit def tuple7ToValidator[A, B, C, D, E, F, G]
  (in: (Ex[A], Ex[B], Ex[C], Ex[D], Ex[E], Ex[F], Ex[G])):
  Validator[(Op[A], Op[B], Op[C], Op[D], Op[E], Op[F], Op[G])] =
    new Validator(in, (<--(in._1), <--(in._2), <--(in._3), <--(in._4), <--(in._5), <--(in._6), <--(in._7)))

  implicit def tuple8ToValidator[A, B, C, D, E, F, G, H]
  (in: (Ex[A], Ex[B], Ex[C], Ex[D], Ex[E], Ex[F], Ex[G], Ex[H])):
  Validator[(Op[A], Op[B], Op[C], Op[D], Op[E], Op[F], Op[G], Op[H])] =
    new Validator(in, (<--(in._1), <--(in._2), <--(in._3), <--(in._4), <--(in._5), <--(in._6), <--(in._7), <--(in._8)))

  implicit def tuple9ToValidator[A, B, C, D, E, F, G, H, I]
  (in: (Ex[A], Ex[B], Ex[C], Ex[D], Ex[E], Ex[F], Ex[G], Ex[H], Ex[I])):
  Validator[(Op[A], Op[B], Op[C], Op[D], Op[E], Op[F], Op[G], Op[H], Op[I])] =
    new Validator(in, (<--(in._1), <--(in._2), <--(in._3), <--(in._4), <--(in._5), <--(in._6), <--(in._7), <--(in._8), <--(in._9)))

  implicit def tuple10ToValidator[A, B, C, D, E, F, G, H, I, J]
  (in: (Ex[A], Ex[B], Ex[C], Ex[D], Ex[E], Ex[F], Ex[G], Ex[H], Ex[I], Ex[J])):
  Validator[(Op[A], Op[B], Op[C], Op[D], Op[E], Op[F], Op[G], Op[H], Op[I], Op[J])] =
    new Validator(in, (<--(in._1), <--(in._2), <--(in._3), <--(in._4), <--(in._5), <--(in._6), <--(in._7), <--(in._8), <--(in._9), <--(in._10)))
}

