package examples

import java.net.InetSocketAddress

import com.twitter.finagle.Service
import com.twitter.finagle.builder.ServerBuilder
import com.twitter.finagle.http.path.Root
import com.twitter.finagle.http.{Http, Request, Response, RichHttp}
import com.twitter.util.Future
import io.github.daviddenton.fintrospect.{Description, FintrospectModule}
import io.github.daviddenton.fintrospect.SegmentMatchers._
import io.github.daviddenton.fintrospect.simple.SimpleJson
import org.jboss.netty.handler.codec.http.HttpMethod

object Simple extends App {

  case class AService() extends Service[Request, Response] {
    def apply(request: Request): Future[Response] = ???
  }

  val module = FintrospectModule(Root, SimpleJson)
    .withRoute(Description("a get endpoint", HttpMethod.GET, _ / "echo"), string("message"), (s: String) => AService())
    .withRoute(Description("a post endpoint", HttpMethod.POST, _ / "echo"), string("message"), (s: String) => AService())
    .withRoute(Description("a friendly endpoint", HttpMethod.GET, _ / "welcome"), string("firstName"), fixed("bertrand"), string("secondName"), (x: String, y: String, z: String) => AService())

  ServerBuilder()
    .codec(RichHttp[Request](Http()))
    .bindTo(new InetSocketAddress(8080))
    .name("")
    .build(module.toService)
}

