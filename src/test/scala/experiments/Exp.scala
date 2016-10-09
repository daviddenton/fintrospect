package experiments

import com.twitter.finagle.Service
import com.twitter.finagle.http.path.{->, /, Path}
import com.twitter.finagle.http.{Method, Request, Response}
import com.twitter.util.Future
import experiments.types.PathParam
import io.fintrospect.RequestBuilder
import io.fintrospect.parameters.Binding

trait PathBuilder[RqParams, T <: Request => RqParams] {
  def buildRequest(method: Method, suppliedBindings: Seq[Binding]): Request = suppliedBindings
    .foldLeft(RequestBuilder(method)) { (requestBuild, next) => next(requestBuild) }.build()
}

case class PathBuilder0[In, Out](method: Method, terms: Terms, extract: Request => In, bind: Out => Seq[Binding]) extends PathBuilder[In, Request => In] {

  def /(next: String) = copy(terms = terms.addPath(next))

  def /[NEXT](next: PathParam[NEXT]) = PathBuilder1(method, terms, extract, next)

  def bindToClient(svc: Service[Request, Response]): Out => Future[Response] =
    in => svc(buildRequest(method, bind(in)))

  def bindTo(fn: (In, Request) => Future[Response]) = new NuServerRoute(method, identity) {
    override def toPf(inPath: Path) = {
      case actualMethod -> path if matches(actualMethod, inPath, path) => terms.useFilter.andThen(Service.mk[Request, Response] {
        (req: Request) => fn(extract(req), req)
      })
    }
  }
}

case class PathBuilder1[In, PP1](method: Method, terms: Terms, extract: Request => In,
                                   pp1: PathParam[PP1]) extends PathBuilder[In, Request => In] {

  def /(next: String) = copy(terms = terms.addPath(next))

  def /[NEXT](next: PathParam[NEXT]) = PathBuilder2(method, terms, extract, pp1, next)

  def bindTo(fn: (PP1, In, Request) => Future[Response]) = new NuServerRoute(method, identity, pp1) {
    override def toPf(inPath: Path) = {
      case actualMethod -> path / pp1(s1) if matches(actualMethod, inPath, path) => Service.mk[Request, Response] {
        (req: Request) => fn(s1, extract(req), req)
      }
    }
  }
}

case class PathBuilder2[In, PP1, PP2](method: Method, terms: Terms,
                                        extract: Request => In,
                                        pp1: PathParam[PP1], pp2: PathParam[PP2]) extends PathBuilder[In, Request => In] {

  def /(next: String) = copy(terms = terms.addPath(next))

  def /[NEXT](next: PathParam[NEXT]) = PathBuilder3(method, terms, extract, pp1, pp2, next)


  def bindTo(fn: (PP1, PP2, In, Request) => Future[Response]) = new NuServerRoute(method, identity, pp1, pp2) {
    override def toPf(inPath: Path) = {
      case actualMethod -> path / pp1(s1) / pp2(s2) if matches(actualMethod, inPath, path) => Service.mk[Request, Response] {
        (req: Request) => fn(s1, s2, extract(req), req)
      }
    }
  }
}

case class PathBuilder3[In, PP1, PP2, PP3](method: Method, terms: Terms,
                                             extract: Request => In,
                                             pp1: PathParam[PP1], pp2: PathParam[PP2], pp3: PathParam[PP3]) extends PathBuilder[In, Request => In] {
  def /(next: String) = copy(terms = terms.addPath(next))

  def /[NEXT](next: PathParam[NEXT]) = PathBuilder4(method, terms, extract, pp1, pp2, pp3, next)

  def bindTo(fn: (PP1, PP2, PP3, In, Request) => Future[Response]) = new NuServerRoute(method, identity, pp1, pp2, pp3) {
    override def toPf(inPath: Path) = {
      case actualMethod -> path / pp1(s1) / pp2(s2) / pp3(s3) if matches(actualMethod, inPath, path) => Service.mk[Request, Response] {
        (req: Request) => fn(s1, s2, s3, extract(req), req)
      }
    }
  }
}

case class PathBuilder4[In, PP1, PP2, PP3, PP4](method: Method, terms: Terms,
                                                  extract: Request => In,
                                                  pp1: PathParam[PP1], pp2: PathParam[PP2], pp3: PathParam[PP3], pp4: PathParam[PP4]) extends PathBuilder[In, Request => In] {
  def /(next: String) = copy(terms = terms.addPath(next))

  def /[NEXT](next: PathParam[NEXT]) = PathBuilder5(method, terms, extract, pp1, pp2, pp3, pp4, next)

  def bindTo(fn: (PP1, PP2, PP3, PP4, In, Request) => Future[Response]) = new NuServerRoute(method, identity, pp1, pp2, pp3, pp4) {
    override def toPf(inPath: Path) = {
      case actualMethod -> path / pp1(s1) / pp2(s2) / pp3(s3) / pp4(s4) if matches(actualMethod, inPath, path) => Service.mk[Request, Response] {
        (req: Request) => fn(s1, s2, s3, s4, extract(req), req)
      }
    }
  }
}

case class PathBuilder5[In, PP1, PP2, PP3, PP4, PP5](method: Method, terms: Terms,
                                                       extract: Request => In,
                                                       pp1: PathParam[PP1], pp2: PathParam[PP2], pp3: PathParam[PP3], pp4: PathParam[PP4], pp5: PathParam[PP5]) extends PathBuilder[In, Request => In] {

  def /(next: String) = copy(terms = terms.addPath(next))

  def /[NEXT](next: PathParam[NEXT]) = PathBuilder6(method, terms, extract, pp1, pp2, pp3, pp4, pp5, next)

  def bindTo(fn: (PP1, PP2, PP3, PP4, PP5, In, Request) => Future[Response]) = new NuServerRoute(method, identity, pp1, pp2, pp3, pp4, pp5) {
    override def toPf(inPath: Path) = {
      case actualMethod -> path / pp1(s1) / pp2(s2) / pp3(s3) / pp4(s4) / pp5(s5) if matches(actualMethod, inPath, path) => Service.mk[Request, Response] {
        (req: Request) => fn(s1, s2, s3, s4, s5, extract(req), req)
      }
    }
  }
}

case class PathBuilder6[In, PP1, PP2, PP3, PP4, PP5, PP6](method: Method, terms: Terms,
                                                            extract: Request => In,
                                                            pp1: PathParam[PP1], pp2: PathParam[PP2], pp3: PathParam[PP3], pp4: PathParam[PP4], pp5: PathParam[PP5], pp6: PathParam[PP6]) extends PathBuilder[In, Request => In] {
  def bindTo(fn: (PP1, PP2, PP3, PP4, PP5, PP6, In, Request) => Future[Response]) = new NuServerRoute(method, identity, pp1, pp2, pp3, pp4, pp5, pp6) {
    override def toPf(inPath: Path) = {
      case actualMethod -> path / pp1(s1) / pp2(s2) / pp3(s3) / pp4(s4) / pp5(s5) / pp6(s6) if matches(actualMethod, inPath, path) => Service.mk[Request, Response] {
        (req: Request) => fn(s1, s2, s3, s4, s5, s6, extract(req), req)
      }
    }
  }
}