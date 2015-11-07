package io.fintrospect.formats

import java.nio.charset.Charset._

import com.twitter.finagle.http.Status
import com.twitter.io.Bufs
import io.fintrospect.util.HttpRequestResponseUtil._
import org.jboss.netty.buffer.ChannelBuffers._
import org.scalatest.{FunSpec, ShouldMatchers}

abstract class ResponseBuilderMethodsSpec[T](bldr: ResponseBuilderMethods[T]) extends FunSpec with ShouldMatchers {
  val message = "some text goes here"

  val expectedContent: String
  val expectedErrorContent: String
  val customError: T
  val customType: T
  val customTypeSerialised: String

  describe("Rendering") {
    it("ok") {
      statusAndContentFrom(bldr.OK) shouldEqual(Status.Ok, "")
    }

    it("ok with message - String") {
      statusAndContentFrom(bldr.OK(message)) shouldEqual(Status.Ok, expectedContent)
    }

    it("ok with message - Buf") {
      statusAndContentFrom(bldr.OK(Bufs.utf8Buf(message))) shouldEqual(Status.Ok, expectedContent)
    }

    it("ok with message - ChannelBuffer") {
      statusAndContentFrom(bldr.OK(copiedBuffer(message, defaultCharset()))) shouldEqual(Status.Ok, expectedContent)
    }

    it("error with message - String") {
      statusAndContentFrom(bldr.Error(Status.NotFound, message)) shouldEqual(Status.NotFound, expectedErrorContent)
    }

    it("error with message - Buf") {
      statusAndContentFrom(bldr.Error(Status.NotFound, customError)) shouldEqual(Status.NotFound, expectedErrorContent)
    }

    it("error with message - ChannelBuffer") {
      statusAndContentFrom(bldr.Error(Status.NotFound, new RuntimeException(message))) shouldEqual(Status.NotFound, expectedErrorContent)
    }

    it("content - String") {
      statusAndContentFrom(bldr.HttpResponse().withContent(message).build) shouldEqual(Status.Ok, expectedContent)
    }

    it("content - Buf") {
      statusAndContentFrom(bldr.HttpResponse().withContent(Bufs.utf8Buf(message)).build) shouldEqual(Status.Ok, expectedContent)
    }

    it("content - ChannelBuffer") {
      statusAndContentFrom(bldr.HttpResponse().withContent(copiedBuffer(message, defaultCharset())).build) shouldEqual(Status.Ok, expectedContent)
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
