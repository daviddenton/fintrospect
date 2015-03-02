package io.github.daviddenton.fintrospect.swagger.v1dot1

import java.beans.Introspector.decapitalize

import argo.jdom.JsonNode
import com.twitter.finagle.http.path.Path
import io.github.daviddenton.fintrospect.{Description, SegmentMatcher}
import org.jboss.netty.handler.codec.http.HttpMethod

case class SwDescription(value: String, method: HttpMethod, complete: (Path => Path)) extends Description {
  def toJsonField(rootPath: Path, sm: Seq[SegmentMatcher[_]]): (String, JsonNode) = {
    val params = sm
      .flatMap(_.argument)
      .map { case (name, clazz) => SwParameter(name, Location.path, decapitalize(clazz.getSimpleName))}

    SwPathMethod(method, value, params, Seq(SwResponse(200, ""))).toJsonPair
  }

}