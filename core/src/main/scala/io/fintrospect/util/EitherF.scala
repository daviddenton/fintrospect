package io.fintrospect.util

import com.twitter.util.Future

import scala.util.Left

/**
  * Wrapper for Either which supports Futures as the wrapping type
  */
class EitherF[L, R] private(f: Future[Either[L, R]]) {
  def map[Ra](next: R => Either[L, Ra]): EitherF[L, Ra] = new EitherF(f.map {
    case Right(v) => next(v)
    case Left(e) => Left(e)
  })

  def flatMap[Ra](next: R => Future[Either[L, Ra]]): EitherF[L, Ra] =
    new EitherF[L, Ra](f.flatMap {
      case Right(v) => next(v)
      case Left(e) => Future.value(Left(e))
    })

  def end[Ra](fn: Either[L, R] => Ra) = f.map(fn)
}

object EitherF {
  def eitherF[L, R](a: R): EitherF[L, R] = new EitherF[L, R](Future.value(Right(a)))
  def eitherF[L, R](a: Future[R]): EitherF[L, R] = new EitherF[L, R](a.map(Right(_)))
  def eitherF[L, R](next: => Either[L, R]): EitherF[L, R] = new EitherF[L, R](Future.value(next))
}

