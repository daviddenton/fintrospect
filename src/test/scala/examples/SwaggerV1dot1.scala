package examples

import java.net.InetSocketAddress

import _root_.util.Echo
import com.twitter.finagle.builder.ServerBuilder
import com.twitter.finagle.http.filter.Cors._
import com.twitter.finagle.http.path.Root
import com.twitter.finagle.http.{Http, Request, RichHttp}
import io.github.daviddenton.fintrospect.Locations._
import io.github.daviddenton.fintrospect.SegmentMatchers.{string, _}
import io.github.daviddenton.fintrospect._
import io.github.daviddenton.fintrospect.renderers.Swagger1dot1Json
import org.jboss.netty.handler.codec.http.HttpMethod

object SwaggerV1dot1 extends App {
  val module = FintrospectModule(Root, Swagger1dot1Json())
    .withRoute(Description("a get endpoint", HttpMethod.GET, _ / "echo").requiring(Parameters.string(Header, "header")), string("message"), (s: String) => Echo(s))
    .withRoute(Description("a post endpoint", HttpMethod.POST, _ / "echo").requiring(Parameters.int(Query, "query")), string("message"), (s: String) => Echo(s))
    .withRoute(Description("a friendly endpoint", HttpMethod.GET, _ / "welcome").requiring(Parameters.boolean(Query, "query")), string("firstName"), fixed("bertrand"), string("secondName"), (x: String, y: String, z: String) => Echo(x, y, z))

  ServerBuilder()
    .codec(RichHttp[Request](Http()))
    .bindTo(new InetSocketAddress(8080))
    .name("")
    .build(new HttpFilter(UnsafePermissivePolicy).andThen(module.toService))

  println("See the service description at: http://localhost:8080")
}

