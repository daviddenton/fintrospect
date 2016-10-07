package experiments

import com.twitter.finagle.Service
import com.twitter.finagle.http.path.{->, /, Path}
import com.twitter.finagle.http.{Method, Request, Response}
import com.twitter.util.Future
import experiments.types.PathParam
import io.fintrospect.parameters.PathParameter

trait PathBuilder[RqParams, T <: Request => RqParams] {
}

abstract class SR(method: Method, pathFn: Path => Path) {
  def toPf(basePath: Path): PartialFunction[(Method, Path), Service[Request, Response]]

  def matches(actualMethod: Method, basePath: Path, actualPath: Path) = actualMethod == method && actualPath == pathFn(basePath)
}

case class PathBuilder0[Base](method: Method, terms: Terms, extract: Request => Base) extends PathBuilder[Base, Request => Base] {

  def /(next: String) = copy(terms = terms.addPath(next))

  def /[NEXT](next: PathParam[NEXT]) = PathBuilder1(method, terms, extract, next)

  def bindTo(fn: Base => Future[Response]) = new SR(method, identity) {
    override def toPf(basePath: Path) = {
      case actualMethod -> path if matches(actualMethod, basePath, path) => terms.useFilter.andThen(Service.mk[Request, Response] {
        (req: Request) => fn(extract(req))
      })
    }
  }
}

case class PathBuilder1[Base, PP0](method: Method, terms: Terms, extract: Request => Base, pp0: PathParameter[PP0]) extends PathBuilder[Base, Request => Base] {

  def /(next: String) = copy(terms = terms.addPath(next))

  def /[NEXT](next: PathParam[NEXT]) = PathBuilder2(method, terms, extract, pp0, next)

  def bindTo(fn: (PP0, Base) => Future[Response]) = new SR(method, identity) {
    override def toPf(basePath: Path) = {
      case actualMethod -> path / pp0(s1) if matches(actualMethod, basePath, path) => Service.mk[Request, Response] {
        (req: Request) => fn(s1, extract(req))
      }
    }
  }
}

case class PathBuilder2[Base, PP0, PP1](method: Method, terms: Terms, extract: Request => Base, pp0: PathParameter[PP0], pp1: PathParameter[PP1]) extends PathBuilder[Base, Request => Base] {
  def bindTo(fn: (PP0, PP1, Base) => Future[Response]) = new SR(method, identity) {
    override def toPf(basePath: Path) = {
      case actualMethod -> path / pp0(s1) / pp1(s2) if matches(actualMethod, basePath, path) => Service.mk[Request, Response] {
        (req: Request) => fn(s1, s2, extract(req))
      }
    }
  }
}