package experiments

import com.twitter.finagle.Service
import com.twitter.finagle.http.path.{->, /, Path}
import com.twitter.finagle.http.{Method, Request, Response}
import com.twitter.util.Future
import experiments.types.PathParam
import io.fintrospect.parameters.PathParameter

trait PathBuilder[RqParams, T <: Request => RqParams] {
}

case class PathBuilder0[Base](method: Method, terms: Terms, extract: Request => Base) extends PathBuilder[Base, Request => Base] {

  def /(next: String) = copy(terms = terms.addPath(next))

  def /[NEXT](next: PathParam[NEXT]) = PathBuilder1(method, terms, extract, next)

  def bindTo(fn: Base => Future[Response]) = new NuServerRoute(method, identity) {
    override def toPf(basePath: Path) = {
      case actualMethod -> path if matches(actualMethod, basePath, path) => terms.useFilter.andThen(Service.mk[Request, Response] {
        (req: Request) => fn(extract(req))
      })
    }
  }
}

case class PathBuilder1[Base, PP1](method: Method, terms: Terms, extract: Request => Base,
                                   pp1: PathParameter[PP1]) extends PathBuilder[Base, Request => Base] {

  def /(next: String) = copy(terms = terms.addPath(next))

  def /[NEXT](next: PathParam[NEXT]) = PathBuilder2(method, terms, extract, pp1, next)

  def bindTo(fn: (PP1, Base) => Future[Response]) = new NuServerRoute(method, identity, pp1) {
    override def toPf(basePath: Path) = {
      case actualMethod -> path / pp1(s1) if matches(actualMethod, basePath, path) => Service.mk[Request, Response] {
        (req: Request) => fn(s1, extract(req))
      }
    }
  }
}

case class PathBuilder2[Base, PP1, PP2](method: Method, terms: Terms, extract: Request => Base,
                                        pp1: PathParameter[PP1], pp2: PathParameter[PP2]) extends PathBuilder[Base, Request => Base] {

  def /(next: String) = copy(terms = terms.addPath(next))

  def /[NEXT](next: PathParam[NEXT]) = PathBuilder3(method, terms, extract, pp1, pp2, next)

  def bindTo(fn: (PP1, PP2, Base) => Future[Response]) = new NuServerRoute(method, identity, pp1, pp2) {
    override def toPf(basePath: Path) = {
      case actualMethod -> path / pp1(s1) / pp2(s2) if matches(actualMethod, basePath, path) => Service.mk[Request, Response] {
        (req: Request) => fn(s1, s2, extract(req))
      }
    }
  }
}

case class PathBuilder3[Base, PP1, PP2, PP3](method: Method, terms: Terms, extract: Request => Base,
                                             pp1: PathParameter[PP1], pp2: PathParameter[PP2], pp3: PathParameter[PP3]) extends PathBuilder[Base, Request => Base] {
  def /(next: String) = copy(terms = terms.addPath(next))

  def /[NEXT](next: PathParam[NEXT]) = PathBuilder4(method, terms, extract, pp1, pp2, pp3, next)

  def bindTo(fn: (PP1, PP2, PP3, Base) => Future[Response]) = new NuServerRoute(method, identity, pp1, pp2, pp3) {
    override def toPf(basePath: Path) = {
      case actualMethod -> path / pp1(s1) / pp2(s2) / pp3(s3) if matches(actualMethod, basePath, path) => Service.mk[Request, Response] {
        (req: Request) => fn(s1, s2, s3, extract(req))
      }
    }
  }
}

case class PathBuilder4[Base, PP1, PP2, PP3, PP4](method: Method, terms: Terms, extract: Request => Base,
                                                  pp1: PathParameter[PP1], pp2: PathParameter[PP2], pp3: PathParameter[PP3], pp4: PathParameter[PP4]) extends PathBuilder[Base, Request => Base] {
  def /(next: String) = copy(terms = terms.addPath(next))

  def /[NEXT](next: PathParam[NEXT]) = PathBuilder5(method, terms, extract, pp1, pp2, pp3, pp4, next)

  def bindTo(fn: (PP1, PP2, PP3, PP4, Base) => Future[Response]) = new NuServerRoute(method, identity, pp1, pp2, pp3, pp4) {
    override def toPf(basePath: Path) = {
      case actualMethod -> path / pp1(s1) / pp2(s2) / pp3(s3) / pp4(s4) if matches(actualMethod, basePath, path) => Service.mk[Request, Response] {
        (req: Request) => fn(s1, s2, s3, s4, extract(req))
      }
    }
  }
}

case class PathBuilder5[Base, PP1, PP2, PP3, PP4, PP5](method: Method, terms: Terms, extract: Request => Base,
                                                       pp1: PathParameter[PP1], pp2: PathParameter[PP2], pp3: PathParameter[PP3], pp4: PathParameter[PP4], pp5: PathParameter[PP5]) extends PathBuilder[Base, Request => Base] {

  def /(next: String) = copy(terms = terms.addPath(next))

  def /[NEXT](next: PathParam[NEXT]) = PathBuilder6(method, terms, extract, pp1, pp2, pp3, pp4, pp5, next)

  def bindTo(fn: (PP1, PP2, PP3, PP4, PP5, Base) => Future[Response]) = new NuServerRoute(method, identity, pp1, pp2, pp3, pp4, pp5) {
    override def toPf(basePath: Path) = {
      case actualMethod -> path / pp1(s1) / pp2(s2) / pp3(s3) / pp4(s4) / pp5(s5) if matches(actualMethod, basePath, path) => Service.mk[Request, Response] {
        (req: Request) => fn(s1, s2, s3, s4, s5, extract(req))
      }
    }
  }
}

case class PathBuilder6[Base, PP1, PP2, PP3, PP4, PP5, PP6](method: Method, terms: Terms, extract: Request => Base,
                                                            pp1: PathParameter[PP1], pp2: PathParameter[PP2], pp3: PathParameter[PP3], pp4: PathParameter[PP4], pp5: PathParameter[PP5], pp6: PathParameter[PP6]) extends PathBuilder[Base, Request => Base] {
  def bindTo(fn: (PP1, PP2, PP3, PP4, PP5, PP6, Base) => Future[Response]) = new NuServerRoute(method, identity, pp1, pp2, pp3, pp4, pp5, pp6) {
    override def toPf(basePath: Path) = {
      case actualMethod -> path / pp1(s1) / pp2(s2) / pp3(s3) / pp4(s4) / pp5(s5) / pp6(s6) if matches(actualMethod, basePath, path) => Service.mk[Request, Response] {
        (req: Request) => fn(s1, s2, s3, s4, s5, s6, extract(req))
      }
    }
  }
}