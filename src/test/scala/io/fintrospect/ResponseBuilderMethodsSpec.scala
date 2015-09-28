package io.fintrospect

import io.fintrospect.formats.ResponseBuilderMethods
import io.fintrospect.util.HttpRequestResponseUtil._
import org.jboss.netty.handler.codec.http.HttpResponseStatus._
import org.scalatest.{FunSpec, ShouldMatchers}

abstract class ResponseBuilderMethodsSpec[T](bldr: ResponseBuilderMethods[T]) extends FunSpec with ShouldMatchers {
  val message = "some text goes here"

  val expectedContent: String
  val expectedErrorContent: String
  val customType: T
  val customTypeSerialised: String

  describe("Rendering") {
    it("ok") {
      statusAndContentFrom(bldr.Ok) shouldEqual(OK, "")
    }

    it("ok with message") {
      statusAndContentFrom(bldr.Ok(message)) shouldEqual(OK, expectedContent)
    }

    it("content") {
      statusAndContentFrom(bldr.Response().withContent(message).build) shouldEqual(OK, expectedContent)
    }

    it("errors - message") {
      statusAndContentFrom(bldr.Response(BAD_GATEWAY).withErrorMessage(message).build) shouldEqual(BAD_GATEWAY, expectedErrorContent)
    }

    it("errors - exception") {
      statusAndContentFrom(bldr.Response(BAD_GATEWAY).withError(new RuntimeException(message)).build) shouldEqual(BAD_GATEWAY, expectedErrorContent)
    }

    it("builds Ok with custom type") {
      statusAndContentFrom(bldr.Ok(customType)) shouldEqual (OK, customTypeSerialised)
    }
  }

}
