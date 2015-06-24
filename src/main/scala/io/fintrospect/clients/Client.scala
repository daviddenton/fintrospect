package io.fintrospect.clients

import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Future
import io.fintrospect.Headers._
import io.fintrospect.clients.Client.Identify
import io.fintrospect.parameters._
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
             queryParams: Seq[QueryParameter[_]],
             pathParams: Seq[PathParameter[_]],
             body: Option[Body[_]],
             underlyingService: Service[HttpRequest, HttpResponse]) {

//  private val providedBindings = pathParams.filter(_.isEmpty).map(parameter => parameter.of(parameter.name))
  private val allPossibleParams: Seq[Parameter[_]] = pathParams ++ headerParams ++ queryParams ++ body.toSeq.flatMap(_.iterator)

  private val requiredParams = allPossibleParams.filter(_.required)

  private val service = Identify(method, pathParams).andThen(underlyingService)

//
//  def into(requestBindings: ParamBinding[_]*): Future[HttpResponse] = {
//    val allSuppliedParams: Map[Parameter[_], String] = Map((requestBindings ++ systemBindings).map(b => (b.parameter, b.value)): _*)
//    val illegalParams = allSuppliedParams.keys.filterNot(param => allPossibleParams.contains(param))
//    if (illegalParams.nonEmpty) {
//      return Future.value(Error(BAD_REQUEST, "Client: Illegal params passed: " + illegalParams))
//    }
//
//    val missingParams = requiredParams.filterNot(allSuppliedParams.keySet.contains)
//    if (missingParams.nonEmpty) {
//      return Future.value(Error(BAD_REQUEST, "Client: Missing required params passed: " + missingParams))
//    }
//    val request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, method, buildUrl(allSuppliedParams))
//    headerParams.filter(allSuppliedParams.contains).foreach(p => p.into(request, allSuppliedParams(p)))
//
//    service(request)
//  }

  /**
   * Make a request to this client route using the passed bindings
   * @param bindings the bindings for this request
   * @return the response Future
   */
  def apply(bindings: Bindings*): Future[HttpResponse] =

    // check missing here...
    service(bindings.flatMap(_.bindings).foldLeft(RequestBuild()) {
      (requestBuild, next) => requestBuild.bind(next)
    }.build(method))

  private def buildUrl(allSuppliedParams: Map[Parameter[_], String]): String = {
    val baseUrl = "/" + pathParams.map(allSuppliedParams(_)).mkString("/")
    val encoder = new QueryStringEncoder(baseUrl)
    allSuppliedParams
      .filter(sp => queryParams.contains(sp._1))
      .foreach(paramAndValue => encoder.addParam(paramAndValue._1.name, paramAndValue._2))
    encoder.toString
  }
}
