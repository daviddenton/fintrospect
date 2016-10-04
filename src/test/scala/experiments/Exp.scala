package experiments

import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response}

trait Builder[T] {
}

class Builder0[Base](base: Base) extends Builder[Base] {
  def /[A](a: Gen[A]) = new Builder1(base, a)
}

class Builder1[Base, A](base: Base, a: Gen[A]) extends Builder[Base] {
  def /[B](b: Gen[B]) = new Builder2(base, a, b)
}

class Builder2[Base, A, B](base: Base, a: Gen[A], b: Gen[B]) extends Builder[Base] {
}

trait Contract {
  self =>
  type T

  def gens: Seq[Gen[_]]

  def at(): Builder0[self.type] = new Builder0(this)
}

case class Gen[T](t: T) {
  def get: T = t
}

class Contract0() extends Contract {
  type T = () => Service[Request, Response]

  val gens = Nil

  def taking[A](a: Gen[A]): Contract1[A] = new Contract1(a)
}

class Contract1[A](a: Gen[A]) extends Contract {
  type T = (A) => Service[Request, Response]

  val gens = Seq(a)

  def taking[B](b: Gen[B]) = new Contract2(a, b)
}

class Contract2[A, B](a: Gen[A], b: Gen[B]) extends Contract {
  type T = (A, B) => Service[Request, Response]

  val gens = Seq(a, b)
}


object Test extends App {
  private val builder = new Contract0().taking(Gen("string")).taking(Gen(123)).at() / Gen('a') / Gen(true)
}