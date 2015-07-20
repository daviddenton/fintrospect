package io.fintrospect.clients

import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Future
import io.fintrospect.Headers._
import io.fintrospect.clients.Client.Identify
import io.fintrospect.parameters._
import io.fintrospect.util.PlainTextResponseBuilder._
import org.jboss.netty.handler.codec.http.HttpResponseStatus._
import org.jboss.netty.handler.codec.http._

object Client {

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
 * @param method the HTTP method
 * @param headerParams the header parameters to use
 * @param queryParams the query parameters to use
 * @param pathParams the path parameters to use
 * @param underlyingService the underlying service to make the request from
 */
class Client(method: HttpMethod,
             headerParams: Seq[HeaderParameter[_]],
             queryParams: Seq[QueryParameter[_, _]],
             pathParams: Seq[PathParameter[_]],
             body: Option[Body[_]],
             underlyingService: Service[HttpRequest, HttpResponse]) {

  private val providedBindings = pathParams.filter(_.isFixed).map(p => new PathBinding(p, p.name))
  private val allPossibleParams = pathParams ++ headerParams ++ queryParams ++ body.toSeq.flatMap(_.iterator)
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
      return Future.value(Error(BAD_REQUEST, "Client: Missing required params passed: " + missing.toSet))
    }

    val invalid = userSuppliedParams.diff(allPossibleParams)
    if (invalid.nonEmpty) {
      return Future.value(Error(BAD_REQUEST, "Client: Unknown params passed: " + invalid.toSet))
    }

    val req = suppliedBindings
      .sortBy(p => pathParams.indexOf(p.parameter))
      .foldLeft(RequestBuild()) { (requestBuild, next) => next(requestBuild) }.build(method)

    service(req)
  }
}
