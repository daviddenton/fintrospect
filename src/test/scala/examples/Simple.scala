package examples

import java.net.InetSocketAddress

import com.twitter.finagle.builder.ServerBuilder
import com.twitter.finagle.http.filter.Cors._
import com.twitter.finagle.http.path.Root
import com.twitter.finagle.http.{Http, Request, RichHttp}
import io.github.daviddenton.fintrospect.parameters.{Query, Header}
import io.github.daviddenton.fintrospect.parameters.Path._
import io.github.daviddenton.fintrospect.renderers.SimpleJson
import io.github.daviddenton.fintrospect.{On, Description, FintrospectModule}
import org.jboss.netty.handler.codec.http.HttpMethod._
import org.jboss.netty.handler.codec.http.HttpResponseStatus._
import util.Echo

object Simple extends App {
  val module = FintrospectModule(Root, SimpleJson())
    .withRoute(
      Description("a get endpoint")
        .requiring(Header.string("header"))
        .returning(OK -> "peachy")
        .returning(FORBIDDEN -> "no way jose"),
      On(GET, _ / "echo"), string("message"), (s: String) => Echo(s))
    .withRoute(Description("a post endpoint"), On(POST, _ / "echo"), string("message"), (s: String) => Echo(s))
    .withRoute(
      Description("a friendly endpoint")
        .requiring(Query.boolean("query")),
      On(GET, _ / "welcome"), string("firstName"), fixed("bertrand"), string("secondName"), (x: String, y: String, z: String) => Echo(x, y, z))

  ServerBuilder()
    .codec(RichHttp[Request](Http()))
    .bindTo(new InetSocketAddress(8080))
    .name("")
    .build(new HttpFilter(UnsafePermissivePolicy).andThen(module.toService))
}
