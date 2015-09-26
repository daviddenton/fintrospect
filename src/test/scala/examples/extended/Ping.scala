package examples.extended

import com.twitter.finagle.Service
import com.twitter.util.Future
import io.fintrospect._
import io.fintrospect.util.ResponseBuilder._
import io.fintrospect.util.json.Argo.ResponseBuilder._
import org.jboss.netty.handler.codec.http.HttpMethod._
import org.jboss.netty.handler.codec.http.{HttpRequest, HttpResponse}

class Ping {
  private def pong() = new Service[HttpRequest, HttpResponse] {
    override def apply(request: HttpRequest): Future[HttpResponse] = Ok("pong")
  }

  val route = RouteSpec("Uptime monitor").at(GET) / "ping" bindTo pong
}
