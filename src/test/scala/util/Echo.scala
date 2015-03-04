package util

import argo.jdom.JsonNodeFactories._
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Future
import io.github.daviddenton.fintrospect.util.ArgoUtil.obj
import util.ResponseBuilder._

case class Echo(parts: String*) extends Service[Request, Response] {
  def apply(request: Request): Future[Response] = {
    Ok(obj("message" -> string(parts.mkString(" "))))
  }
}
