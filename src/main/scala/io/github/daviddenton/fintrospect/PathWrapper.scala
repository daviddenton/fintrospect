package io.github.daviddenton.fintrospect

import com.twitter.finagle.http.path.{->, /, Path}
import io.github.daviddenton.fintrospect.FintrospectModule2.{FF, Svc}
import io.github.daviddenton.fintrospect.PathWrapper.PP
import io.github.daviddenton.fintrospect.parameters.{Path => Fp, PathParameter}
import org.jboss.netty.handler.codec.http.HttpMethod

object PathWrapper {
  def apply(method: HttpMethod): PathWrapper0 = PathWrapper0(method, p => p)

  type PP[A] = PathParameter[A]
}

trait PathWrapper extends Iterable[PP[_]] {

  val method: HttpMethod
  val completePath: Path => Path

  def matches(actualMethod: HttpMethod, basePath: Path, actualPath: Path) = actualMethod == method && actualPath == completePath(basePath)

  def toPf(basePath: Path, fn: () => Svc): FintrospectModule2.FF => PartialFunction[(HttpMethod, Path), FintrospectModule2.Svc]
}

case class PathWrapper0(method: HttpMethod, completePath: Path => Path) extends PathWrapper {
  def /(part: String) = copy(method, completePath.andThen(_ / part))

  def /[T](pp0: PP[T]) = PathWrapper1(method, completePath, pp0)

  override def iterator: Iterator[PP[_]] = Nil.iterator

  override def toPf(basePath: Path, fn: () => Svc): (FF) => PartialFunction[(HttpMethod, Path), Svc] = {
    filtered: FF => {
      case actualMethod -> path if matches(actualMethod, basePath, path) => filtered.andThen(fn())
    }
  }
}

case class PathWrapper1[A](method: HttpMethod, completePath: Path => Path,
                           pp1: PathParameter[A]) extends PathWrapper {
  def /(part: String): PathWrapper2[A, String] = /(Fp.fixed(part))
  def /[B](pp2: PP[B]): PathWrapper2[A, B] = PathWrapper2(method, completePath, pp1, pp2)

  override def iterator: Iterator[PP[_]] = Seq(pp1).iterator

  override def toPf(basePath: Path, fn: () => Svc): (FF) => PartialFunction[(HttpMethod, Path), Svc] = {
    filtered: FF => {
      case actualMethod -> path / pp1(s1) if matches(actualMethod, basePath, path) => filtered.andThen(fn())
    }
  }
}

case class PathWrapper2[A, B](method: HttpMethod, completePath: Path => Path,
                              pp1: PathParameter[A],
                              pp2: PathParameter[B]) extends PathWrapper {
  def /(part: String): PathWrapper3[A, B, String] = /(Fp.fixed(part))
  def /[C](pp3: PP[C]): PathWrapper3[A, B, C] = PathWrapper3(method, completePath, pp1, pp2, pp3)

  override def iterator: Iterator[PP[_]] = Seq(pp1, pp2).iterator

  override def toPf(basePath: Path, fn: () => Svc): (FF) => PartialFunction[(HttpMethod, Path), Svc] = {
    filtered: FF => {
      case actualMethod -> path / pp1(s1) / pp2(s2) if matches(actualMethod, basePath, path) => filtered.andThen(fn())
    }
  }
}

case class PathWrapper3[A, B, C](method: HttpMethod, completePath: Path => Path,
                                 pp1: PathParameter[A],
                                 pp2: PathParameter[B],
                                 pp3: PathParameter[C]) extends PathWrapper {
  def /(part: String): PathWrapper4[A, B, C, String] = /(Fp.fixed(part))
  def /[D](pp4: PP[D]): PathWrapper4[A, B, C, D] = PathWrapper4(method, completePath, pp1, pp2, pp3, pp4)

  override def iterator: Iterator[PP[_]] = Seq(pp1, pp2, pp3).iterator

  override def toPf(basePath: Path, fn: () => Svc): (FF) => PartialFunction[(HttpMethod, Path), Svc] = {
    filtered: FF => {
      case actualMethod -> path / pp1(s1) / pp2(s2) / pp3(s3) if matches(actualMethod, basePath, path) => filtered.andThen(fn())
    }
  }
}

case class PathWrapper4[A, B, C, D](method: HttpMethod, completePath: Path => Path,
                                    pp1: PathParameter[A],
                                    pp2: PathParameter[B],
                                    pp3: PathParameter[C],
                                    pp4: PathParameter[D]
                                     ) extends PathWrapper {
  def /(part: String): PathWrapper5[A, B, C, D, String] = /(Fp.fixed(part))
  def /[E](pp5: PP[E]): PathWrapper5[A, B, C, D, E] = PathWrapper5(method, completePath, pp1, pp2, pp3, pp4, pp5)

  override def iterator: Iterator[PP[_]] = Seq(pp1, pp2, pp3, pp4).iterator

  override def toPf(basePath: Path, fn: () => Svc): (FF) => PartialFunction[(HttpMethod, Path), Svc] = {
    filtered: FF => {
      case actualMethod -> path / pp1(s1) / pp2(s2) / pp3(s3) / pp4(s4) if matches(actualMethod, basePath, path) => filtered.andThen(fn())
    }
  }
}

case class PathWrapper5[A, B, C, D, E](method: HttpMethod, completePath: Path => Path,
                                       pp1: PathParameter[A],
                                       pp2: PathParameter[B],
                                       pp3: PathParameter[C],
                                       pp4: PathParameter[D],
                                       pp5: PathParameter[E]
                                        ) extends PathWrapper {
  def /[F](pp5: PP[F]) = throw new UnsupportedOperationException("Limit on number of elements!")

  override def iterator: Iterator[PP[_]] = Seq(pp1, pp2, pp3, pp5).iterator

  override def toPf(basePath: Path, fn: () => Svc): (FF) => PartialFunction[(HttpMethod, Path), Svc] = {
    filtered: FF => {
      case actualMethod -> path / pp1(s1) / pp2(s2) / pp3(s3) / pp4(s4) / pp5(s5) if matches(actualMethod, basePath, path) => filtered.andThen(fn())
    }
  }
}
