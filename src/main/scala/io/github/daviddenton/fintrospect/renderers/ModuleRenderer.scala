package io.github.daviddenton.fintrospect.renderers

import com.twitter.finagle.http.path.Path
import io.github.daviddenton.fintrospect.parameters.RequestParameter
import io.github.daviddenton.fintrospect.util.ResponseBuilder
import io.github.daviddenton.fintrospect.Route
import org.jboss.netty.handler.codec.http.HttpResponse
import org.jboss.netty.handler.codec.http.HttpResponseStatus._

import scala.language.implicitConversions

/**
 * This is used by the FintrospectModule to render the various standard responses (bad request/the description route).
 * Provide one of these to implement a custom format for module responses.
 * @param newBuilder function to supply a new response builder
 * @param descriptionRenderer converts the module routes into an HttpResponse
 * @param badParametersRenderer in the case of invalid or missing parameters to a route, renders the error response
 * @tparam T the configured module renderer
 */
class ModuleRenderer[T](newBuilder: () => ResponseBuilder[T],
                        descriptionRenderer: DescriptionRenderer[HttpResponse],
                        badParametersRenderer: List[RequestParameter[_]] => T) {
  def badRequest(badParameters: List[RequestParameter[_]]): HttpResponse = newBuilder().withCode(BAD_REQUEST).withContent(badParametersRenderer(badParameters)).build

  def description(basePath: Path, routes: Seq[Route]) = descriptionRenderer(basePath, routes)
}


