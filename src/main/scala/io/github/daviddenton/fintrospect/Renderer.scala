package io.github.daviddenton.fintrospect

import argo.jdom.JsonRootNode
import com.twitter.finagle.http.path.Path
import io.github.daviddenton.fintrospect.util.ResponseBuilder
import org.jboss.netty.handler.codec.http.HttpResponse

/**
 * Contract trait for the pluggable Renderers (Swagger etc..)
 */
trait Renderer {
  def apply(basePath: Path, routes: Seq[Route]): JsonRootNode
}

object Renderer {
  def toResponse(jsonRootNode: JsonRootNode): HttpResponse = ResponseBuilder.Json.Ok(jsonRootNode).build
}
