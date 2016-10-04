package experiments

import com.twitter.finagle.Service
import com.twitter.finagle.http.path.{->, /, Path}
import com.twitter.finagle.http.{Method, Request, Response}

trait Builder[T] {
}

class Builder0[Base](base: Base) extends Builder[Base] {
  def /[A](a: Gen[A]) = new Builder1(base, a)
}

class Builder1[Base, A](base: Base, a: Gen[A]) extends Builder[Base] {
  def /[B](b: Gen[B]) = new Builder2(base, a, b)
}

trait SR {
  def toPf(basePath: Path): PartialFunction[(Method, Path), Service[Request, Response]]
}


class Builder2[Base, A, B](base: Base, val ga: Gen[A], val gb: Gen[B]) extends Builder[Base] {
  def bindTo(fn: (A, B) => Service[Request, Response]) = new SR {
    override def toPf(basePath: Path) = {
      case actualMethod -> path / ga(s1) / gb(s2) => fn(s1, s2)
    }
  }
}

trait Contract {
  self =>
  type T

  def gens: Seq[Gen[_]]

}

case class Gen[T](t: T) {
  def get: T = t
  def unapply(str: String): Option[T] = ???
}

class Contract0() extends Contract {
  type T = () => Service[Request, Response]

  val gens = Nil

  def taking[A](a: Gen[A]): Contract1[A] = new Contract1(a)

  //  def at(): Builder0[T] = new Builder0(this)
}

class Contract1[A](a: Gen[A]) extends Contract {
  type T = (A) => Service[Request, Response]

  val gens = Seq(a)

  def taking[B](b: Gen[B]) = new Contract2(a, b)

  //  def at(): Builder0[T] = new Builder0(this)
}

class Contract2[A, B](a: Gen[A], b: Gen[B]) extends Contract {
  type T = (A, B) => Service[Request, Response]

  val gens = Seq(a, b)

  def aaa(req: Request) = (a: A, b: B) => Service.mk[Request, Response] { req => ??? }

  def at(): Builder0[Request => T] = new Builder0(aaa)
}


object Test extends App {
  private val builder = new Contract0().taking(Gen("string")).taking(Gen(123)).at() / Gen('a') / Gen(true)

}