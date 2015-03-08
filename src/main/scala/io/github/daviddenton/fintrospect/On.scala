package io.github.daviddenton.fintrospect

import com.twitter.finagle.http.path.Path
import org.jboss.netty.handler.codec.http.HttpMethod

case class On(method: HttpMethod, completeRoutePath: (Path => Path)) {
  def matches(actualMethod: HttpMethod, basePath: Path, actualPath: Path) = actualMethod == method && actualPath == completeRoutePath(basePath)
}
