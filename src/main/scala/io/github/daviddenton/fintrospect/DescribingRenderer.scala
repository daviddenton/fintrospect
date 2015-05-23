package io.github.daviddenton.fintrospect

import com.twitter.finagle.http.path.Path
import org.jboss.netty.handler.codec.http.HttpResponse

/**
 * Contract trait for the pluggable Renderers (Swagger etc..)
 */
trait DescribingRenderer {
  def apply(basePath: Path, routes: Seq[Route]): HttpResponse
}


