package io.github.daviddenton.fintrospect.renderers

import com.twitter.finagle.http.path.Path
import io.github.daviddenton.fintrospect.Route
import io.github.daviddenton.fintrospect.parameters.RequestParameter
import org.jboss.netty.handler.codec.http.HttpResponse

import scala.language.implicitConversions

/**
 * This is used by the FintrospectModule to render the various standard responses (bad request/the description route).
 * Provide one of these to implement a custom format for module responses.
 */
trait ModuleRenderer {
  def badRequest(badParameters: List[RequestParameter[_]]): HttpResponse

  def description(basePath: Path, routes: Seq[Route]): HttpResponse
}


