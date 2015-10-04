package examples.extended

import com.twitter.finagle.Service
import com.twitter.finagle.httpx.{Method, Request, Response}
import com.twitter.util.Future
import io.fintrospect._
import io.fintrospect.formats.ResponseBuilder._
import io.fintrospect.formats.json.Argo.ResponseBuilder._

class Ping {
  private def pong() = new Service[Request, Response] {
    override def apply(request: Request): Future[Response] = Ok("pong")
  }

  val route = RouteSpec("Uptime monitor").at(Method.Get) / "ping" bindTo pong
}
