package io.fintrospect

import com.twitter.finagle.Service
import com.twitter.finagle.http.Request
import com.twitter.finagle.http.path.{Path => FPath}
import com.twitter.util.Future
import io.fintrospect.ClientPath.Bob
import io.fintrospect.parameters.{Path, PathParameter}
import org.jboss.netty.handler.codec.http.{HttpRequest, HttpResponse}

object ClientPath {
  type Bob = (Service[HttpRequest, HttpResponse]) => Future[HttpResponse]
}

case class ClientPath[R](route: R, path: FPath) {
  def /(part: String): ClientPath[R] = ClientPath(route, path / part)

  def /[A](pp0: PathParameter[A]): ClientPath1[R, A] = ClientPath1(route, path, pp0)

  def apply(): Bob = _(Request(path.toString))
}

case class ClientPath1[R, A](route: R, path: FPath, pp0: PathParameter[A]) {
  def /(part: String): ClientPath2[R, A, String] = /(Path.fixed(part))

  def /[B](pp1: PathParameter[B]): ClientPath2[R, A, B] = ClientPath2(route, path, pp0, pp1)

  private def pathFor(a: A) = path / pp0(a)

  def apply(a: A): Bob = _(Request((path / pp0(a)).toString()))
}

case class ClientPath2[R, A, B](route: R, path: FPath,
                                pp0: PathParameter[A],
                                pp1: PathParameter[B]
                                 ) {
  def /(part: String): ClientPath3[R, A, B, String] = /(Path.fixed(part))

  def /[C](pp2: PathParameter[C]): ClientPath3[R, A, B, C] = ClientPath3(route, path, pp0, pp1, pp2)

  def apply(a: A, b: B): Bob = _(Request((path / pp0(a) / pp1(b)).toString()))
}

case class ClientPath3[R, A, B, C](route: R, path: FPath,
                                   pp0: PathParameter[A],
                                   pp1: PathParameter[B],
                                   pp2: PathParameter[C]
                                    ) {
  def /(part: String): ClientPath4[R, A, B, C, String] = /(Path.fixed(part))

  def /[D](pp3: PathParameter[D]): ClientPath4[R, A, B, C, D] = ClientPath4(route, path, pp0, pp1, pp2, pp3)

  def apply(a: A, b: B, c: C): Bob = _(Request((path / pp0(a) / pp1(b) / pp2(c)).toString()))
}

case class ClientPath4[R, A, B, C, D](route: R,
                                      path: FPath,
                                      pp0: PathParameter[A],
                                      pp1: PathParameter[B],
                                      pp2: PathParameter[C],
                                      pp3: PathParameter[D]
                                       ) {
  def apply(a: A, b: B, c: C, d: D): Bob = _(Request((path / pp0(a) / pp1(b) / pp2(c) / pp3(d)).toString()))
}
