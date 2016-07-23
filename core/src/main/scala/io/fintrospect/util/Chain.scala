package io.fintrospect.util

import com.twitter.util.Future

sealed trait EitherF[+R, +L] {
  def map[O](f: R => O): EitherF[O, L]

  def flatMap[O, EO >: L](f: R => EitherF[O, EO]): EitherF[O, EO]
}

case class RightF[I](value: I) extends EitherF[I, Nothing] {
  override def map[O](f: I => O): EitherF[O, Nothing] = RightF(f(value))

  override def flatMap[O, EO >: Nothing](f: I => EitherF[O, EO]): EitherF[O, EO] = f(value)
}

case class LeftF[E](error: E) extends EitherF[Nothing, E] {
  override def map[O](f: Nothing => O): EitherF[Nothing, E] = this

  override def flatMap[O, EO >: E](f: Nothing => EitherF[O, EO]): EitherF[O, EO] = LeftF(error)
}

class Chain[A, E] private(f: Future[EitherF[A, E]]) {
  def map[B](next: A => EitherF[B, E]): Chain[B, E] = new Chain(f.map(check => check.flatMap(next)))

  def flatMap[B](next: A => Future[EitherF[B, E]]): Chain[B, E] =
    new Chain[B, E](f.flatMap {
      case RightF(v) => next(v)
      case LeftF(e) => Future.value(LeftF(e))
    })

  def end[B](fn: EitherF[A, E] => B) = f.map(fn)
}

object Chain {
  def apply[A, E](a: A): Chain[A, E] = new Chain[A, E](Future.value(RightF(a)))
}

