package io.github.daviddenton.fintrospect.renderers

import argo.jdom.JsonRootNode
import com.twitter.finagle.http.path.Path
import io.github.daviddenton.fintrospect.Route
import io.github.daviddenton.fintrospect.util.JsonResponseBuilder
import org.jboss.netty.handler.codec.http.HttpResponse

class ArgoToHttpResponseDescriptionRenderer(delegate: DescriptionRenderer[JsonRootNode]) extends DescriptionRenderer[HttpResponse] {
  override def apply(basePath: Path, routes: Seq[Route]): HttpResponse = JsonResponseBuilder.Ok(delegate(basePath, routes))
}
