package io.github.daviddenton.fintrospect

import com.twitter.finagle.http.path.Path
import io.github.daviddenton.fintrospect.PathWrapper.PP
import io.github.daviddenton.fintrospect.parameters.PathParameter

object PathWrapper {
  type PP[A] = PathParameter[A]
}

case class PathWrapper0(rootPath: Path) {
  def /(part: String) = copy(rootPath / part)

  def /[T](pp0: PP[T]): PathWrapper1[T] = PathWrapper1(rootPath, pp0)
}

case class PathWrapper1[A](rootPath: Path,
                           pp0: PathParameter[A]) {
  def /[B](pp1: PP[B]) = PathWrapper2(rootPath, pp0, pp1)
}

case class PathWrapper2[A, B](rootPath: Path,
                              pp0: PathParameter[A],
                              pp1: PathParameter[B]) {
  def /[C](pp2: PP[C]) = PathWrapper3(rootPath, pp0, pp1, pp2)
}

case class PathWrapper3[A, B, C](rootPath: Path,
                                 pp0: PathParameter[A],

                                 pp1: PathParameter[B], pp2: PathParameter[C]) {
  def /[D](pp3: PP[D]) = PathWrapper4(rootPath, pp0, pp1, pp2, pp3)
}

case class PathWrapper4[A, B, C, D](rootPath: Path,
                                    pp0: PathParameter[A],
                                    pp1: PathParameter[B],
                                    pp2: PathParameter[C],
                                    pp3: PathParameter[D]
                                     ) {
  def /[E](pp4: PP[E]) = PathWrapper5(rootPath, pp0, pp1, pp2, pp3, pp4)
}

case class PathWrapper5[A, B, C, D, E](rootPath: Path,
                                       pp0: PathParameter[A],
                                       pp1: PathParameter[B],
                                       pp2: PathParameter[C],
                                       pp3: PathParameter[D],
                                       pp4: PathParameter[E]
                                        ) {
}
