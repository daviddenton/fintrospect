package util

import com.twitter.finagle.Service
import com.twitter.util.Future
import io.fintrospect.formats.ResponseBuilder._
import io.fintrospect.formats.json.Argo
import io.fintrospect.formats.json.Argo.JsonFormat._
import io.fintrospect.formats.json.Argo.ResponseBuilder._
import io.fintrospect.util.HttpRequestResponseUtil.headersFrom
import org.jboss.netty.handler.codec.http.{HttpRequest, HttpResponse}

case class Echo(parts: String*) extends Service[HttpRequest, HttpResponse] {
  def apply(request: HttpRequest): Future[HttpResponse] = {
    Ok(obj(
      "headers" -> string(headersFrom(request).toString()),
      "params" -> string(request.toString),
      "message" -> string(parts.mkString(" "))))
  }
}
