package io.github.daviddenton.fintrospect

import com.twitter.finagle.http.path.{->, /, Path}
import io.github.daviddenton.fintrospect.FintrospectModule2.{FF, Svc}
import io.github.daviddenton.fintrospect.PathWrapper.PP
import io.github.daviddenton.fintrospect.parameters.{Path => Fp, PathParameter}
import org.jboss.netty.handler.codec.http.HttpMethod

object PathWrapper {
  def apply(method: HttpMethod): PathWrapper0 = PathWrapper0(method, identity)

  type PP[A] = PathParameter[A]
}

abstract class CompletePath(wrapper: PathWrapper, val params: PP[_]*) {
  def toPf(basePath: Path): (FF) => PartialFunction[(HttpMethod, Path), Svc]

  def describeFor(basePath: Path): String = (wrapper.completePath(basePath).toString :: params.map(_.toString()).toList).mkString("/")
}

trait PathWrapper {
  val method: HttpMethod
  val completePath: Path => Path
  def matches(actualMethod: HttpMethod, basePath: Path, actualPath: Path) = actualMethod == method && actualPath == completePath(basePath)
}

case class PathWrapper0(method: HttpMethod, completePath: Path => Path) extends PathWrapper {
  def /(part: String) = copy(method, completePath.andThen(_ / part))

  def /[T](pp0: PP[T]) = PathWrapper1(method, completePath, pp0)

  def then(fn: () => Svc): CompletePath = new CompletePath(this) {
    override def toPf(basePath: Path): (FF) => PartialFunction[(HttpMethod, Path), Svc] = {
      filtered: FF => {
        case actualMethod -> path if matches(actualMethod, basePath, path) => filtered.andThen(fn())
      }
    }
  }
}

case class PathWrapper1[A](method: HttpMethod, completePath: Path => Path,
                           pp1: PathParameter[A]) extends PathWrapper {
  def /(part: String): PathWrapper2[A, String] = /(Fp.fixed(part))

  def /[B](pp2: PP[B]): PathWrapper2[A, B] = PathWrapper2(method, completePath, pp1, pp2)

  def then(fn: (A) => Svc): CompletePath = new CompletePath(this, pp1) {
    override def toPf(basePath: Path): (FF) => PartialFunction[(HttpMethod, Path), Svc] = {
      filtered: FF => {
        case actualMethod -> path / pp1(s1) if matches(actualMethod, basePath, path) => filtered.andThen(fn(s1))
      }
    }
  }
}

case class PathWrapper2[A, B](method: HttpMethod, completePath: Path => Path,
                              pp1: PathParameter[A],
                              pp2: PathParameter[B]) extends PathWrapper {
  def /(part: String): PathWrapper3[A, B, String] = /(Fp.fixed(part))

  def /[C](pp3: PP[C]): PathWrapper3[A, B, C] = PathWrapper3(method, completePath, pp1, pp2, pp3)

  def then(fn: (A, B) => Svc): CompletePath = new CompletePath(this, pp1, pp2) {
    override def toPf(basePath: Path): (FF) => PartialFunction[(HttpMethod, Path), Svc] = {
      filtered: FF => {
        case actualMethod -> path / pp1(s1) / pp2(s2) if matches(actualMethod, basePath, path) => filtered.andThen(fn(s1, s2))
      }
    }
  }
}

case class PathWrapper3[A, B, C](method: HttpMethod, completePath: Path => Path,
                                 pp1: PathParameter[A],
                                 pp2: PathParameter[B],
                                 pp3: PathParameter[C]) extends PathWrapper {
  def /(part: String): PathWrapper4[A, B, C, String] = /(Fp.fixed(part))

  def /[D](pp4: PP[D]): PathWrapper4[A, B, C, D] = PathWrapper4(method, completePath, pp1, pp2, pp3, pp4)

  def then(fn: (A, B, C) => Svc): CompletePath = new CompletePath(this, pp1, pp2, pp3) {
    override def toPf(basePath: Path): (FF) => PartialFunction[(HttpMethod, Path), Svc] = {
      filtered: FF => {
        case actualMethod -> path / pp1(s1) / pp2(s2) / pp3(s3) if matches(actualMethod, basePath, path) => filtered.andThen(fn(s1, s2, s3))
      }
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

  def then(fn: (A, B, C, D) => Svc): CompletePath = new CompletePath(this, pp1, pp2, pp3, pp4) {
    override def toPf(basePath: Path): (FF) => PartialFunction[(HttpMethod, Path), Svc] = {
      filtered: FF => {
        case actualMethod -> path / pp1(s1) / pp2(s2) / pp3(s3) / pp4(s4) if matches(actualMethod, basePath, path) => filtered.andThen(fn(s1, s2, s3, s4))
      }
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

  def then(fn: (A, B, C, D, E) => Svc): CompletePath = new CompletePath(this, pp1, pp2, pp3, pp4, pp5) {
    override def toPf(basePath: Path): (FF) => PartialFunction[(HttpMethod, Path), Svc] = {
      filtered: FF => {
        case actualMethod -> path / pp1(s1) / pp2(s2) / pp3(s3) / pp4(s4) / pp5(s5) if matches(actualMethod, basePath, path) => filtered.andThen(fn(s1, s2, s3, s4, s5))
      }
    }
  }
}
