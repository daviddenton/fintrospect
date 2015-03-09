package examples

import java.net.InetSocketAddress

import _root_.util.Echo
import com.twitter.finagle.builder.ServerBuilder
import com.twitter.finagle.http.filter.Cors._
import com.twitter.finagle.http.path.Root
import com.twitter.finagle.http.{Http, Request, RichHttp}
import io.github.daviddenton.fintrospect.MimeTypes._
import io.github.daviddenton.fintrospect._
import io.github.daviddenton.fintrospect.parameters.{Query, Header}
import io.github.daviddenton.fintrospect.parameters.Path._
import io.github.daviddenton.fintrospect.renderers.Swagger1dot1Json
import org.jboss.netty.handler.codec.http.HttpMethod._
import org.jboss.netty.handler.codec.http.HttpResponseStatus._

object SwaggerV1dot1 extends App {
  val module = FintrospectModule(Root, Swagger1dot1Json())
    .withRoute(
      Description("a get endpoint", "some rambling description of what this thing actually does")
        .producing(APPLICATION_JSON)
        .requiring(Header.string("header", "description of the header"))
        .returning(OK -> "peachy")
        .returning(FORBIDDEN -> "no way jose"),
      On(GET, _ / "echo"), string("message"), (s: String) => Echo(s))
    .withRoute(
      Description("a post endpoint")
        .consuming(APPLICATION_ATOM_XML, APPLICATION_SVG_XML)
        .producing(APPLICATION_JSON)
        .requiring(Query.int("query")),
      On(POST, _ / "echo"), string("message"), (s: String) => Echo(s))
    .withRoute(
      Description("a friendly endpoint", "this service doesn't really do anything real")
        .requiring(Query.boolean("query", "description of the query")),
      On(GET, _ / "welcome"), string("firstName"), fixed("bertrand"), string("secondName"), (x: String, y: String, z: String) => Echo(x, y, z))

  ServerBuilder()
    .codec(RichHttp[Request](Http()))
    .bindTo(new InetSocketAddress(8080))
    .name("")
    .build(new HttpFilter(UnsafePermissivePolicy).andThen(module.toService))

  println("See the service description at: http://localhost:8080")
}

