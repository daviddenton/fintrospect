package io.fintrospect.util

import com.twitter.util.Future

sealed trait Check[+I] {
  def map[O](f: I => O): Check[O]

  def flatMap[O](f: I => Check[O]): Check[O]
}

case class Pass[I](value: I) extends Check[I] {
  override def map[O](f: I => O): Check[O] = Pass(f(value))

  override def flatMap[O](f: I => Check[O]): Check[O] = f(value)
}

case class Fail[E](error: E) extends Check[Nothing] {
  override def map[O](f: Nothing => O): Check[Nothing] = this

  override def flatMap[O](f: Nothing => Check[O]): Check[O] = this
}

case class Chain[A](f: Future[Check[A]]) {
  def map[B](next: A => Check[B]): Chain[B] = new Chain(f.map(check => check.flatMap(next)))

  def flatMap[B](next: A => Future[Check[B]]): Chain[B] =
    Chain(f.flatMap {
      case Pass(v) => next(v)
      case Fail(v) => Future.value(Fail(v))
    })

  def finish[B](fn: Check[A] => B) = f.map(fn)
}

trait ChainMagnet[A] extends (() => Chain[A])

object ChainMagnet {
  implicit def valueToMagnet[A](a: A): ChainMagnet[A] = new ChainMagnet[A] {
    override def apply(): Chain[A] = new Chain[A](Future.value(Pass(a)))
  }

  implicit def checkToMagnet[A](a: Check[A]): ChainMagnet[A] = new ChainMagnet[A] {
    override def apply(): Chain[A] = new Chain[A](Future.value(a))
  }

  implicit def futureToMagnet[A](fa: Future[A]): ChainMagnet[A] = new ChainMagnet[A] {
    override def apply(): Chain[A] = new Chain[A](fa.map(Pass(_)))
  }
}

object Chain {
  def apply[A](magnet: ChainMagnet[A]): Chain[A] = magnet()
}

