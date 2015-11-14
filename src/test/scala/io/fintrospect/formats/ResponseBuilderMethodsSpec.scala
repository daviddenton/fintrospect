package io.fintrospect.formats

import java.nio.charset.Charset._

import com.twitter.finagle.http.Status._
import com.twitter.io.Bufs
import io.fintrospect.util.HttpRequestResponseUtil._
import org.jboss.netty.buffer.ChannelBuffers._
import org.scalatest.{FunSpec, ShouldMatchers}

abstract class ResponseBuilderMethodsSpec[T](bldr: ResponseBuilderMethods[T]) extends FunSpec with ShouldMatchers {
  import bldr._

  val message = "some text goes here"

  val expectedContent: String
  val expectedErrorContent: String
  val customError: T
  val customType: T
  val customTypeSerialised: String

  describe("Rendering") {
    it("ok") {
      statusAndContentFrom(bldr.OK) shouldEqual(Ok, "")
      statusAndContentFrom(Ok()) shouldEqual(Ok, "")
    }

    it("ok with message - String") {
      statusAndContentFrom(bldr.OK(message)) shouldEqual(Ok, expectedContent)
      statusAndContentFrom(Ok(message)) shouldEqual(Ok, expectedContent)
    }

    it("ok with message - Buf") {
      statusAndContentFrom(bldr.OK(Bufs.utf8Buf(message))) shouldEqual(Ok, expectedContent)
      statusAndContentFrom(Ok(Bufs.utf8Buf(message))) shouldEqual(Ok, expectedContent)
    }

    it("ok with message - ChannelBuffer") {
      statusAndContentFrom(bldr.OK(copiedBuffer(message, defaultCharset()))) shouldEqual(Ok, expectedContent)
      statusAndContentFrom(Ok(copiedBuffer(message, defaultCharset()))) shouldEqual(Ok, expectedContent)
    }

    it("builds Ok with custom type") {
      statusAndContentFrom(bldr.OK(customType)) shouldEqual (Ok, customTypeSerialised)
      statusAndContentFrom(Ok(customType)) shouldEqual (Ok, customTypeSerialised)
    }

    it("content - String") {
      statusAndContentFrom(bldr.HttpResponse().withContent(message)) shouldEqual(Ok, expectedContent)
    }

    it("content - Buf") {
      statusAndContentFrom(bldr.HttpResponse().withContent(Bufs.utf8Buf(message))) shouldEqual(Ok, expectedContent)
    }

    it("content - ChannelBuffer") {
      statusAndContentFrom(bldr.HttpResponse().withContent(copiedBuffer(message, defaultCharset()))) shouldEqual(Ok, expectedContent)
    }

    it("error with message - Buf") {
      statusAndContentFrom(bldr.Error(NotFound, customError)) shouldEqual(NotFound, expectedErrorContent)
      statusAndContentFrom(NotFound(customError)) shouldEqual(NotFound, expectedErrorContent)
    }

    it("error with message - ChannelBuffer") {
      statusAndContentFrom(bldr.Error(NotFound, copiedBuffer(message, defaultCharset()))) shouldEqual(NotFound, message)
      statusAndContentFrom(NotFound(copiedBuffer(message, defaultCharset()))) shouldEqual(NotFound, message)
    }

    it("errors - message") {
      statusAndContentFrom(bldr.HttpResponse(BadGateway).withErrorMessage(message).build) shouldEqual(BadGateway, expectedErrorContent)
      statusAndContentFrom(bldr.Error(NotFound, message)) shouldEqual(NotFound, expectedErrorContent)
      statusAndContentFrom(NotFound(message)) shouldEqual(NotFound, expectedErrorContent)
    }

    it("errors - exception") {
      statusAndContentFrom(bldr.HttpResponse(BadGateway).withError(new RuntimeException(message))) shouldEqual(BadGateway, expectedErrorContent)
      statusAndContentFrom(bldr.Error(NotFound, new RuntimeException(message))) shouldEqual(NotFound, expectedErrorContent)
      statusAndContentFrom(BadGateway(new RuntimeException(message))) shouldEqual(BadGateway, expectedErrorContent)
    }

  }

}
