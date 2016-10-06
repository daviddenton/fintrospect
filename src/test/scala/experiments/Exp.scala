package experiments

import com.twitter.finagle.http.Method.Get
import com.twitter.finagle.http.path.{->, /, Path}
import com.twitter.finagle.http.{Method, Request, Response}
import com.twitter.finagle.{Filter, Service}
import com.twitter.util.Future
import experiments.types.{Filt, PathParam, RqParam}
import io.fintrospect.parameters.{Binding, Parameter, PathBindable, PathParameter, Query, Rebindable, Retrieval, Path => FPath}
import io.fintrospect.util.Extractor

object types {
  type Filt = Filter[Request, Response, Request, Response]
  type PathParam[T] = PathParameter[T] with PathBindable[T]
  type RqParam[T] = Parameter with Retrieval[Request, T] with Extractor[Request, T] with Rebindable[Request, T, Binding]
}
trait BuilderWithParams[RqParams, T <: Request => RqParams] {
}

class BuilderWithParams0[Base](method: Method, filter: Filt, extract: Request => Base) extends BuilderWithParams[Base, Request => Base] {
  def /[A](a: PathParam[A]): BuilderWithParams1[Base, A] = new BuilderWithParams1(method, filter, extract, a)

  def bindTo(fn: Base => Future[Response]) = new SR {
    override def toPf(basePath: Path) = {
      case actualMethod -> path => filter.andThen(Service.mk[Request, Response] {
        (req: Request) => fn(extract(req))
      })
    }
  }
}

class BuilderWithParams1[Base, A](method: Method, filter: Filt, extract: Request => Base, ga: PathParameter[A]) extends BuilderWithParams[Base, Request => Base] {
  def /[B](b: PathParam[B]) = new BuilderWithParams2(method, filter, extract, ga, b)

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

class BuilderWithParams2[Base, A, B](method: Method, filter: Filt, extract: Request => Base, val ga: PathParameter[A], val gb: PathParameter[B]) extends BuilderWithParams[Base, Request => Base] {
  def bindTo(fn: (A, B, Base) => Future[Response]) = new SR {
    override def toPf(basePath: Path) = {
      case actualMethod -> path / ga(s1) / gb(s2) => Service.mk[Request, Response] {
        (req: Request) => fn(s1, s2, extract(req))
      }
    }
  }
}

trait Contract {
  self: Product =>
  type T

  def params: Seq[RqParam[_]]

}

case class Contract0(private val filter: Option[Filt] = None) extends Contract {
  type T = () => Service[Request, Response]

  val params = Nil

  def taking[A](a: RqParam[A]): Contract1[A] = Contract1(filter, a)

  def withFilter(filter: Filt) = copy(filter = Option(filter))

  def at(method: Method) = new BuilderWithParams0(method, filter.getOrElse(Filter.identity), identity)
}

case class Contract1[A](private val filter: Option[Filt], private val a: RqParam[A]) extends Contract {
  type T = (A) => Service[Request, Response]

  val params = Seq[RqParam[_]](a)

  def taking[B](b: RqParam[B]) = Contract2(filter, a, b)

  def withFilter(filter: Filt) = copy(filter = Option(filter))

  def at(method: Method) = new BuilderWithParams0(method, filter.getOrElse(Filter.identity), req => (a.from(req), req))
}

case class Contract2[A, B](private val filter: Option[Filt], private val a: RqParam[A], private val b: RqParam[B]) extends Contract {
  type T = (A, B) => Service[Request, Response]

  val params = Seq[RqParam[_]](a, b)

  def withFilter(filter: Filt) = copy(filter = Option(filter))

  def at(method: Method) = new BuilderWithParams0(method, filter.getOrElse(Filter.identity), req => (a.from(req), b.from(req), req))
}


object Exp extends App {
  private val onePathOneParam = Contract0().taking(Query.required.string("a")).at(Get) / FPath.string("a")

  def svc0(c: String, params: (String, Request)) = Future[Response] {
    ???
  }

  onePathOneParam.bindTo(svc0)

  private val pathAndParams = Contract0().taking(Query.required.string("a")).taking(Query.required.int("a")).at(Get) / FPath.string("a") / FPath.boolean("a")

  def svc(c: String, b: Boolean, params: (String, Int, Request)) = Future[Response] {
    val (str, int, req) = params
    ???
  }

  pathAndParams.bindTo(svc)

  private val pathOnly = Contract0().at(Get) / FPath.string("a") / FPath.boolean("a")

  def svc2(c: String, b: Boolean, req: Request) = Future[Response] {
    ???
  }

  pathOnly.bindTo(svc2)

  private val paramsOnly = Contract0()
    .withFilter(Filter.identity)
    .taking(Query.required.string("a")).taking(Query.required.int("a")).at(Get)

  def svc3(params: (String, Int, Request)) = Future[Response] {
    val (str, int, req) = params
    ???
  }

  paramsOnly.bindTo(svc3)

  private val nothing = Contract0().at(Get)

  def svc4(req: Request) = Future[Response] {
    ???
  }

  nothing.bindTo(svc4)

}