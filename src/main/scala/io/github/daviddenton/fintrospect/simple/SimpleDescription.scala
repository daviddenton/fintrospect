package io.github.daviddenton.fintrospect.simple

import argo.jdom.JsonNode
import argo.jdom.JsonNodeFactories._
import com.twitter.finagle.http.path.Path
import org.jboss.netty.handler.codec.http.HttpMethod
import io.github.daviddenton.fintrospect.{Description, SegmentMatcher}

case class SimpleDescription(value: String, method: HttpMethod, complete: (Path => Path)) extends Description {
  override def toJsonField(rootPath: Path, sm: Seq[SegmentMatcher[_]]): (String, JsonNode) = {
    method + ":" + (complete(rootPath).toString :: sm.map(_.toString).toList).mkString("/") -> string(value)
  }
}
