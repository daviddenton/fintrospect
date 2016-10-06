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
  def /[NEXT](next: PathParam[NEXT]): BuilderWithParams1[Base, NEXT] = new BuilderWithParams1(method, filter, extract, next)

  def bindTo(fn: Base => Future[Response]) = new SR {
    override def toPf(basePath: Path) = {
      case actualMethod -> path => filter.andThen(Service.mk[Request, Response] {
        (req: Request) => fn(extract(req))
      })
    }
  }
}

class BuilderWithParams1[Base, PP0](method: Method, filter: Filt, extract: Request => Base, pp0: PathParameter[PP0]) extends BuilderWithParams[Base, Request => Base] {
  def /[NEXT](next: PathParam[NEXT]) = new BuilderWithParams2(method, filter, extract, pp0, next)

  def bindTo(fn: (PP0, Base) => Future[Response]) = new SR {
    override def toPf(basePath: Path) = {
      case actualMethod -> path / pp0(s1) => Service.mk[Request, Response] {
        (req: Request) => fn(s1, extract(req))
      }
    }
  }
}

trait SR {
  def toPf(basePath: Path): PartialFunction[(Method, Path), Service[Request, Response]]
}

class BuilderWithParams2[Base, PP0, PP1](method: Method, filter: Filt, extract: Request => Base, pp0: PathParameter[PP0], pp1: PathParameter[PP1]) extends BuilderWithParams[Base, Request => Base] {
  def bindTo(fn: (PP0, PP1, Base) => Future[Response]) = new SR {
    override def toPf(basePath: Path) = {
      case actualMethod -> path / pp0(s1) / pp1(s2) => Service.mk[Request, Response] {
        (req: Request) => fn(s1, s2, extract(req))
      }
    }
  }
}

trait Contract {
  def params: Seq[RqParam[_]]
}

case class Contract0(private val filter: Option[Filt] = None) extends Contract {
  val params = Nil

  def taking[NEXT](next: RqParam[NEXT]): Contract1[NEXT] = Contract1(filter, next)

  def withFilter(filter: Filt) = copy(filter = Option(filter))

  def at(method: Method) = new BuilderWithParams0(method, filter.getOrElse(Filter.identity), identity)
}

case class Contract1[RP0](private val filter: Option[Filt], private val rp0: RqParam[RP0]) extends Contract {
  val params = Seq[RqParam[_]](rp0)

  def taking[NEXT](next: RqParam[NEXT]) = Contract2(filter, rp0, next)

  def withFilter(filter: Filt) = copy(filter = Option(filter))

  def at(method: Method) = new BuilderWithParams0(method, filter.getOrElse(Filter.identity), req => (rp0.from(req), req))
}

case class Contract2[RP0, RP1](private val filter: Option[Filt], private val rp0: RqParam[RP0], private val rp1: RqParam[RP1]) extends Contract {
  val params = Seq[RqParam[_]](rp0, rp1)

  def withFilter(filter: Filt) = copy(filter = Option(filter))

  def at(method: Method) = new BuilderWithParams0(method, filter.getOrElse(Filter.identity), req => (rp0.from(req), rp1.from(req), req))
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