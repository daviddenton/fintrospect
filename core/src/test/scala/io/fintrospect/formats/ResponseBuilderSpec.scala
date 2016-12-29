package io.fintrospect.formats

import java.io.OutputStream
import java.nio.charset.Charset.defaultCharset

import com.twitter.finagle.http.Status
import com.twitter.io.{Buf, Bufs, Reader}
import com.twitter.util.Await
import io.fintrospect.util.HttpRequestResponseUtil.statusAndContentFrom
import org.jboss.netty.buffer.ChannelBuffers.copiedBuffer
import org.scalatest.{FunSpec, Matchers}

abstract class ResponseBuilderSpec[T](bldr: AbstractResponseBuilder[T]) extends FunSpec with Matchers {

  val message = "some text goes here"

  val expectedContent: String
  val expectedErrorContent: String
  val customError: T
  val customType: T
  val customTypeSerialized: String

  describe("Rendering") {
    it("ok with message - String") {
      statusAndContentFrom(bldr.Ok(message)) shouldBe(Status.Ok, expectedContent)
    }

    it("ok with message - Buf") {
      statusAndContentFrom(bldr.Ok(Bufs.utf8Buf(message))) shouldBe(Status.Ok, expectedContent)
    }

    it("ok with message - Reader") {
      val reader = Reader.writable()
      val okBuilder = bldr.Ok(reader)
      reader.write(Bufs.utf8Buf(message)).ensure(reader.close())
      Await.result(okBuilder.reader.read(message.length)).map(Bufs.asUtf8String).get shouldBe message
    }

    it("ok with message - ChannelBuffer") {
      statusAndContentFrom(bldr.Ok(copiedBuffer(message, defaultCharset()))) shouldBe(Status.Ok, expectedContent)
    }

    it("builds Ok with custom type") {
      statusAndContentFrom(bldr.Ok(customType)) shouldBe(Status.Ok, customTypeSerialized)
    }

    it("content - OutputStream") {
      statusAndContentFrom(bldr.HttpResponse().withContent((out: OutputStream) => out.write(expectedContent.getBytes()))) shouldBe(Status.Ok, expectedContent)
      val streamToUnit1 = (out: OutputStream) => out.write(expectedContent.getBytes())
      statusAndContentFrom(bldr.Ok(streamToUnit1)) shouldBe(Status.Ok, expectedContent)
    }

    it("content - String") {
      statusAndContentFrom(bldr.HttpResponse().withContent(message)) shouldBe(Status.Ok, expectedContent)
      statusAndContentFrom(bldr.Ok(message)) shouldBe(Status.Ok, expectedContent)
    }

    it("content - Buf") {
      statusAndContentFrom(bldr.HttpResponse().withContent(Bufs.utf8Buf(message))) shouldBe(Status.Ok, expectedContent)
      statusAndContentFrom(bldr.Ok(Bufs.utf8Buf(message))) shouldBe(Status.Ok, expectedContent)
    }

    it("content - ChannelBuffer") {
      statusAndContentFrom(bldr.HttpResponse().withContent(copiedBuffer(message, defaultCharset()))) shouldBe(Status.Ok, expectedContent)
      statusAndContentFrom(bldr.Ok(copiedBuffer(message, defaultCharset()))) shouldBe(Status.Ok, expectedContent)
    }

    it("error with code only") {
      statusAndContentFrom(bldr.NotFound(""))._1 shouldBe Status.NotFound
    }

    it("error with message - String") {
      statusAndContentFrom(bldr.NotFound(customError)) shouldBe(Status.NotFound, expectedErrorContent)
      statusAndContentFrom(bldr.NotFound(customError)) shouldBe(Status.NotFound, expectedErrorContent)
    }

    it("error with message - Buf") {
      statusAndContentFrom(bldr.NotFound(Buf.Utf8(expectedErrorContent))) shouldBe(Status.NotFound, expectedErrorContent)
      statusAndContentFrom(bldr.NotFound(Buf.Utf8(expectedErrorContent))) shouldBe(Status.NotFound, expectedErrorContent)
    }

    //    it("error with message - Reader") {
    //      statusAndContentFrom(bldr.NotFound(Reader.fromBuf(Buf.Utf8(expectedErrorContent)))) shouldBe(Status.NotFound, expectedErrorContent)
    //      statusAndContentFrom(NotFound(Reader.fromBuf(Buf.Utf8(expectedErrorContent)))) shouldBe(Status.NotFound, expectedErrorContent)
    //    }

    it("error with message - ChannelBuffer") {
      statusAndContentFrom(bldr.NotFound(copiedBuffer(message, defaultCharset()))) shouldBe(Status.NotFound, message)
      statusAndContentFrom(bldr.NotFound(copiedBuffer(message, defaultCharset()))) shouldBe(Status.NotFound, message)
    }

    it("errors - message") {
      statusAndContentFrom(bldr.HttpResponse(Status.BadGateway).withErrorMessage(message).build()) shouldBe(Status.BadGateway, expectedErrorContent)
      statusAndContentFrom(bldr.NotFound(message)) shouldBe(Status.NotFound, expectedErrorContent)
    }

    it("errors - exception") {
      statusAndContentFrom(bldr.HttpResponse(Status.BadGateway).withError(new RuntimeException(message))) shouldBe(Status.BadGateway, expectedErrorContent)
      statusAndContentFrom(bldr.NotFound(new RuntimeException(message))) shouldBe(Status.NotFound, expectedErrorContent)
    }

  }

}
