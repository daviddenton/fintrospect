package io.fintrospect

import com.twitter.finagle.http.Status.BadRequest
import com.twitter.finagle.http.{Method, Request, Response}
import com.twitter.finagle.{Filter, Service}
import com.twitter.util.Future
import io.fintrospect.Headers.IDENTIFY_SVC_HEADER
import io.fintrospect.formats.PlainText.ResponseBuilder.statusToResponseBuilderConfig
import io.fintrospect.parameters.{Binding, PathBinding, PathParameter, RequestBuilder}


object RouteClient {
  private def identify(method: Method, pathParams: Seq[PathParameter[_]]) = Filter.mk[Request, Response, Request ,Response] {
    (request, svc) => {
      val description = method + ":" + pathParams.map(_.toString()).mkString("/")
      request.headerMap.set(IDENTIFY_SVC_HEADER, description)
      svc(request)
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
  private val service = RouteClient.identify(method, pathParams).andThen(underlyingService)

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
