package examples.full.main

import com.twitter.finagle.Service
import com.twitter.finagle.http.Method._
import com.twitter.finagle.http.Status._
import com.twitter.finagle.http.{Request, Response}
import io.fintrospect._
import io.fintrospect.formats.ResponseBuilder._
import io.fintrospect.formats.json.Argo.ResponseBuilder._

class Ping {
  private def pong() = Service.mk[Request, Response] { _ => Ok("pong") }

  val route = RouteSpec("Uptime monitor").at(Get) / "ping" bindTo pong
}
