package io.fintrospect.renderers

import _root_.util.Echo
import com.twitter.finagle.http.Request
import com.twitter.finagle.http.path.Root
import com.twitter.util.Await
import io.fintrospect.ContentTypes._
import io.fintrospect._
import io.fintrospect.parameters.Path._
import io.fintrospect.parameters._
import io.fintrospect.util.ArgoUtil
import io.fintrospect.util.ArgoUtil.{number, obj, parse}
import io.fintrospect.util.HttpRequestResponseUtil._
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
            .taking(Header.optional(ParameterSpec.string("header", "description of the header")))
            .returning(ResponseWithExample(OK, "peachy", obj("anObject" -> obj("aStringField" -> number(123)))))
            .returning(FORBIDDEN -> "no way jose")
            .at(GET) / "echo" / Path(ParameterSpec.string("message")) bindTo ((s: String) => Echo(s)))
        .withRoute(
          DescribedRoute("a post endpoint")
            .consuming(APPLICATION_ATOM_XML, APPLICATION_SVG_XML)
            .producing(APPLICATION_JSON)
            .returning(FORBIDDEN -> "no way jose", obj("aString" -> ArgoUtil.string("a message of some kind")))
            .taking(Query.required(ParameterSpec.int("query")))
            .body(Body.json(Some("the body of the message"), obj("anObject" -> obj("aStringField" -> number(123)))))
            .at(POST) / "echo" / Path(ParameterSpec.string("message")) bindTo ((s: String) => Echo(s)))
        .withRoute(
          DescribedRoute("a friendly endpoint")
            .taking(Query.required(ParameterSpec.boolean("query", "description of the query")))
            .body(Body.form(FormField.required(ParameterSpec.int("form", "description of the form"))))
            .at(GET) / "welcome" / Path(ParameterSpec.string("firstName")) / fixed("bertrand") / Path(ParameterSpec.string("secondName")) bindTo ((x: String, y: String, z: String) => Echo(x, y, z)))

      val expected = parse(Source.fromInputStream(this.getClass.getResourceAsStream(s"$name.json")).mkString)

      val actual = contentFrom(Await.result(module.toService(Request("/basepath"))))
//                  println(actual)
      parse(actual) shouldEqual expected
    }
  }


}
