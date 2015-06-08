package io.fintrospect.renderers.util

import io.fintrospect.util.ArgoUtil._
import io.fintrospect.util.HttpRequestResponseUtil._
import io.fintrospect.util.JsonResponseBuilder._
import org.jboss.netty.handler.codec.http.HttpResponseStatus._
import org.scalatest.{FunSpec, ShouldMatchers}

class JsonResponseBuilderTest extends FunSpec with ShouldMatchers {

  describe("JsonResponseBuilder") {

    it("builds Error from messages") {
      val error = Error(BAD_GATEWAY, "theMessage")
      error.getStatus shouldEqual BAD_GATEWAY
      parse(contentFrom(error)) shouldEqual obj("message" -> string("theMessage"))
    }

    it("builds Error from throwables") {
      val error = Error(BAD_GATEWAY, new RuntimeException("theMessage"))
      error.getStatus shouldEqual BAD_GATEWAY
      parse(contentFrom(error)) shouldEqual obj("message" -> string("theMessage"))
    }

    it("builds Ok with string") {
      statusAndContentFrom(Ok("theMessage")) shouldEqual (OK, "theMessage")
    }

    it("builds Ok with JsonRootNode") {
      val content = obj("okThing" -> string("theMessage"))
      val ok = Ok(content)
      ok.getStatus shouldEqual OK
      parse(contentFrom(ok)) shouldEqual content
    }
  }
}
