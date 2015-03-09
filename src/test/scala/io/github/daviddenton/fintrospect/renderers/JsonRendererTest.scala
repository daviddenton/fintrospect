package io.github.daviddenton.fintrospect.renderers

import argo.jdom.JsonRootNode
import com.twitter.finagle.http.Request
import com.twitter.finagle.http.path.Root
import com.twitter.io.Charsets._
import com.twitter.util.Await
import io.github.daviddenton.fintrospect.MimeTypes._
import io.github.daviddenton.fintrospect.parameters.Path._
import io.github.daviddenton.fintrospect.parameters._
import io.github.daviddenton.fintrospect.util.ArgoUtil._
import io.github.daviddenton.fintrospect._
import org.jboss.netty.handler.codec.http.HttpMethod._
import org.jboss.netty.handler.codec.http.HttpResponseStatus._
import org.scalatest.{FunSpec, ShouldMatchers}
import _root_.util.Echo

import scala.io.Source

abstract class JsonRendererTest(name: String, renderer: Seq[ModuleRoute] => JsonRootNode) extends FunSpec with ShouldMatchers {
  describe(name) {
    it("renders as expected") {
      val module = FintrospectModule(Root, renderer)
        .withRoute(
          Description("a get endpoint")
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
          Description("a friendly endpoint")
            .requiring(Query.boolean("query", "description of the query")),
          On(GET, _ / "welcome"), string("firstName"), fixed("bertrand"), string("secondName"), (x: String, y: String, z: String) => Echo(x, y, z))

      val expected = parse(Source.fromInputStream(renderer.getClass.getResourceAsStream(s"$name.json")).mkString)
      val actual = Await.result(module.toService(Request("/"))).content.toString(Utf8)
//            println(actual)
      parse(actual) should be === expected
    }
  }


}
