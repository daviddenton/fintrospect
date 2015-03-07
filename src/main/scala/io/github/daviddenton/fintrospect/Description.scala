package io.github.daviddenton.fintrospect

import com.twitter.finagle.http.path.Path
import org.jboss.netty.handler.codec.http.HttpMethod

case class Description private(value: String, method: HttpMethod, params: List[RequestParameter[_]], complete: (Path => Path)) {
  def requiring(rp: RequestParameter[_]) = copy(params = rp :: params)

  def matches(method: HttpMethod, rootPath: Path, path: Path) = method == this.method && path == complete(rootPath)
}

object Description {
  def apply(value: String, method: HttpMethod, complete: (Path => Path)): Description = Description(value, method, Nil, complete)
}