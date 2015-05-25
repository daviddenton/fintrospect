package io.github.daviddenton.fintrospect.renderers

import com.twitter.finagle.http.path.Path
import io.github.daviddenton.fintrospect.Route

/**
 * Contract trait for the pluggable Renderers (Swagger etc..)
 */
trait DescriptionRenderer[T] {
  def apply(basePath: Path, routes: Seq[Route]): T
}
