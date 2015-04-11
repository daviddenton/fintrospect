package io.github.daviddenton.fintrospect

import argo.jdom.JsonRootNode
import com.twitter.finagle.http.path.Path

/**
 * Contract trait for the pluggable Renderers (Swagger etc..)
 */
trait Renderer {
  def apply(basePath: Path, routes: Seq[Route]): JsonRootNode
}
