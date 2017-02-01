package io.fintrospect

import com.twitter.finagle.http.{Method, Request, Response}
import com.twitter.finagle.{Filter, Service}
import com.twitter.util.Future
import io.fintrospect.formats.PlainText.ResponseBuilder._
import io.fintrospect.parameters.{Binding, PathBinding, PathParameter}

object RouteClient {
  private def identify[O](method: Method, pathParams: Seq[PathParameter[_]]) = Filter.mk[Request, O, Request, O] {
    (request, svc) => {
      request.headerMap(Headers.IDENTIFY_SVC_HEADER) = method + ":" + pathParams.map(_.toString()).mkString("/")
      svc(request)
    }
  }

  /**
    * This exception is thrown at runtime when not all of the required parameters have been passed to the RouteClient
    * function
    */
  class BrokenContract(message: String) extends Exception(message)
}

/**
  * Representation of a pre-configured client HTTP call
  *
  * @param spec              the route specification
  * @param method            the HTTP method
  * @param underlyingService the underlying service to make the request from
  * @param pathParams        the path parameters to use
  */
class RouteClient[Rsp](method: Method,
                  spec: RouteSpec,
                  pathParams: Seq[PathParameter[_]],
                  underlyingService: Service[Request, Rsp]) {

  private val providedBindings = pathParams.filter(_.isFixed).map(p => new PathBinding(p, p.name))
  private val allPossibleParams = pathParams ++ spec.requestParams ++ spec.body.toSeq.flatMap(_.iterator)
  private val requiredParams = allPossibleParams.filter(_.required)
  private val service = RouteClient.identify(method, pathParams).andThen(underlyingService)

  /**
    * Make a request to this client route using the passed bindings. Returns Future.exception[BrokenContract]
    * at runtime if not all required parameters have been passed.
    *
    * @param userBindings the bindings for this request
    * @return the response Future
    */
  def apply(userBindings: Iterable[Binding]*): Future[Rsp] = {
    val suppliedBindings = userBindings.flatten ++ providedBindings

    val userSuppliedParams = suppliedBindings.map(_.parameter).filter(_ != null)

    val missing = requiredParams.diff(userSuppliedParams)
    val unknown = userSuppliedParams.diff(allPossibleParams)

    if (missing.nonEmpty) {
      Future.exception(new RouteClient.BrokenContract("Client: Missing required params passed: " + missing.mkString(", ")))
    } else if (unknown.nonEmpty) {
      Future.exception(new RouteClient.BrokenContract("Client: Unknown params passed: " + unknown.mkString(", ")))
    } else {
      service(buildRequest(suppliedBindings))
    }
  }

  private def buildRequest(suppliedBindings: Seq[Binding]): Request = suppliedBindings
    .sortBy(p => pathParams.indexOf(p.parameter))
    .foldLeft(RequestBuilder(method)) { (requestBuild, next) => next(requestBuild) }.build()
}
