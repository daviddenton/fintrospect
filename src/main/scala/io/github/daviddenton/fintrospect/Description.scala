package io.github.daviddenton.fintrospect

import com.twitter.finagle.http.path.Path
import io.github.daviddenton.fintrospect.Parameter._
import org.jboss.netty.handler.codec.http.HttpMethod

case class Description private(value: String, method: HttpMethod, params: List[Parameter], complete: (Path => Path)) {
  def withHeader(name: String, clazz: Class[_]) = copy(params = header(name, clazz) :: params)

  def withQueryParam(name: String, clazz: Class[_]) = copy(params = query(name, clazz) :: params)

  def withBodyParam(name: String, clazz: Class[_]) = copy(params = body(name, clazz) :: params)

  def matches(method: HttpMethod, rootPath: Path, path: Path) = method == this.method && path == complete(rootPath)
}

object Description {
  def apply(value: String, method: HttpMethod, complete: (Path => Path)): Description = new Description(value, method, Nil, complete)
}