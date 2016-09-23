package io.fintrospect.util

import com.twitter.finagle.Service
import com.twitter.finagle.http.Status.Ok
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Future
import io.fintrospect.formats.Argo.JsonFormat.{obj, string}
import io.fintrospect.formats.Argo.ResponseBuilder.implicits.statusToResponseBuilderConfig
import io.fintrospect.util.HttpRequestResponseUtil.headersFrom

case class Echo(parts: String*) extends Service[Request, Response] {
  def apply(request: Request): Future[Response] = {
    Ok(obj(
      "headers" -> string(headersFrom(request).toString()),
      "params" -> string(request.toString()),
      "message" -> string(parts.mkString(" "))))
  }
}
