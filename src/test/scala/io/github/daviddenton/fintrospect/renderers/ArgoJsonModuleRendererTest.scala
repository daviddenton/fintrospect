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

abstract class ArgoJsonModuleRendererTest() extends FunSpec with ShouldMatchers {
  def name: String = this.getClass.getSimpleName

  def renderer: ModuleRenderer

  describe(name) {
    it("renders as expected") {
      val module = FintrospectModule(Root / "basepath", renderer)
        .withRoute(
          DescribedRoute("some rambling description of what this thing actually does")
            .producing(APPLICATION_JSON)
            .taking(Header.optional.string("header", "description of the header"))
            .returning(ResponseWithExample(OK, "peachy", obj("anObject" -> obj("aStringField" -> number(123)))))
            .returning(FORBIDDEN -> "no way jose")
            .at(GET) / "echo" / string("message") bindTo ((s: String) => Echo(s)))
        .withRoute(
          DescribedRoute("a post endpoint")
            .consuming(APPLICATION_ATOM_XML, APPLICATION_SVG_XML)
            .producing(APPLICATION_JSON)
            .returning(FORBIDDEN -> "no way jose", obj("aString" -> ArgoUtil.string("a message of some kind")))
            .taking(Query.required.int("query"))
            .taking(Body.json(Some("the body of the message"), obj("anObject" -> obj("aStringField" -> number(123)))))
            .at(POST) / "echo" / string("message") bindTo ((s: String) => Echo(s)))
        .withRoute(
          DescribedRoute("a friendly endpoint")
            .taking(Query.required.boolean("query", "description of the query"))
            .taking(Form.required.int("form", "description of the form"))
            .at(GET) / "welcome" / string("firstName") / fixed("bertrand") / string("secondName") bindTo ((x: String, y: String, z: String) => Echo(x, y, z)))

      val expected = parse(Source.fromInputStream(this.getClass.getResourceAsStream(s"$name.json")).mkString)
      val actual = Await.result(module.toService(Request("/basepath"))).getContent.toString(Utf8)
//            println(actual)
      parse(actual) shouldEqual expected
    }
  }


}
