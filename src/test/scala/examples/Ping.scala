package examples

import com.twitter.util.Future
import io.github.daviddenton.fintrospect.FinagleTypeAliases.{FTRequest, FTResponse, FTService}
import io.github.daviddenton.fintrospect._
import io.github.daviddenton.fintrospect.util.ResponseBuilder._
import org.jboss.netty.handler.codec.http.HttpMethod._

class Ping {
  private def pong() = new FTService {
    override def apply(request: FTRequest): Future[FTResponse] = Ok("pong")
  }

  val route = DescribedRoute("Uptime monitor").at(GET) / "ping" bindTo pong
}
