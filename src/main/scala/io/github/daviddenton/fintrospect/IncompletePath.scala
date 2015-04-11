package io.github.daviddenton.fintrospect

import com.twitter.finagle.http.path.{->, /, Path}
import io.github.daviddenton.fintrospect.FintrospectModule2.{FF, Svc}
import io.github.daviddenton.fintrospect.parameters.{Path => Fp, PathParameter}
import org.jboss.netty.handler.codec.http.HttpMethod

object IncompletePath {
  def apply(method: HttpMethod): IncompletePath0 = IncompletePath0(method, identity)
}

abstract class CompletePath(val method: HttpMethod, completePathFn: Path => Path, val params: PathParameter[_]*) {
  def toPf(basePath: Path): (FF) => PartialFunction[(HttpMethod, Path), Svc]

  def describeFor(basePath: Path): String = (completePathFn(basePath).toString :: params.map(_.toString()).toList).mkString("/")
}

trait IncompletePath {
  val method: HttpMethod
  val pathFn: Path => Path
  def matches(actualMethod: HttpMethod, basePath: Path, actualPath: Path) = actualMethod == method && actualPath == pathFn(basePath)
}

case class IncompletePath0(method: HttpMethod, pathFn: Path => Path) extends IncompletePath {
  def /(part: String) = copy(method, pathFn.andThen(_ / part))

  def /[T](pp0: PathParameter[T]) = IncompletePath1(method, pathFn, pp0)

  def then(fn: () => Svc): CompletePath = new CompletePath(method, pathFn) {
    override def toPf(basePath: Path): (FF) => PartialFunction[(HttpMethod, Path), Svc] = {
      filtered: FF => {
        case actualMethod -> path if matches(actualMethod, basePath, path) => filtered.andThen(fn())
      }
    }
  }
}

case class IncompletePath1[A](method: HttpMethod, pathFn: Path => Path,
                           pp1: PathParameter[A]) extends IncompletePath {
  def /(part: String): IncompletePath2[A, String] = /(Fp.fixed(part))

  def /[B](pp2: PathParameter[B]): IncompletePath2[A, B] = IncompletePath2(method, pathFn, pp1, pp2)

  def then(fn: (A) => Svc): CompletePath = new CompletePath(method, pathFn, pp1) {
    override def toPf(basePath: Path): (FF) => PartialFunction[(HttpMethod, Path), Svc] = {
      filtered: FF => {
        case actualMethod -> path / pp1(s1) if matches(actualMethod, basePath, path) => filtered.andThen(fn(s1))
      }
    }
  }
}

case class IncompletePath2[A, B](method: HttpMethod, pathFn: Path => Path,
                              pp1: PathParameter[A],
                              pp2: PathParameter[B]) extends IncompletePath {
  def /(part: String): IncompletePath3[A, B, String] = /(Fp.fixed(part))

  def /[C](pp3: PathParameter[C]): IncompletePath3[A, B, C] = IncompletePath3(method, pathFn, pp1, pp2, pp3)

  def then(fn: (A, B) => Svc): CompletePath = new CompletePath(method, pathFn, pp1, pp2) {
    override def toPf(basePath: Path): (FF) => PartialFunction[(HttpMethod, Path), Svc] = {
      filtered: FF => {
        case actualMethod -> path / pp1(s1) / pp2(s2) if matches(actualMethod, basePath, path) => filtered.andThen(fn(s1, s2))
      }
    }
  }
}

case class IncompletePath3[A, B, C](method: HttpMethod, pathFn: Path => Path,
                                 pp1: PathParameter[A],
                                 pp2: PathParameter[B],
                                 pp3: PathParameter[C]) extends IncompletePath {
  def /(part: String): IncompletePath4[A, B, C, String] = /(Fp.fixed(part))

  def /[D](pp4: PathParameter[D]): IncompletePath4[A, B, C, D] = IncompletePath4(method, pathFn, pp1, pp2, pp3, pp4)

  def then(fn: (A, B, C) => Svc): CompletePath = new CompletePath(method, pathFn, pp1, pp2, pp3) {
    override def toPf(basePath: Path): (FF) => PartialFunction[(HttpMethod, Path), Svc] = {
      filtered: FF => {
        case actualMethod -> path / pp1(s1) / pp2(s2) / pp3(s3) if matches(actualMethod, basePath, path) => filtered.andThen(fn(s1, s2, s3))
      }
    }
  }
}

case class IncompletePath4[A, B, C, D](method: HttpMethod, pathFn: Path => Path,
                                    pp1: PathParameter[A],
                                    pp2: PathParameter[B],
                                    pp3: PathParameter[C],
                                    pp4: PathParameter[D]
                                     ) extends IncompletePath {
  def /(part: String): IncompletePath5[A, B, C, D, String] = /(Fp.fixed(part))

  def /[E](pp5: PathParameter[E]): IncompletePath5[A, B, C, D, E] = IncompletePath5(method, pathFn, pp1, pp2, pp3, pp4, pp5)

  def then(fn: (A, B, C, D) => Svc): CompletePath = new CompletePath(method, pathFn, pp1, pp2, pp3, pp4) {
    override def toPf(basePath: Path): (FF) => PartialFunction[(HttpMethod, Path), Svc] = {
      filtered: FF => {
        case actualMethod -> path / pp1(s1) / pp2(s2) / pp3(s3) / pp4(s4) if matches(actualMethod, basePath, path) => filtered.andThen(fn(s1, s2, s3, s4))
      }
    }
  }
}

case class IncompletePath5[A, B, C, D, E](method: HttpMethod, pathFn: Path => Path,
                                       pp1: PathParameter[A],
                                       pp2: PathParameter[B],
                                       pp3: PathParameter[C],
                                       pp4: PathParameter[D],
                                       pp5: PathParameter[E]
                                        ) extends IncompletePath {
  def /[F](pp5: PathParameter[F]) = throw new UnsupportedOperationException("Limit on number of elements!")

  def then(fn: (A, B, C, D, E) => Svc): CompletePath = new CompletePath(method, pathFn, pp1, pp2, pp3, pp4, pp5) {
    override def toPf(basePath: Path): (FF) => PartialFunction[(HttpMethod, Path), Svc] = {
      filtered: FF => {
        case actualMethod -> path / pp1(s1) / pp2(s2) / pp3(s3) / pp4(s4) / pp5(s5) if matches(actualMethod, basePath, path) => filtered.andThen(fn(s1, s2, s3, s4, s5))
      }
    }
  }
}
