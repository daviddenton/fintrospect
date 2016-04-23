package io.fintrospect.renderers

import com.twitter.finagle.http.Status
import io.fintrospect.formats.json.Json4s.Native.JsonFormat.{parse, string}
import io.fintrospect.parameters.Query
import io.fintrospect.renderers.JsonErrorResponseRenderer.{badRequest, notFound}
import io.fintrospect.util.HttpRequestResponseUtil.statusAndContentFrom
import org.scalatest.{FunSpec, ShouldMatchers}

class JsonErrorResponseRendererTest extends FunSpec with ShouldMatchers {

  it("can build 400") {
    val response = statusAndContentFrom(badRequest(Seq(Query.required.string("bob"))))
    response._1 shouldBe Status.BadRequest
    parse(response._2) \ "message" shouldBe string("Missing/invalid parameters")
  }

  it("can build 404") {
    val response = statusAndContentFrom(notFound())
    response._1 shouldBe Status.NotFound
    parse(response._2) \ "message" shouldBe string("No route found on this path. Have you used the correct HTTP verb?")
  }
}
