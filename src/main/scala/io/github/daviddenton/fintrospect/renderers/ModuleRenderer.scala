package io.github.daviddenton.fintrospect.renderers

import com.twitter.finagle.http.path.Path
import io.github.daviddenton.fintrospect.parameters.RequestParameter
import io.github.daviddenton.fintrospect.util.ResponseBuilder
import io.github.daviddenton.fintrospect.{DescriptionRenderer, Route}
import org.jboss.netty.handler.codec.http.HttpResponse
import org.jboss.netty.handler.codec.http.HttpResponseStatus._

import scala.language.implicitConversions

/**
 * This is used by the FintrospectModule to render the various standard responses (bad request/the description route)
 * @param newBuilder function to supply a new response builder
 * @param descriptionRenderer converts the module routes into a format which can be rendered
 * @param badParametersRenderer in the case of invalid or missing parameters to a route, renders the error response
 * @tparam T the configured module renderer
 */
class ModuleRenderer[T](newBuilder: () => ResponseBuilder[T],
                        descriptionRenderer: DescriptionRenderer[T],
                        badParametersRenderer: List[RequestParameter[_]] => T) {
  def badRequest(badParameters: List[RequestParameter[_]]): HttpResponse = newBuilder().withCode(BAD_REQUEST).withContent(badParametersRenderer(badParameters)).build

  def description(basePath: Path, routes: Seq[Route]) = newBuilder().withCode(OK).withContent(descriptionRenderer(basePath, routes)).build
}


