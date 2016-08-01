package io.fintrospect.util

import com.twitter.util.Future

import scala.util.Left

/**
  * Wrapper for Either which terminates in a Future. The API here may look unusual, but is designed to mirror
  * the types of traditional Either operations.
  */
class EitherF[L, R] private(f: Future[Either[L, R]]) {

  /**
    * Traditional map()
    */
  def map[Ra](next: R => Ra): EitherF[L, Ra] = new EitherF(f.map {
    case Right(v) => Right(next(v))
    case Left(e) => Left(e)
  })

  /**
    * Async map()
    */
  def mapF[Ra](next: R => Future[Ra]): EitherF[L, Ra] = new EitherF(f.flatMap {
    case Right(value) => next(value).map(Right(_))
    case Left(e) => Future.value(Left(e))
  })

  /**
    * Traditional flatMap which returns a new wrapped Either
    */
  def flatMap[Ra](next: R => Either[L, Ra]): EitherF[L, Ra] = new EitherF(f.map {
    case Right(v) => next(v)
    case Left(e) => Left(e)
  })

  /**
    * Async flatMap
    */
  def flatMapF[Ra](next: R => Future[Either[L, Ra]]): EitherF[L, Ra] = new EitherF[L, Ra](f.flatMap {
    case Right(v) => next(v)
    case Left(e) => Future.value(Left(e))
  })

  /**
    * This is a replacement of the traditional "match {}" operation, which operates on the contained value
    * in the wrapped Either and returns a Future.
    */
  def matchF[Ra](fn: PartialFunction[Either[L, R], Future[Ra]]): Future[Ra] = f.flatMap(fn)
}

object EitherF {
  def eitherF[L, R](a: R): EitherF[L, R] = new EitherF[L, R](Future.value(Right(a)))

  def eitherF[L, R](a: Future[R]): EitherF[L, R] = new EitherF[L, R](a.map(Right(_)))

  def eitherF[L, R](next: => Either[L, R]): EitherF[L, R] = new EitherF[L, R](Future.value(next))
}

