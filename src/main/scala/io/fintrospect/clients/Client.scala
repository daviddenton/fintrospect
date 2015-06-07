package io.fintrospect.clients

import com.twitter.finagle.Service
import com.twitter.util.Future
import io.fintrospect.parameters.{Parameter, PathParameter, RequestParameter, Requirement}
import io.fintrospect.util.PlainTextResponseBuilder._
import org.jboss.netty.handler.codec.http.HttpResponseStatus._
import org.jboss.netty.handler.codec.http._

class Client(method: HttpMethod,
                   requestParams: List[RequestParameter[_]],
                   pathParams: List[PathParameter[_]],
                   service: Service[HttpRequest, HttpResponse]) {
  private val systemSuppliedParams = pathParams.filter(_.isEmpty).map(p => p -> p.name)
  private val allPossibleParams = pathParams ++ requestParams
  private val requiredParams = allPossibleParams.filter(_.requirement == Requirement.Mandatory)

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
    service(new DefaultHttpRequest(HttpVersion.HTTP_1_1, method, pathParams.map(allSuppliedParams(_)).mkString("/")))
  }
}
