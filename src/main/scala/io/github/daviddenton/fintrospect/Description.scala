package io.github.daviddenton.fintrospect

import com.twitter.finagle.http.path.Path
import org.jboss.netty.handler.codec.http.HttpMethod

case class Description(value: String, method: HttpMethod, complete: (Path => Path)) {
  def matches(method: HttpMethod, rootPath: Path, path: Path) = method == this.method && path == complete(rootPath)
}