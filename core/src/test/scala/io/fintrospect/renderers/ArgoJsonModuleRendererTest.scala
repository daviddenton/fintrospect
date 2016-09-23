package io.fintrospect.renderers

import com.twitter.finagle.http.Method.{Get, Post}
import com.twitter.finagle.http.path.Root
import com.twitter.finagle.http.{Request, Status}
import com.twitter.util.{Await, Future}
import io.fintrospect.ContentTypes.{APPLICATION_ATOM_XML, APPLICATION_JSON, APPLICATION_SVG_XML}
import io.fintrospect.formats.Argo
import io.fintrospect.formats.Argo.JsonFormat.{number, obj, parse}
import io.fintrospect.parameters.{Body, FormField, Header, Path, Query}
import io.fintrospect.util.HttpRequestResponseUtil.statusAndContentFrom
import io.fintrospect.util.{Echo, ExtractionError}
import io.fintrospect.{ApiKey, ModuleSpec, ResponseSpec, RouteSpec}
import org.scalatest.{FunSpec, Matchers}

import scala.io.Source

abstract class ArgoJsonModuleRendererTest() extends FunSpec with Matchers {
  def name: String = this.getClass.getSimpleName

  def renderer: ModuleRenderer

  describe(name) {
    it("renders as expected") {

      val customBody = Body.json(Option("the body of the message"), obj("anObject" -> obj("notAStringField" -> number(123))))

      val module = ModuleSpec(Root / "basepath", renderer)
        .securedBy(ApiKey(Header.required.string("the_api_key"), (_: String) => Future.value(true)))
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
            .at(Get) / "welcome" / Path.string("firstName") / "bertrand" / Path.string("secondName") bindTo ((x: String, y: String, z: String) => Echo(x, y, z)))

      val expected = parse(Source.fromInputStream(this.getClass.getResourceAsStream(s"$name.json")).mkString)

      val actual = Await.result(module.toService(Request("/basepath"))).contentString
      //                  println(actual)
      parse(actual) shouldBe expected
    }

    it("can build 400") {
      val response = statusAndContentFrom(renderer.badRequest(Seq(ExtractionError(Query.required.string("bob"), "missing"))))
      response._1 shouldBe Status.BadRequest
      parse(response._2).getStringValue("message") shouldBe "Missing/invalid parameters"
    }

    it("can build 404") {
      val response = statusAndContentFrom(renderer.notFound(Request()))
      response._1 shouldBe Status.NotFound
      parse(response._2).getStringValue("message") shouldBe "No route found on this path. Have you used the correct HTTP verb?"
    }

  }
}
