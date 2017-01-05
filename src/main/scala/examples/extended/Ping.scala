package examples.extended


import com.twitter.finagle.Service
import com.twitter.finagle.http.Method.Get
import com.twitter.finagle.http.{Request, Response}
import io.fintrospect.RouteSpec
import io.fintrospect.formats.Argo.ResponseBuilder._

class Ping {
  private def pong() = Service.mk[Request, Response] { _ => Ok("pong") }

  val route = RouteSpec("Uptime monitor").at(Get) / "ping" bindTo pong
}
