package examples

import java.net.InetSocketAddress

import com.twitter.finagle.builder.ServerBuilder
import com.twitter.finagle.http.filter.Cors._
import com.twitter.finagle.http.path.Root
import com.twitter.finagle.http.{Http, Request, RichHttp}
import io.github.daviddenton.fintrospect.parameters.Path._
import io.github.daviddenton.fintrospect.SegmentMatchers._
import io.github.daviddenton.fintrospect.renderers.Swagger2dot0Json
import io.github.daviddenton.fintrospect.{Description, FintrospectModule}
import org.jboss.netty.handler.codec.http.HttpMethod._
import util.Echo

object SwaggerV2dot0 extends App {
  val module = FintrospectModule(Root, Swagger2dot0Json())
    .withRoute(Description("a get endpoint", GET, _ / "echo"), string("message"), (s: String) => Echo(s))
    .withRoute(Description("a post endpoint", POST, _ / "echo"), string("message"), (s: String) => Echo(s))
    .withRoute(Description("a friendly endpoint", GET, _ / "welcome"), string("firstName"), fixed("bertrand"), string("secondName"), (x: String, y: String, z: String) => Echo(x))

  ServerBuilder()
    .codec(RichHttp[Request](Http()))
    .bindTo(new InetSocketAddress(8080))
    .name("")
    .build(new HttpFilter(UnsafePermissivePolicy).andThen(module.toService))

  println("See the service description at: http://localhost:8080")
}

