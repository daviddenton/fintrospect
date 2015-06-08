package io.fintrospect.clients

import com.twitter.finagle.Service
import com.twitter.util.Future
import io.fintrospect.parameters.{Parameter, PathParameter, RequestParameter}
import io.fintrospect.util.PlainTextResponseBuilder._
import org.jboss.netty.handler.codec.http.HttpResponseStatus._
import org.jboss.netty.handler.codec.http._

class Client(method: HttpMethod,
             requestParams: List[RequestParameter[_, _]],
             pathParams: List[PathParameter[_]],
             service: Service[HttpRequest, HttpResponse]) {
  private val systemSuppliedParams = pathParams.filter(_.isEmpty).map(p => p -> p.name)
  private val allPossibleParams = pathParams ++ requestParams
  private val requiredParams = allPossibleParams.filter(_.required)
  private val queryParams = requestParams.filter(_.location.toString == "query")

  def apply(userSuppliedParams: (Parameter[_], String)*): Future[HttpResponse] = {
    val allSuppliedParams = Map(userSuppliedParams: _*) ++ systemSuppliedParams
    val illegalParams = allSuppliedParams.keys.filterNot(param => allPossibleParams.contains(param))
    if (illegalParams.nonEmpty) {
      return Future.value(Error(BAD_REQUEST, "Client: Illegal params passed: " + illegalParams))
    }

    val missingParams = requiredParams.filterNot(allSuppliedParams.contains)
    if (missingParams.nonEmpty) {
      return Future.value(Error(BAD_REQUEST, "Client: Missing required params passed: " + missingParams))
    }
    service(new DefaultHttpRequest(HttpVersion.HTTP_1_1, method, buildUrl(allSuppliedParams)))
  }

  def buildUrl(allSuppliedParams: Map[Parameter[_], String]): String = {
    val baseUrl = pathParams.map(allSuppliedParams(_)).mkString("/")
    val encoder = new QueryStringEncoder(baseUrl)
    allSuppliedParams
      .filter(sp => queryParams.contains(sp._1))
      .foreach(paramAndValue => encoder.addParam(paramAndValue._1.name, paramAndValue._2))
    encoder.toString
  }
}
