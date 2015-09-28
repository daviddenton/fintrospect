package util

import com.twitter.finagle.Service
import com.twitter.util.Future
import io.fintrospect.formats.ResponseBuilder._
import io.fintrospect.util.HttpRequestResponseUtil.headersFrom
import io.fintrospect.util.json.Argo.JsonFormat._
import io.fintrospect.util.json.Argo.ResponseBuilder._
import org.jboss.netty.handler.codec.http.{HttpRequest, HttpResponse}

case class Echo(parts: String*) extends Service[HttpRequest, HttpResponse] {
  def apply(request: HttpRequest): Future[HttpResponse] = {
    Ok(obj(
      "headers" -> string(headersFrom(request).toString()),
      "params" -> string(request.toString),
      "message" -> string(parts.mkString(" "))))
  }
}
