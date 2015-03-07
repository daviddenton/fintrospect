package io.github.daviddenton.fintrospect

import com.twitter.finagle.http.path.Path
import io.github.daviddenton.fintrospect.Parameters._
import org.jboss.netty.handler.codec.http.HttpMethod

import scala.reflect.ClassTag

case class Description private(value: String, method: HttpMethod, params: List[Parameter[_]], complete: (Path => Path)) {
  def withHeader[T](name: String)(implicit ct: ClassTag[T]) = copy(params = header[T](name) :: params)

  def withQueryParam[T](name: String)(implicit ct: ClassTag[T]) = copy(params = query[T](name) :: params)

  def withBodyParam[T](name: String)(implicit ct: ClassTag[T]) = copy(params = body[T](name) :: params)

  def matches(method: HttpMethod, rootPath: Path, path: Path) = method == this.method && path == complete(rootPath)
}

object Description {
  def apply(value: String, method: HttpMethod, complete: (Path => Path)): Description = new Description(value, method, Nil, complete)
}