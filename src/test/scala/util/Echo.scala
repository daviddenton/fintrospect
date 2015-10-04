package util

import com.twitter.finagle.Service
import com.twitter.finagle.httpx.{Request, Response}
import com.twitter.util.Future
import io.fintrospect.formats.ResponseBuilder._
import io.fintrospect.formats.json.Argo.JsonFormat._
import io.fintrospect.formats.json.Argo.ResponseBuilder._
import io.fintrospect.util.HttpRequestResponseUtil.headersFrom

case class Echo(parts: String*) extends Service[Request, Response] {
  def apply(request: Request): Future[Response] = {
    Ok(obj(
      "headers" -> string(headersFrom(request).toString()),
      "params" -> string(request.toString()),
      "message" -> string(parts.mkString(" "))))
  }
}
