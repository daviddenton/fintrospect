package io.github.daviddenton.fintrospect

import argo.jdom.JsonNode
import com.twitter.finagle.http.path.Path
import org.jboss.netty.handler.codec.http.HttpMethod

trait Description {
  val value: String
  val method: HttpMethod
  val complete: (Path => Path)

  def toJsonField(rootPath: Path, sm: Seq[SegmentMatcher[_]]): (String, JsonNode)

  def matches(method: HttpMethod, rootPath: Path, path: Path) = method == this.method && path == complete(rootPath)
}
