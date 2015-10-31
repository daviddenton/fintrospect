package io.fintrospect.formats

import com.twitter.finagle.http.Status
import io.fintrospect.util.HttpRequestResponseUtil._
import org.scalatest.{FunSpec, ShouldMatchers}

abstract class ResponseBuilderMethodsSpec[T](bldr: ResponseBuilderMethods[T]) extends FunSpec with ShouldMatchers {
  val message = "some text goes here"

  val expectedContent: String
  val expectedErrorContent: String
  val customType: T
  val customTypeSerialised: String

  describe("Rendering") {
    it("ok") {
      statusAndContentFrom(bldr.OK) shouldEqual(Status.Ok, "")
      statusAndContentFrom(bldr.OK) shouldEqual(Status.Ok, "")
    }

    it("ok with message") {
      statusAndContentFrom(bldr.OK(message)) shouldEqual(Status.Ok, expectedContent)
      statusAndContentFrom(bldr.OK(message)) shouldEqual(Status.Ok, expectedContent)
    }

    it("content") {
      statusAndContentFrom(bldr.HttpResponse().withContent(message).build) shouldEqual(Status.Ok, expectedContent)
    }

    it("errors - message") {
      statusAndContentFrom(bldr.HttpResponse(Status.BadGateway).withErrorMessage(message).build) shouldEqual(Status.BadGateway, expectedErrorContent)
    }

    it("errors - exception") {
      statusAndContentFrom(bldr.HttpResponse(Status.BadGateway).withError(new RuntimeException(message)).build) shouldEqual(Status.BadGateway, expectedErrorContent)
    }

    it("builds Ok with custom type") {
      statusAndContentFrom(bldr.OK(customType)) shouldEqual (Status.Ok, customTypeSerialised)
    }
  }

}
