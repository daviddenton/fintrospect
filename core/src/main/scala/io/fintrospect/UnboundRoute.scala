package io.fintrospect

import com.twitter.finagle.http.path.{->, /, Path}
import com.twitter.finagle.http.{Method, Request, Response}
import com.twitter.finagle.{Filter, Service}
import io.fintrospect.ModuleSpec.ModifyPath
import io.fintrospect.UnboundRoute.{clientFor, proxyFor}
import io.fintrospect.parameters.{PathParameter, Path => Fp}

/**
  * An unbound route represents a RouteSpec which has been assigned a Method (and a partial or complete Path), but has
  * not yet been bound to a server or client Service.
  */
object UnboundRoute {
  def apply(routeSpec: RouteSpec, method: Method): UnboundRoute0 = new UnboundRoute0(routeSpec, method, identity)

  private[fintrospect] def clientFor(ip: UnboundRoute,
                                     service: Service[Request, Response],
                                     pp: PathParameter[_]*): RouteClient = {
    new RouteClient(ip.method, ip.routeSpec, Fp.fixed(ip.pathFn(Path("")).toString) +: pp, service)
  }

  private[fintrospect] def proxyFor[T](ip: UnboundRoute, service: Service[Request, Response]): Service[Request, Response] =
    Service.mk {
      request: Request => {
        val bindings = ip.routeSpec.body.map(b => b.rebind(request)).getOrElse(Nil) ++ ip.routeSpec.requestParams.flatMap(p => p.rebind(request))
        service(bindings.foldLeft(RequestBuilder(ip.method, request.path.split("/"))) { (builder, binding) => binding(builder) }.build())
      }
    }
}

trait UnboundRoute {
  type ModifyPath = Path => Path
  val routeSpec: RouteSpec
  val method: Method
  val pathFn: ModifyPath

  def bindToClient(service: Service[Request, Response]): RouteClient

  def bindToProxy(service: Service[Request, Response]): ServerRoute[Request, Response]
}

/**
  * An UnboundRoute with 0 path parts
  */
class UnboundRoute0(val routeSpec: RouteSpec, val method: Method, val pathFn: ModifyPath) extends UnboundRoute {
  def /(part: String) = new UnboundRoute0(routeSpec, method, pathFn = pathFn.andThen(_ / part))

  def /[T](pp0: PathParameter[T]) = new UnboundRoute1(routeSpec, method, pathFn, pp0)

  /**
    * Non-lazy bindTo(). Depending on use-case, can use this instead of the lazy version.
    */
  def bindTo[RQ, RS](svc: Service[RQ, RS]): ServerRoute[RQ, RS] = bindTo(() => svc)

  /**
    * Lazy bindTo(). Depending on use-case, can use this instead of the non-lazy version.
    */
  def bindTo[RQ, RS](fn: () => Service[RQ, RS]): ServerRoute[RQ, RS] = new ServerRoute[RQ, RS](routeSpec, method, pathFn) {
    override def toPf(filter: Filter[Request, Response, RQ, RS], basePath: Path) = {
      case actualMethod -> path if matches(actualMethod, basePath, path) => filter.andThen(fn())
    }
  }

  def bindToProxy(service: Service[Request, Response]): ServerRoute[Request, Response] = bindTo(proxyFor(this, service))

  override def bindToClient(service: Service[Request, Response]) = clientFor(this, service)
}

/**
  * An UnboundRoute with 1 path part
  */
class UnboundRoute1[A](val routeSpec: RouteSpec, val method: Method, val pathFn: ModifyPath,
                       pp1: PathParameter[A]) extends UnboundRoute {
  def /(part: String): UnboundRoute2[A, String] = /(Fp.fixed(part))

  def /[B](pp2: PathParameter[B]): UnboundRoute2[A, B] = new UnboundRoute2(routeSpec, method, pathFn, pp1, pp2)

  def bindTo[RQ, RS](fn: (A) => Service[RQ, RS]): ServerRoute[RQ, RS] = new ServerRoute[RQ, RS](routeSpec, method, pathFn, pp1) {
    override def toPf(filter: Filter[Request, Response, RQ, RS], basePath: Path) = {
      case actualMethod -> path / pp1(s1) if matches(actualMethod, basePath, path) => filter.andThen(fn(s1))
    }
  }

  override def bindToClient(service: Service[Request, Response]) = clientFor(this, service, pp1)

  override def bindToProxy(service: Service[Request, Response]): ServerRoute[Request, Response] = bindTo(_ => proxyFor(this, service))
}

/**
  * An UnboundRoute with 2 path parts
  */
