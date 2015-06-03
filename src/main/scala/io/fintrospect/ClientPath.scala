package io.fintrospect

import com.twitter.finagle.http.path.{Path => FPath}
import io.fintrospect.parameters.{Path, PathParameter}

case class ClientPath[R](route: R, pathFn: FPath => FPath) {
  def /(part: String): ClientPath[R] = ClientPath(route, pathFn.andThen(p => p / part))

  def /[A](pp0: PathParameter[A]) = ClientPath1(route, pathFn, pp0)

  def toService = route
}

case class ClientPath1[R, A](route: R, pathFn: FPath => FPath, pp0: PathParameter[A]) {
  def /(part: String) = /(Path.fixed(part))

  def /[B](pp1: PathParameter[B]) = ClientPath2(route, pathFn, pp0, pp1)

  def toService = {

    route
  }
}

case class ClientPath2[R, A, B](route: R, pathFn: FPath => FPath,
                                pp0: PathParameter[A],
                                pp1: PathParameter[B]
                                 ) {
  def /(part: String) = /(Path.fixed(part))

  def /[C](pp2: PathParameter[C]) = ClientPath3(route, pathFn, pp0, pp1, pp2)

  def toService = ???
}

case class ClientPath3[R, A, B, C](route: R, pathFn: FPath => FPath,
                                   pp0: PathParameter[A],
                                   pp1: PathParameter[B],
                                   pp2: PathParameter[C]
                                    ) {
  def /(part: String) = /(Path.fixed(part))

  def /[D](pp3: PathParameter[D]) = ClientPath4(route, pathFn, pp0, pp1, pp2, pp3)

  def toService = ???
}

case class ClientPath4[R, A, B, C, D](route: R,
                                      pathFn: FPath => FPath,
                                      pp0: PathParameter[A],
                                      pp1: PathParameter[B],
                                      pp2: PathParameter[C],
                                      pp3: PathParameter[D]
                                       ) {
  def toService = ???
}
