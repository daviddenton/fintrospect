package io.fintrospect.formats

import java.io.OutputStream
import java.nio.charset.Charset.defaultCharset

import com.twitter.finagle.http.Status
import com.twitter.io.{Buf, Bufs, Reader}
import com.twitter.util.Await
import io.fintrospect.ContentType
import io.fintrospect.util.HttpRequestResponseUtil.statusAndContentFrom
import org.jboss.netty.buffer.ChannelBuffers.copiedBuffer
import org.scalatest.{FunSpec, Matchers}

object LengthyIntResponseBuilder extends AbstractResponseBuilder[Int] {
  override def HttpResponse(): ResponseBuilder[Int] = new ResponseBuilder[Int](i => Buf.Utf8(i.toString), _.length, _.getMessage.length, ContentType("numbers"))
}

class AbstractResponseBuilderSpec extends FunSpec with Matchers {

  val bldr = LengthyIntResponseBuilder
  val message = "some text goes here"

  describe("abstract response builder") {

    it("ok with message - Buf") {
      statusAndContentFrom(bldr.Ok(Bufs.utf8Buf(message))) shouldBe(Status.Ok, message)
    }

    it("ok with message - Reader") {
      val reader = Reader.writable()
      val okBuilder = bldr.Ok(reader)
      reader.write(Bufs.utf8Buf(message)).ensure(reader.close())
      Await.result(okBuilder.reader.read(message.length)).map(Bufs.asUtf8String).get shouldBe message
    }

    it("ok with message - ChannelBuffer") {
      statusAndContentFrom(bldr.Ok(copiedBuffer(message, defaultCharset()))) shouldBe(Status.Ok, message)
    }

    it("builds Ok with custom type") {
      statusAndContentFrom(bldr.Ok(10)) shouldBe(Status.Ok, "10")
    }

    it("content - OutputStream") {
      statusAndContentFrom(bldr.HttpResponse().withContent((out: OutputStream) => out.write(message.getBytes()))) shouldBe(Status.Ok, message)
      val streamToUnit1 = (out: OutputStream) => out.write(message.getBytes())
      statusAndContentFrom(bldr.Ok(streamToUnit1)) shouldBe(Status.Ok, message)
    }

    it("content - String") {
      statusAndContentFrom(bldr.HttpResponse().withContent(message)) shouldBe(Status.Ok, message)
      statusAndContentFrom(bldr.Ok(message)) shouldBe(Status.Ok, message)
    }

    it("content - Buf") {
      statusAndContentFrom(bldr.HttpResponse().withContent(Bufs.utf8Buf(message))) shouldBe(Status.Ok, message)
      statusAndContentFrom(bldr.Ok(Bufs.utf8Buf(message))) shouldBe(Status.Ok, message)
    }

    it("content - ChannelBuffer") {
      statusAndContentFrom(bldr.HttpResponse().withContent(copiedBuffer(message, defaultCharset()))) shouldBe(Status.Ok, message)
      statusAndContentFrom(bldr.Ok(copiedBuffer(message, defaultCharset()))) shouldBe(Status.Ok, message)
    }

    it("error with code only") {
      statusAndContentFrom(bldr.NotFound(""))._1 shouldBe Status.NotFound
    }

    it("error with message - String") {
      statusAndContentFrom(bldr.NotFound(5)) shouldBe(Status.NotFound, 5.toString)
      statusAndContentFrom(bldr.NotFound(5)) shouldBe(Status.NotFound, 5.toString)
    }

    it("error with message - Buf") {
      statusAndContentFrom(bldr.NotFound(Buf.Utf8(5.toString))) shouldBe(Status.NotFound, 5.toString)
      statusAndContentFrom(bldr.NotFound(Buf.Utf8(5.toString))) shouldBe(Status.NotFound, 5.toString)
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
      statusAndContentFrom(bldr.HttpResponse(Status.BadGateway).withErrorMessage(message).build()) shouldBe(Status.BadGateway, message.length.toString)
      statusAndContentFrom(bldr.NotFound(message)) shouldBe(Status.NotFound, message.length.toString)
    }

    it("errors - exception") {
      statusAndContentFrom(bldr.HttpResponse(Status.BadGateway).withError(new RuntimeException(message))) shouldBe(Status.BadGateway, message.length.toString)
      statusAndContentFrom(bldr.NotFound(new RuntimeException(message))) shouldBe(Status.NotFound, message.length.toString)
    }
  }

}
