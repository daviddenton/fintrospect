package io.github.daviddenton.fintrospect

import com.twitter.finagle.http.path.Path
import org.jboss.netty.handler.codec.http.HttpMethod

case class On(method: HttpMethod, complete: (Path => Path)) {
  def matches(method: HttpMethod, basePath: Path, path: Path) = method == this.method && path == complete(basePath)
}
