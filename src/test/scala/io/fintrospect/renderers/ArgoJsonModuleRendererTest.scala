package io.fintrospect.renderers

import _root_.util.Echo
import com.twitter.finagle.httpx.Method._
import com.twitter.finagle.httpx.path.Root
import com.twitter.finagle.httpx.{Request, Status}
import com.twitter.util.Await
import io.fintrospect.ContentTypes._
import io.fintrospect._
import io.fintrospect.formats.json.Argo
import io.fintrospect.formats.json.Argo.JsonFormat._
import io.fintrospect.parameters.Path._
import io.fintrospect.parameters._
import io.fintrospect.util.HttpRequestResponseUtil._
import org.scalatest.{FunSpec, ShouldMatchers}

import scala.io.Source

abstract class ArgoJsonModuleRendererTest() extends FunSpec with ShouldMatchers {
  def name: String = this.getClass.getSimpleName

  def renderer: ModuleRenderer

  describe(name) {
    it("renders as expected") {

      val customBody = Body.json(Option("the body of the message"), obj("anObject" -> obj("notAStringField" -> number(123))))

      val module = FintrospectModule(Root / "basepath", renderer)
        .withRoute(
          RouteSpec("summary of this route", "some rambling description of what this thing actually does")
            .producing(APPLICATION_JSON)
            .taking(Header.optional.string("header", "description of the header"))
            .returning(ResponseSpec.json(Status.Ok -> "peachy", obj("anAnotherObject" -> obj("aNumberField" -> number(123)))))
            .returning(Status.Forbidden -> "no way jose")
            .at(Get) / "echo" / Path.string("message") bindTo ((s: String) => Echo(s)))
        .withRoute(
          RouteSpec("a post endpoint")
            .consuming(APPLICATION_ATOM_XML, APPLICATION_SVG_XML)
            .producing(APPLICATION_JSON)
            .returning(ResponseSpec.json(Status.Forbidden -> "no way jose", obj("aString" -> Argo.JsonFormat.string("a message of some kind"))))
            .taking(Query.required.int("query"))
            .body(customBody)
            .at(Post) / "echo" / Path.string("message") bindTo ((s: String) => Echo(s)))
        .withRoute(
          RouteSpec("a friendly endpoint")
            .taking(Query.required.boolean("query", "description of the query"))
            .body(Body.form(FormField.required.int("form", "description of the form")))
            .at(Get) / "welcome" / Path.string("firstName") / fixed("bertrand") / Path.string("secondName") bindTo ((x: String, y: String, z: String) => Echo(x, y, z)))

      val expected = parse(Source.fromInputStream(this.getClass.getResourceAsStream(s"$name.json")).mkString)

      val actual = contentFrom(Await.result(module.toService(Request("/basepath"))))
                  println(actual)
      parse(actual) shouldEqual expected
    }
  }


}
