package io.fintrospect.formats

import java.nio.charset.Charset.defaultCharset

import com.twitter.finagle.http.{Cookie, Status}
import com.twitter.io.Buf.Utf8
import com.twitter.io.Bufs.utf8Buf
import com.twitter.io.{Bufs, Reader}
import com.twitter.util.Await
import io.fintrospect.ContentType
import io.fintrospect.ContentTypes.TEXT_PLAIN
import io.fintrospect.formats.ResponseBuilder.HttpResponse
import io.fintrospect.util.HttpRequestResponseUtil.headerOf
import org.jboss.netty.buffer.ChannelBuffers.copiedBuffer
import org.scalatest.FunSpec
import org.scalatest.Matchers._

class ResponseBuilderTest extends FunSpec {

  private case class PlainTextValue(value: String)

  it("should set the error correctly with no type") {
    HttpResponse(TEXT_PLAIN).withError(new Throwable("Bad error")).build().contentString shouldBe "Bad error"
  }

  it("should set the content correctly with no type") {
    HttpResponse(TEXT_PLAIN).withContent("content").build().contentString shouldBe "content"
  }

  it("should set the error correctly given Throwable") {
    val response = new ResponseBuilder[PlainTextValue](i => Utf8(i.value), PlainTextValue, e => PlainTextValue(e.getMessage), ContentType("anyContentType"))
      .withError(new Throwable("Bad error")).build()
    response.contentString shouldBe "Bad error"
  }

  it("should set the error correctly given error message") {
    val response = new ResponseBuilder[PlainTextValue](i => Utf8(i.value), PlainTextValue, e => PlainTextValue(e.getMessage), ContentType("anyContentType"))
      .withErrorMessage("Bad error").build()
    response.contentString shouldBe "Bad error"
  }

  it("should set the status code correctly") {
    val response = new ResponseBuilder[PlainTextValue](i => Utf8(i.value), PlainTextValue, e => PlainTextValue(e.getMessage), ContentType("anyContentType"))
      .withCode(Status.Ok).build()
    response.status shouldBe Status.Ok
  }

  it("should set the content correctly given content string") {
    val response = new ResponseBuilder[PlainTextValue](i => Utf8(i.value), PlainTextValue, e => PlainTextValue(e.getMessage), ContentType("anyContentType"))
      .withContent("hello").build()
    response.contentString shouldBe "hello"
  }

  it("should set the content correctly given channel buffer") {
    val response = new ResponseBuilder[PlainTextValue](i => Utf8(i.value), PlainTextValue, e => PlainTextValue(e.getMessage), ContentType("anyContentType"))
      .withContent(copiedBuffer("hello", defaultCharset())).build()
    response.contentString shouldBe "hello"
  }

  it("should set cookies") {
    val response = new ResponseBuilder[PlainTextValue](i => Utf8(i.value), PlainTextValue, e => PlainTextValue(e.getMessage), ContentType("anyContentType"))
      .withCookies(new Cookie("name", "value")).build()

    response.cookies("name").value shouldBe "value"
  }

  it("should set the content correctly given reader and copies over all headers") {
    val reader = Reader.writable()
    val response = new ResponseBuilder[PlainTextValue](i => Utf8(i.value), PlainTextValue, e => PlainTextValue(e.getMessage), ContentType("anyContentType"))
      .withHeaders("bob" -> "rita", "bob" -> "rita2")
      .withContent(reader).build()
    reader.write(utf8Buf("hello")).ensure(reader.close())

    headerOf("bob")(response) shouldBe "rita, rita2"
    response.headerMap("Content-Type") shouldBe "anyContentType;charset=utf-8"
    Await.result(response.reader.read(5)).map(Bufs.asUtf8String).get shouldBe "hello"
  }

  it("should set the content correctly given Buf") {
    val response = new ResponseBuilder[PlainTextValue](i => Utf8(i.value), PlainTextValue, e => PlainTextValue(e.getMessage), ContentType("anyContentType"))
      .withContent(utf8Buf("hello")).build()
    response.contentString shouldBe "hello"
  }

  it("should set the content correctly when writing to output stream") {
    val response = new ResponseBuilder[PlainTextValue](i => Utf8(i.value), PlainTextValue, e => PlainTextValue(e.getMessage), ContentType("anyContentType"))
      .withContent(out => out.write("hello".getBytes)).build()
    response.contentString shouldBe "hello"
  }

  it("should set one header correctly") {
    val response = new ResponseBuilder[PlainTextValue](i => Utf8(i.value), PlainTextValue, e => PlainTextValue(e.getMessage), ContentType("anyContentType"))
      .withHeaders("content_disposition" -> "attachment; filename=foo.txt").build()
    response.headerMap("CONTENT_DISPOSITION") shouldBe "attachment; filename=foo.txt"
  }

  it("should set multiple headers correctly") {
    val response = new ResponseBuilder[PlainTextValue](i => Utf8(i.value), PlainTextValue, e => PlainTextValue(e.getMessage), ContentType("anyContentType"))
      .withHeaders("content_disposition" -> "attachment; filename=foo.txt",
        "authorization" -> "Authorization: Basic").build()

    response.headerMap("CONTENT_DISPOSITION") shouldBe "attachment; filename=foo.txt"
    response.headerMap("authorization") shouldBe "Authorization: Basic"
  }

}
