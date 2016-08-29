package io.fintrospect.formats

import java.io.OutputStream
import java.nio.charset.Charset.defaultCharset

import com.twitter.finagle.http.Status.{BadGateway, NotFound, Ok}
import com.twitter.io.{Buf, Bufs, Reader}
import com.twitter.util.Await
import io.fintrospect.util.HttpRequestResponseUtil.statusAndContentFrom
import org.jboss.netty.buffer.ChannelBuffers.copiedBuffer
import org.scalatest.{FunSpec, Matchers}

abstract class AbstractResponseBuilderSpec[T](bldr: AbstractResponseBuilder[T]) extends FunSpec with Matchers {

  import bldr.implicits.{responseBuilderToResponse, statusToResponseBuilderConfig}

  val message = "some text goes here"

  val expectedContent: String
  val expectedErrorContent: String
  val customError: T
  val customType: T
  val customTypeSerialised: String

  describe("Rendering") {
    it("ok") {
      statusAndContentFrom(bldr.OK) shouldBe(Ok, "")
      statusAndContentFrom(Ok()) shouldBe(Ok, "")
    }

    it("ok with message - String") {
      statusAndContentFrom(bldr.OK(message)) shouldBe(Ok, expectedContent)
      statusAndContentFrom(Ok(message)) shouldBe(Ok, expectedContent)
    }

    it("ok with message - Buf") {
      statusAndContentFrom(bldr.OK(Bufs.utf8Buf(message))) shouldBe(Ok, expectedContent)
      statusAndContentFrom(Ok(Bufs.utf8Buf(message))) shouldBe(Ok, expectedContent)
    }

    it("ok with message - Reader") {
      {
        val reader = Reader.writable()
        val okBuilder = bldr.OK(reader)
        reader.write(Bufs.utf8Buf(message)).ensure(reader.close())
        Await.result(okBuilder.reader.read(message.length)).map(Bufs.asUtf8String).get shouldBe message
      }
      {
        val reader = Reader.writable()
        val okBuilder = Ok(reader)
        reader.write(Bufs.utf8Buf(message)).ensure(reader.close())
        Await.result(okBuilder.reader.read(message.length)).map(Bufs.asUtf8String).get shouldBe message
      }
    }

    it("ok with message - ChannelBuffer") {
      statusAndContentFrom(bldr.OK(copiedBuffer(message, defaultCharset()))) shouldBe(Ok, expectedContent)
      statusAndContentFrom(Ok(copiedBuffer(message, defaultCharset()))) shouldBe(Ok, expectedContent)
    }

    it("builds Ok with custom type") {
      statusAndContentFrom(bldr.OK(customType)) shouldBe(Ok, customTypeSerialised)
      statusAndContentFrom(Ok(customType)) shouldBe(Ok, customTypeSerialised)
    }

    it("content - OutputStream") {
      statusAndContentFrom(bldr.HttpResponse().withContent((out:OutputStream) => out.write(expectedContent.getBytes()))) shouldBe(Ok, expectedContent)
      statusAndContentFrom(Ok((out:OutputStream) => out.write(expectedContent.getBytes()))) shouldBe(Ok, expectedContent)
    }

    it("content - String") {
      statusAndContentFrom(bldr.HttpResponse().withContent(message)) shouldBe(Ok, expectedContent)
      statusAndContentFrom(Ok(message)) shouldBe(Ok, expectedContent)
    }

    it("content - Buf") {
      statusAndContentFrom(bldr.HttpResponse().withContent(Bufs.utf8Buf(message))) shouldBe(Ok, expectedContent)
      statusAndContentFrom(Ok(Bufs.utf8Buf(message))) shouldBe(Ok, expectedContent)
    }

    it("content - ChannelBuffer") {
      statusAndContentFrom(bldr.HttpResponse().withContent(copiedBuffer(message, defaultCharset()))) shouldBe(Ok, expectedContent)
      statusAndContentFrom(Ok(copiedBuffer(message, defaultCharset()))) shouldBe(Ok, expectedContent)
    }

    it("error with code only") {
      statusAndContentFrom(bldr.Error(NotFound))._1 shouldBe NotFound
    }

    it("error with message - String") {
      statusAndContentFrom(bldr.Error(NotFound, customError)) shouldBe(NotFound, expectedErrorContent)
      statusAndContentFrom(NotFound(customError)) shouldBe(NotFound, expectedErrorContent)
    }

    it("error with message - Buf") {
      statusAndContentFrom(bldr.Error(NotFound, Buf.Utf8(expectedErrorContent))) shouldBe(NotFound, expectedErrorContent)
      statusAndContentFrom(NotFound(Buf.Utf8(expectedErrorContent))) shouldBe(NotFound, expectedErrorContent)
    }

//    it("error with message - Reader") {
//      statusAndContentFrom(bldr.Error(NotFound, Reader.fromBuf(Buf.Utf8(expectedErrorContent)))) shouldBe(NotFound, expectedErrorContent)
//      statusAndContentFrom(NotFound(Reader.fromBuf(Buf.Utf8(expectedErrorContent)))) shouldBe(NotFound, expectedErrorContent)
//    }

    it("error with message - ChannelBuffer") {
      statusAndContentFrom(bldr.Error(NotFound, copiedBuffer(message, defaultCharset()))) shouldBe(NotFound, message)
      statusAndContentFrom(NotFound(copiedBuffer(message, defaultCharset()))) shouldBe(NotFound, message)
    }

    it("errors - message") {
      statusAndContentFrom(bldr.HttpResponse(BadGateway).withErrorMessage(message).build()) shouldBe(BadGateway, expectedErrorContent)
      statusAndContentFrom(bldr.Error(NotFound, message)) shouldBe(NotFound, expectedErrorContent)
      statusAndContentFrom(NotFound(message)) shouldBe(NotFound, expectedErrorContent)
    }

    it("errors - exception") {
      statusAndContentFrom(bldr.HttpResponse(BadGateway).withError(new RuntimeException(message))) shouldBe(BadGateway, expectedErrorContent)
      statusAndContentFrom(bldr.Error(NotFound, new RuntimeException(message))) shouldBe(NotFound, expectedErrorContent)
      statusAndContentFrom(BadGateway(new RuntimeException(message))) shouldBe(BadGateway, expectedErrorContent)
    }

  }

}
