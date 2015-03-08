package io.github.daviddenton.fintrospect.renderers

import com.twitter.finagle.http.Request
import com.twitter.finagle.http.path.Root
import com.twitter.io.Charsets._
import com.twitter.util.Await
import io.github.daviddenton.fintrospect.parameters.Path._
import io.github.daviddenton.fintrospect.parameters._
import io.github.daviddenton.fintrospect.util.ArgoUtil._
import io.github.daviddenton.fintrospect.{On, Description, FintrospectModule}
import org.jboss.netty.handler.codec.http.HttpMethod._
import org.jboss.netty.handler.codec.http.HttpResponseStatus._
import org.scalatest.{FunSpec, ShouldMatchers}
import util.Echo

import scala.io.Source

abstract class JsonRendererTest(name: String, renderer: FintrospectModule.Renderer) extends FunSpec with ShouldMatchers {
  describe(name) {
    it("renders as expected") {
      val module = FintrospectModule(Root, renderer)
        .withRoute(
          Description("a get endpoint")
            .requiring(Header.string("header"))
            .returning(OK -> "peachy")
            .returning(FORBIDDEN -> "no way jose"),
          On(GET, _ / "echo"), string("message"), (s: String) => Echo(s))
        .withRoute(Description("a post endpoint").requiring(Query.int("query")), On(POST, _ / "echo"), string("message"), (s: String) => Echo(s))
        .withRoute(Description("a friendly endpoint").requiring(Query.boolean("query")), On(GET, _ / "welcome"), string("firstName"), fixed("bertrand"), string("secondName"), (x: String, y: String, z: String) => Echo(x, y, z))

      val expected = parse(Source.fromInputStream(renderer.getClass.getResourceAsStream(s"$name.json")).mkString)
      val actual = Await.result(module.toService(Request("/"))).content.toString(Utf8)
      //      println(actual)
      parse(actual) should be === expected
    }
  }


}