class UnboundRoute2[A, B](val routeSpec: RouteSpec, val method: Method, val pathFn: ModifyPath,
                          pp1: PathParameter[A],
                          pp2: PathParameter[B]) extends UnboundRoute {
  def /(part: String): UnboundRoute3[A, B, String] = /(Fp.fixed(part))

  def /[C](pp3: PathParameter[C]): UnboundRoute3[A, B, C] = new UnboundRoute3(routeSpec, method, pathFn, pp1, pp2, pp3)

  def bindTo[RQ, RS](fn: (A, B) => Service[RQ, RS]): ServerRoute[RQ, RS] = new ServerRoute[RQ, RS](routeSpec, method, pathFn, pp1, pp2) {
    override def toPf(filter: Filter[Request, Response, RQ, RS], basePath: Path) = {
      case actualMethod -> path / pp1(s1) / pp2(s2) if matches(actualMethod, basePath, path) => filter.andThen(fn(s1, s2))
    }
  }

  override def bindToClient(service: Service[Request, Response]) = clientFor(this, service, pp1, pp2)

  override def bindToProxy(service: Service[Request, Response]): ServerRoute[Request, Response] = bindTo((_, _) => proxyFor(this, service))
}

/**
  * An UnboundRoute with 3 path parts
  */
class UnboundRoute3[A, B, C](val routeSpec: RouteSpec, val method: Method, val pathFn: ModifyPath,
                             pp1: PathParameter[A],
                             pp2: PathParameter[B],
                             pp3: PathParameter[C]) extends UnboundRoute {
  def /(part: String): UnboundRoute4[A, B, C, String] = /(Fp.fixed(part))

  def /[D](pp4: PathParameter[D]): UnboundRoute4[A, B, C, D] = new UnboundRoute4(routeSpec, method, pathFn, pp1, pp2, pp3, pp4)

  def bindTo[RQ, RS](fn: (A, B, C) => Service[RQ, RS]): ServerRoute[RQ, RS] = new ServerRoute[RQ, RS](routeSpec, method, pathFn, pp1, pp2, pp3) {
    override def toPf(filter: Filter[Request, Response, RQ, RS], basePath: Path) = {
      case actualMethod -> path / pp1(s1) / pp2(s2) / pp3(s3) if matches(actualMethod, basePath, path) => filter.andThen(fn(s1, s2, s3))
    }
  }

  override def bindToClient(service: Service[Request, Response]) = clientFor(this, service, pp1, pp2, pp3)

  override def bindToProxy(service: Service[Request, Response]): ServerRoute[Request, Response] = bindTo((_, _, _) => proxyFor(this, service))
}

/**
  * An UnboundRoute with 4 path parts
  */
class UnboundRoute4[A, B, C, D](val routeSpec: RouteSpec, val method: Method, val pathFn: ModifyPath,
                                pp1: PathParameter[A],
                                pp2: PathParameter[B],
                                pp3: PathParameter[C],
                                pp4: PathParameter[D]
                                 ) extends UnboundRoute {
  def /(part: String): UnboundRoute5[A, B, C, D, String] = /(Fp.fixed(part))

  def /[E](pp5: PathParameter[E]): UnboundRoute5[A, B, C, D, E] = new UnboundRoute5(routeSpec, method, pathFn, pp1, pp2, pp3, pp4, pp5)

  def bindTo[RQ, RS](fn: (A, B, C, D) => Service[RQ, RS]): ServerRoute[RQ, RS] = new ServerRoute[RQ, RS](routeSpec, method, pathFn, pp1, pp2, pp3, pp4) {
    override def toPf(filter: Filter[Request, Response, RQ, RS], basePath: Path) = {
      case actualMethod -> path / pp1(s1) / pp2(s2) / pp3(s3) / pp4(s4) if matches(actualMethod, basePath, path) => filter.andThen(fn(s1, s2, s3, s4))
    }
  }

  override def bindToClient(service: Service[Request, Response]) = clientFor(this, service, pp1, pp2, pp3, pp4)

  override def bindToProxy(service: Service[Request, Response]): ServerRoute[Request, Response] = bindTo((_, _, _, _) => proxyFor(this, service))
}

/**
  * An UnboundRoute with 5 path parts
  */
