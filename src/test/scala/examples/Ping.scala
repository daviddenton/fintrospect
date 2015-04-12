package examples

import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Future
import io.github.daviddenton.fintrospect._
import io.github.daviddenton.fintrospect.util.ResponseBuilder._
import org.jboss.netty.handler.codec.http.HttpMethod._

class Ping {
  private def pong() = new Service[Request, Response] {
    override def apply(request: Request): Future[Response] = Ok("pong")
  }

  val route = DescribedRoute("Uptime monitor").at(GET) / "ping" bindTo pong
}
