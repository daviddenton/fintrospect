package examples.extended

import com.twitter.finagle.Service
import com.twitter.finagle.http.Method._
import com.twitter.finagle.http.{Request, Response, Status}
import io.fintrospect._
import io.fintrospect.formats.ResponseBuilder._
import io.fintrospect.formats.json.Argo.ResponseBuilder._

class Ping {
  private def pong() = Service.mk[Request, Response] { _ => Status.Ok("pong") }

  val route = RouteSpec("Uptime monitor").at(Get) / "ping" bindTo pong
}
