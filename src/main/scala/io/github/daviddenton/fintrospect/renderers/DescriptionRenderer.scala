package io.github.daviddenton.fintrospect.renderers

import com.twitter.finagle.http.path.Path
import io.github.daviddenton.fintrospect.Route
import org.jboss.netty.handler.codec.http.HttpResponse

/**
 * Contract trait for the pluggable Renderers (Swagger etc..)
 */
trait DescriptionRenderer {
  def apply(basePath: Path, routes: Seq[Route]): HttpResponse
}
