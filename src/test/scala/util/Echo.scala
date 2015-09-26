package util

import argo.jdom.JsonNodeFactories._
import com.twitter.finagle.Service
import com.twitter.util.Future
import io.fintrospect.util.HttpRequestResponseUtil.headersFrom
import io.fintrospect.util.ResponseBuilder._
import io.fintrospect.util.json.ArgoJsonFormat.obj
import io.fintrospect.util.json.ArgoJsonResponseBuilder.Ok
import io.fintrospect.util.json.{ArgoJsonFormat, ArgoJsonResponseBuilder}
import org.jboss.netty.handler.codec.http.{HttpRequest, HttpResponse}

case class Echo(parts: String*) extends Service[HttpRequest, HttpResponse] {
  def apply(request: HttpRequest): Future[HttpResponse] = {
    Ok(obj(
      "headers" -> string(headersFrom(request).toString()),
      "params" -> string(request.toString),
      "message" -> string(parts.mkString(" "))))
  }
}
