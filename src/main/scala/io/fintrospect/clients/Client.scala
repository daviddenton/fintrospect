package io.fintrospect.clients

import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Future
import io.fintrospect.Headers._
import io.fintrospect.clients.Client.Identify
import io.fintrospect.parameters.{ParamBinding, Parameter, PathParameter, RequestParameter}
import io.fintrospect.util.PlainTextResponseBuilder._
import org.jboss.netty.handler.codec.http.HttpResponseStatus._
import org.jboss.netty.handler.codec.http._

object Client {

  private case class Identify(method: HttpMethod, pathParams: List[PathParameter[_]]) extends SimpleFilter[HttpRequest, HttpResponse]() {
    private val description = method + "." + pathParams.map(_.toString()).mkString("/")

    override def apply(request: HttpRequest, service: Service[HttpRequest, HttpResponse]): Future[HttpResponse] = {
      request.headers().set(IDENTIFY_SVC_HEADER, description)
      service(request)
    }
  }

}

class Client(method: HttpMethod,
             requestParams: List[RequestParameter[_]],
             pathParams: List[PathParameter[_]],
             underlyingService: Service[HttpRequest, HttpResponse]) {


  private val systemBindings = pathParams.filter(_.isEmpty).map(parameter => ParamBinding(parameter, parameter.name))
  private val allPossibleParams = pathParams ++ requestParams
  private val requiredParams = allPossibleParams.filter(_.required)
  private val queryParams = requestParams.filter(_.where == "query")
  private val headerParams = requestParams.filter(_.where == "header")
  private val service = Identify(method, pathParams).andThen(underlyingService)

  def apply(requestBindings: ParamBinding[_]*): Future[HttpResponse] = {
    val allSuppliedParams: Map[Parameter[_], String] = Map((requestBindings ++ systemBindings).map(b => (b.parameter, b.value)): _*)
    val illegalParams = allSuppliedParams.keys.filterNot(param => allPossibleParams.contains(param))
    if (illegalParams.nonEmpty) {
      return Future.value(Error(BAD_REQUEST, "Client: Illegal params passed: " + illegalParams))
    }

    val missingParams = requiredParams.filterNot(allSuppliedParams.keySet.contains)
    if (missingParams.nonEmpty) {
      return Future.value(Error(BAD_REQUEST, "Client: Missing required params passed: " + missingParams))
    }
    val request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, method, buildUrl(allSuppliedParams))
    headerParams.filter(allSuppliedParams.contains).foreach(p => p.into(request, allSuppliedParams(p)))

    service(request)
  }

  def buildUrl(allSuppliedParams: Map[Parameter[_], String]): String = {
    val baseUrl = "/" + pathParams.map(allSuppliedParams(_)).mkString("/")
    val encoder = new QueryStringEncoder(baseUrl)
    allSuppliedParams
      .filter(sp => queryParams.contains(sp._1))
      .foreach(paramAndValue => encoder.addParam(paramAndValue._1.name, paramAndValue._2))
    encoder.toString
  }
}
