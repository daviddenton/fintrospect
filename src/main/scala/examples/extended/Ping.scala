package examples.extended


import com.twitter.finagle.Service
import com.twitter.finagle.http.Method.Get
import com.twitter.finagle.http.{Request, Response, Status}
import io.fintrospect.RouteSpec
import io.fintrospect.formats.Argo.ResponseBuilder.implicits.statusToResponseBuilderConfig

class Ping {
  private def pong() = Service.mk[Request, Response] { _ => Status.Ok("pong") }

  val route = RouteSpec("Uptime monitor").at(Get) / "ping" bindTo pong
}
