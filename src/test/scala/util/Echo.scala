package util

import argo.jdom.JsonNodeFactories._
import com.twitter.finagle.Service
import com.twitter.util.Future
import io.github.daviddenton.fintrospect.util.ArgoUtil.obj
import io.github.daviddenton.fintrospect.util.JsonResponseBuilder.Ok
import io.github.daviddenton.fintrospect.util.ResponseBuilder._
import org.jboss.netty.handler.codec.http.{HttpRequest, HttpResponse}

case class Echo(parts: String*) extends Service[HttpRequest, HttpResponse] {
  def apply(request: HttpRequest): Future[HttpResponse] = {
    Ok(obj(
      "headers" -> string(request.headers().toString),
      "params" -> string(request.toString), // ERROR!
      "message" -> string(parts.mkString(" "))))
  }
}
