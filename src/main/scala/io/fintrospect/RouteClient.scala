package io.fintrospect

import com.twitter.finagle.http.Status._
import com.twitter.finagle.http.{Method, Request, Response}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Future
import io.fintrospect.Headers._
import io.fintrospect.RouteClient.Identify
import io.fintrospect.formats.text.PlainTextResponseBuilder._
import io.fintrospect.parameters._


object RouteClient {

  private case class Identify(method: Method, pathParams: Seq[PathParameter[_]]) extends SimpleFilter[Request, Response]() {
    private val description = method + ":" + pathParams.map(_.toString()).mkString("/")

    override def apply(request: Request, service: Service[Request, Response]): Future[Response] = {
      request.headerMap.set(IDENTIFY_SVC_HEADER, description)
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
class RouteClient(method: Method,
                  routeSpec: RouteSpec,
                  pathParams: Seq[PathParameter[_]],
                  underlyingService: Service[Request, Response]) {

  private val providedBindings = pathParams.filter(_.isFixed).map(p => new PathBinding(p, p.name))
  private val allPossibleParams = pathParams ++ routeSpec.headerParams ++ routeSpec.queryParams ++ routeSpec.body.toSeq.flatMap(_.iterator)
  private val requiredParams = allPossibleParams.filter(_.required)
  private val service = Identify(method, pathParams).andThen(underlyingService)

  /**
    * Make a request to this client route using the passed bindings
    * @param userBindings the bindings for this request
    * @return the response Future
    */
  def apply(userBindings: Iterable[Binding]*): Future[Response] = {
    val suppliedBindings = userBindings.flatten ++ providedBindings

    val userSuppliedParams = suppliedBindings.map(_.parameter).filter(_ != null)

    val missing = requiredParams.diff(userSuppliedParams)
    val invalid = userSuppliedParams.diff(allPossibleParams)

    if (missing.nonEmpty) {
      BadRequest("Client: Missing required params passed: " + missing.toSet)
    } else if (invalid.nonEmpty) {
      BadRequest("Client: Unknown params passed: " + invalid.toSet)
    } else {
      service(buildRequest(suppliedBindings))
    }
  }

  private def buildRequest(suppliedBindings: Seq[Binding]): Request = suppliedBindings
    .sortBy(p => pathParams.indexOf(p.parameter))
    .foldLeft(RequestBuilder(method)) { (requestBuild, next) => next(requestBuild) }.build()
}
