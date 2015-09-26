package io.fintrospect

import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Future
import io.fintrospect.Headers._
import io.fintrospect.RouteClient.Identify
import io.fintrospect.parameters._
import io.fintrospect.util.PlainTextResponseBuilder._
import io.fintrospect.util.ResponseBuilder._
import org.jboss.netty.handler.codec.http.HttpResponseStatus._
import org.jboss.netty.handler.codec.http._


object RouteClient {

  private case class Identify(method: HttpMethod, pathParams: Seq[PathParameter[_]]) extends SimpleFilter[HttpRequest, HttpResponse]() {
    private val description = method + "." + pathParams.map(_.toString()).mkString("/")

    override def apply(request: HttpRequest, service: Service[HttpRequest, HttpResponse]): Future[HttpResponse] = {
      request.headers().set(IDENTIFY_SVC_HEADER, description)
      service(request)
    }
  }

}

/**
 * Representation of a pre-configured client HTTP call
 * @param routeSpec the route specification
 * @param method the HTTP method
 * @param underlyingService the underlying service to make the request from
 * @param pathParams the path parameters to use
 */
class RouteClient(method: HttpMethod,
             routeSpec: RouteSpec,
             pathParams: Seq[PathParameter[_]],
             underlyingService: Service[HttpRequest, HttpResponse]) {

  private val providedBindings = pathParams.filter(_.isFixed).map(p => new PathBinding(p, p.name))
  private val allPossibleParams = pathParams ++ routeSpec.headerParams ++ routeSpec.queryParams ++ routeSpec.body.toSeq.flatMap(_.iterator)
  private val requiredParams = allPossibleParams.filter(_.required)
  private val service = Identify(method, pathParams).andThen(underlyingService)

  /**
   * Make a request to this client route using the passed bindings
   * @param userBindings the bindings for this request
   * @return the response Future
   */
  def apply(userBindings: Iterable[Binding]*): Future[HttpResponse] = {
    val suppliedBindings = userBindings.flatten ++ providedBindings

    val userSuppliedParams = suppliedBindings.map(_.parameter).filter(_ != null)

    val missing = requiredParams.diff(userSuppliedParams)
    if (missing.nonEmpty) {
      return Error(BAD_REQUEST, "Client: Missing required params passed: " + missing.toSet)
    }

    val invalid = userSuppliedParams.diff(allPossibleParams)
    if (invalid.nonEmpty) {
      return Error(BAD_REQUEST, "Client: Unknown params passed: " + invalid.toSet)
    }

    val req = suppliedBindings
      .sortBy(p => pathParams.indexOf(p.parameter))
      .foldLeft(RequestBuild()) { (requestBuild, next) => next(requestBuild) }.build(method)

    service(req)
  }
}
