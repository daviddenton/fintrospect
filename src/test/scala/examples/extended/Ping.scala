package examples.extended

import com.twitter.finagle.Service
import com.twitter.util.Future
import io.github.daviddenton.fintrospect._
import io.github.daviddenton.fintrospect.util.JsonResponseBuilder.Ok
import io.github.daviddenton.fintrospect.util.ResponseBuilder._
import org.jboss.netty.handler.codec.http.HttpMethod._
import org.jboss.netty.handler.codec.http.{HttpRequest, HttpResponse}

class Ping {
  private def pong() = new Service[HttpRequest, HttpResponse] {
    override def apply(request: HttpRequest): Future[HttpResponse] = Ok("pong")
  }

  val route = DescribedRoute("Uptime monitor").at(GET) / "ping" bindTo pong
}
