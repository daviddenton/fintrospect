package io.fintrospect.renderers

import com.twitter.finagle.http.Response
import com.twitter.finagle.http.path.Path
import io.fintrospect.ServerRoute
import io.fintrospect.parameters.{Parameter, Security}

/**
 * This is used to render the various standard responses (bad request/the description route).
 * Provide one of these to implement a pluggable custom format for module responses.
 */
trait ModuleRenderer {
  def badRequest(badParameters: Seq[Parameter]): Response

  def description(basePath: Path, security: Security, routes: Seq[ServerRoute]): Response
}


