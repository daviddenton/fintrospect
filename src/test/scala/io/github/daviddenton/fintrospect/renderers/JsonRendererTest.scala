package io.github.daviddenton.fintrospect.renderers

import _root_.util.Echo
import com.twitter.finagle.http.Request
import com.twitter.finagle.http.path.Root
import com.twitter.io.Charsets._
import com.twitter.util.Await
import io.github.daviddenton.fintrospect.ContentTypes._
import io.github.daviddenton.fintrospect._
import io.github.daviddenton.fintrospect.parameters.Path._
import io.github.daviddenton.fintrospect.parameters._
import io.github.daviddenton.fintrospect.util.ArgoUtil
import io.github.daviddenton.fintrospect.util.ArgoUtil.{number, obj, parse}
import org.jboss.netty.handler.codec.http.HttpMethod._
import org.jboss.netty.handler.codec.http.HttpResponseStatus._
import org.scalatest.{FunSpec, ShouldMatchers}

import scala.io.Source

abstract class JsonRendererTest() extends FunSpec with ShouldMatchers {
  def name: String

  def renderer: Renderer

  describe(name) {
    it("renders as expected") {
      val module = FintrospectModule2(Root / "basepath", renderer)
        .withRoute(
          Description("a get endpoint", "some rambling description of what this thing actually does")
            .producing(APPLICATION_JSON)
            .taking(Header.optional.string("header", "description of the header"))
            .returning(ResponseWithExample(OK, "peachy", obj("anObject" -> obj("aStringField" -> number(123)))))
            .returning(FORBIDDEN -> "no way jose")
            .at(GET) / "echo" / string("message") then ((s: String) => Echo(s)))
        .withRoute(
          Description("a post endpoint")
            .consuming(APPLICATION_ATOM_XML, APPLICATION_SVG_XML)
            .producing(APPLICATION_JSON)
            .returning(FORBIDDEN -> "no way jose", obj("aString" -> ArgoUtil.string("a message of some kind")))
            .taking(Query.required.int("query"))
            .taking(Body.json(Some("the body of the message"), obj("anObject" -> obj("aStringField" -> number(123)))))
            .at(POST) / "echo" / string("message") then ((s: String) => Echo(s)))
        .withRoute(
          Description("a friendly endpoint")
            .taking(Query.required.boolean("query", "description of the query"))
            .at(GET) / "welcome" / string("firstName") / fixed("bertrand") / string("secondName") then ((x: String, y: String, z: String) => Echo(x, y, z)))

      val expected = parse(Source.fromInputStream(renderer.getClass.getResourceAsStream(s"$name.json")).mkString)
      val actual = Await.result(module.toService(Request("/basepath"))).content.toString(Utf8)
      //      println(actual)
      parse(actual) should be === expected
    }
  }


}