class UnboundRoute5[A, B, C, D, E](val routeSpec: RouteSpec, val method: Method, val pathFn: ModifyPath,
                                   pp1: PathParameter[A],
                                   pp2: PathParameter[B],
                                   pp3: PathParameter[C],
                                   pp4: PathParameter[D],
                                   pp5: PathParameter[E]
                                    ) extends UnboundRoute {
  def /(part: String): UnboundRoute6[A, B, C, D, E, String] = /(Fp.fixed(part))

  def /[F](pp6: PathParameter[F]): UnboundRoute6[A, B, C, D, E, F] = new UnboundRoute6(routeSpec, method, pathFn, pp1, pp2, pp3, pp4, pp5, pp6)

  def bindTo[RQ, RS](fn: (A, B, C, D, E) => Service[RQ, RS]): ServerRoute[RQ, RS] = new ServerRoute[RQ, RS](routeSpec, method, pathFn, pp1, pp2, pp3, pp4, pp5) {
    override def toPf(filter: Filter[Request, Response, RQ, RS], basePath: Path) = {
      case actualMethod -> path / pp1(s1) / pp2(s2) / pp3(s3) / pp4(s4) / pp5(s5) if matches(actualMethod, basePath, path) => filter.andThen(fn(s1, s2, s3, s4, s5))
    }
  }

  override def bindToClient(service: Service[Request, Response]) = clientFor(this, service, pp1, pp2, pp3, pp4, pp5)

  override def bindToProxy(service: Service[Request, Response]): ServerRoute[Request, Response] = bindTo((_, _, _, _, _) => proxyFor(this, service))
}

/**
  * An UnboundRoute with 6 path parts
  */
class UnboundRoute6[A, B, C, D, E, F](val routeSpec: RouteSpec, val method: Method, val pathFn: ModifyPath,
                                      pp1: PathParameter[A],
                                      pp2: PathParameter[B],
                                      pp3: PathParameter[C],
                                      pp4: PathParameter[D],
                                      pp5: PathParameter[E],
                                      pp6: PathParameter[F]
                                       ) extends UnboundRoute {
  def /(part: String): UnboundRoute7[A, B, C, D, E, F, String] = /(Fp.fixed(part))

  def /[G](pp7: PathParameter[G]): UnboundRoute7[A, B, C, D, E, F, G] = new UnboundRoute7(routeSpec, method, pathFn, pp1, pp2, pp3, pp4, pp5, pp6, pp7)

  def bindTo[RQ, RS](fn: (A, B, C, D, E, F) => Service[RQ, RS]): ServerRoute[RQ, RS] = new ServerRoute[RQ, RS](routeSpec, method, pathFn, pp1, pp2, pp3, pp4, pp5, pp6) {
    override def toPf(filter: Filter[Request, Response, RQ, RS], basePath: Path) = {
      case actualMethod -> path / pp1(s1) / pp2(s2) / pp3(s3) / pp4(s4) / pp5(s5) / pp6(s6) if matches(actualMethod, basePath, path) => filter.andThen(fn(s1, s2, s3, s4, s5, s6))
    }
  }

  override def bindToClient(service: Service[Request, Response]) = clientFor(this, service, pp1, pp2, pp3, pp4, pp5, pp6)

  override def bindToProxy(service: Service[Request, Response]): ServerRoute[Request, Response] = bindTo((_, _, _, _, _, _) => proxyFor(this, service))
}

/**
  * An UnboundRoute with 7 path parts
  */
class UnboundRoute7[A, B, C, D, E, F, G](val routeSpec: RouteSpec, val method: Method, val pathFn: ModifyPath,
                                         pp1: PathParameter[A],
                                         pp2: PathParameter[B],
                                         pp3: PathParameter[C],
                                         pp4: PathParameter[D],
                                         pp5: PathParameter[E],
                                         pp6: PathParameter[F],
                                         pp7: PathParameter[G]
                                          ) extends UnboundRoute {
  def bindTo[RQ, RS](fn: (A, B, C, D, E, F, G) => Service[RQ, RS]): ServerRoute[RQ, RS] = new ServerRoute[RQ, RS](routeSpec, method, pathFn, pp1, pp2, pp3, pp4, pp5, pp6, pp7) {
    override def toPf(filter: Filter[Request, Response, RQ, RS], basePath: Path) = {
      case actualMethod -> path / pp1(s1) / pp2(s2) / pp3(s3) / pp4(s4) / pp5(s5) / pp6(s6) / pp7(s7) if matches(actualMethod, basePath, path) => filter.andThen(fn(s1, s2, s3, s4, s5, s6, s7))
    }
  }

  override def bindToClient(service: Service[Request, Response]) = clientFor(this, service, pp1, pp2, pp3, pp4, pp5, pp6, pp7)

  override def bindToProxy(service: Service[Request, Response]): ServerRoute[Request, Response] = bindTo((_, _, _, _, _, _, _) => proxyFor(this, service))
}
