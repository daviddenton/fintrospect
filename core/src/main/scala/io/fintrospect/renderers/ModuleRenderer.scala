package io.fintrospect.renderers

import com.twitter.finagle.http.Status.NotFound
import com.twitter.finagle.http.path.Path
import com.twitter.finagle.http.{Request, Response}
import io.fintrospect.util.ExtractionError
import io.fintrospect.{Security, ServerRoute}

/**
 * This is used to render the various standard responses (bad request/the description route).
 * Provide one of these to implement a pluggable custom format for module responses.
 */
trait ModuleRenderer {
  def notFound(request: Request): Response = Response(NotFound)

  def badRequest(badParameters: Seq[ExtractionError]): Response

  def description(basePath: Path, security: Security, routes: Seq[ServerRoute[_, _]]): Response
}


