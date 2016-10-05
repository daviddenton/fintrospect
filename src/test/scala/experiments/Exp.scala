package experiments

import com.twitter.finagle.Service
import com.twitter.finagle.http.path.{->, /, Path}
import com.twitter.finagle.http.{Method, Request, Response}
import com.twitter.util.Future

trait BuilderWithParams[RqParams <: Product, T <: Request => RqParams] {
}

class BuilderWithParams0[Base <: Product](extract: Request => Base) extends BuilderWithParams[Base, Request => Base] {
  def /[A](a: Gen[A]): BuilderWithParams1[Base, A] = new BuilderWithParams1(extract, a)

  def bindTo(fn: Base => Future[Response]) = new SR {
    override def toPf(basePath: Path) = {
      case actualMethod -> path => Service.mk[Request, Response] {
        (req: Request) => fn(extract(req))
      }
    }
  }
}

class BuilderWithParams1[Base <: Product, A](extract: Request => Base, ga: Gen[A]) extends BuilderWithParams[Base, Request => Base] {
  def /[B](b: Gen[B]) = new BuilderWithParams2(extract, ga, b)

  def bindTo(fn: (A, Base) => Future[Response]) = new SR {
    override def toPf(basePath: Path) = {
      case actualMethod -> path / ga(s1) => Service.mk[Request, Response] {
        (req: Request) => fn(s1, extract(req))
      }
    }
  }
}

trait SR {
  def toPf(basePath: Path): PartialFunction[(Method, Path), Service[Request, Response]]
}

class BuilderWithParams2[Base <: Product, A, B](extract: Request => Base, val ga: Gen[A], val gb: Gen[B]) extends BuilderWithParams[Base, Request => Base] {
  def bindTo(fn: (A, B, Base) => Future[Response]) = new SR {
    override def toPf(basePath: Path) = {
      case actualMethod -> path / ga(s1) / gb(s2) => Service.mk[Request, Response] {
        (req: Request) => fn(s1, s2, extract(req))
      }
    }
  }
}

trait Contract {
  self =>
  type T

  def gens: Seq[Gen[_]]

}

case class Gen[T](t: T) {

  def from(req: Request): T = ???

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

  private def aaa(req: Request): (A, B) = (a.from(req), b.from(req))

  def at(): BuilderWithParams0[(A, B)] = new BuilderWithParams0(aaa)
}


object Test extends App {
  private val builder = new Contract0().taking(Gen("string")).taking(Gen(123)).at() / Gen('a') / Gen(true)

  def svc(c: Char, b: Boolean, params: (String, Int)) = Future[Response] {
    val (str, int) = params
    ???
  }

  builder.bindTo(svc)

}