package io.github.daviddenton.fintrospect.example

import java.nio.charset.StandardCharsets._

import com.twitter.finagle.Service
import com.twitter.finagle.http.path.Root
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.{Await, Future}
import io.github.daviddenton.fintrospect.SegmentMatchers._
import io.github.daviddenton.fintrospect.FintrospectModule
import io.github.daviddenton.fintrospect.swagger2dot0.{SwDescription, SwaggerJson}
import org.jboss.netty.handler.codec.http.HttpMethod

object Swagger extends App {

  case class AService() extends Service[Request, Response] {
    def apply(request: Request): Future[Response] = ???
  }

  val module = FintrospectModule(Root, SwaggerJson)
    .withRoute(SwDescription("a get endpoint", HttpMethod.GET, _ / "echo"), string("message"), (s: String) => AService())
    .withRoute(SwDescription("a post endpoint", HttpMethod.POST, _ / "echo"), string("message"), (s: String) => AService())
    .withRoute(SwDescription("a friendly endpoint", HttpMethod.GET, _ / "welcome"), string("firstName"), fixed("bertrand"), string("secondName"), (x: String, y: String, z: String) => AService())

  println(Await.result(module.toService.apply(Request("/"))).content.toString(UTF_8))
}

