package io.fintrospect.renderers

import com.twitter.finagle.http.Status._
import com.twitter.finagle.http.path.Path
import com.twitter.finagle.http.{Request, Response}
import io.fintrospect.ServerRoute
import io.fintrospect.parameters.{Parameter, Security}

/**
 * This is used to render the various standard responses (bad request/the description route).
 * Provide one of these to implement a pluggable custom format for module responses.
 */
trait ModuleRenderer {
  def notFound(request: Request): Response = Response(NotFound)

  def badRequest(badParameters: Seq[Parameter]): Response

  def description(basePath: Path, security: Security, routes: Seq[ServerRoute[_, _]]): Response
}


