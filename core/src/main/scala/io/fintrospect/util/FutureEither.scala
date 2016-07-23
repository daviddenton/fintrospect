package io.fintrospect.util

import com.twitter.util.Future

sealed trait EitherF[+L, +R] {
  def map[Ra](f: R => Ra): EitherF[L, Ra]

  def flatMap[La >: L, Ra](f: R => EitherF[La, Ra]): EitherF[La, Ra]
}

case class RightF[R](value: R) extends EitherF[Nothing, R] {
  override def map[Ra](f: R => Ra): EitherF[Nothing, Ra] = RightF(f(value))

  override def flatMap[La >: Nothing, Ra](f: R => EitherF[La, Ra]): EitherF[La, Ra] = f(value)
}

case class LeftF[L](error: L) extends EitherF[L, Nothing] {
  override def map[Ra](f: Nothing => Ra): EitherF[L, Nothing] = this

  override def flatMap[La >: L, O](f: Nothing => EitherF[La, O]): EitherF[La, O] = LeftF(error)
}


class FutureEither[L, R] private(f: Future[EitherF[L, R]]) {
  def map[Ra](next: R => EitherF[L, Ra]): FutureEither[L, Ra] = new FutureEither(f.map(check => check.flatMap(next)))

  def flatMap[Ra](next: R => Future[EitherF[L, Ra]]): FutureEither[L, Ra] =
    new FutureEither[L, Ra](f.flatMap {
      case RightF(v) => next(v)
      case LeftF(e) => Future.value(LeftF(e))
    })

  def end[Ra](fn: EitherF[L, R] => Ra) = f.map(fn)
}

object FutureEither {
  def apply[L, R](a: R): FutureEither[L, R] = new FutureEither[L, R](Future.value(RightF(a)))
}

