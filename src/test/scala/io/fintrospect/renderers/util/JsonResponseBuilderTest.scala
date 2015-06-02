package io.fintrospect.renderers.util

import com.twitter.io.Charsets._
import io.fintrospect.util.ArgoUtil._
import io.fintrospect.util.JsonResponseBuilder._
import org.jboss.netty.handler.codec.http.HttpResponseStatus
import org.jboss.netty.handler.codec.http.HttpResponseStatus.BAD_GATEWAY
import org.scalatest.{FunSpec, ShouldMatchers}

class JsonResponseBuilderTest extends FunSpec with ShouldMatchers {

  describe("JsonResponseBuilder") {

    it("builds Error from messages") {
      val error = Error(BAD_GATEWAY, "theMessage")
      error.getStatus shouldEqual BAD_GATEWAY
      parse(error.getContent.toString(Utf8)) shouldEqual obj("message" -> string("theMessage"))
    }

    it("builds Error from throwables") {
      val error = Error(BAD_GATEWAY, new RuntimeException("theMessage"))
      error.getStatus shouldEqual BAD_GATEWAY
      parse(error.getContent.toString(Utf8)) shouldEqual obj("message" -> string("theMessage"))
    }

    it("builds Ok with string") {
      val error = Ok("theMessage")
      error.getStatus shouldEqual HttpResponseStatus.OK
      error.getContent.toString(Utf8) shouldEqual "theMessage"
    }

    it("builds Ok with JsonRootNode") {
      val content = obj("okThing" -> string("theMessage"))
      val error = Ok(content)
      error.getStatus shouldEqual HttpResponseStatus.OK
      parse(error.getContent.toString(Utf8)) shouldEqual content
    }
  }
}
