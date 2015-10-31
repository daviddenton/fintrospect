package io.fintrospect.formats

import java.nio.charset.Charset._

import com.twitter.finagle.http.{Response, Status}
import io.fintrospect.ContentType
import io.fintrospect.formats.text.PlainText
import org.jboss.netty.buffer.ChannelBuffers._
import org.scalatest.FunSpec
import org.scalatest.Matchers._

class ResponseBuilderTest extends FunSpec {

  it("should set the error correctly given Throwable") {
    val response: Response = new ResponseBuilder[PlainText](_.value, PlainText, e => PlainText(e.getMessage), ContentType("anyContentType"))
      .withError(new Throwable("Bad error")).build
    response.contentString shouldBe "Bad error"
  }

  it("should set the error correctly given error message") {
    val response: Response = new ResponseBuilder[PlainText](_.value, PlainText, e => PlainText(e.getMessage), ContentType("anyContentType"))
      .withErrorMessage("Bad error").build
    response.contentString shouldBe "Bad error"
  }

  it("should set the status code correctly") {
    val response: Response = new ResponseBuilder[PlainText](_.value, PlainText, e => PlainText(e.getMessage), ContentType("anyContentType"))
      .withCode(Status.Ok).build
    response.status shouldBe Status.Ok
  }

  it("should set the content correctly given content string") {
    val response: Response = new ResponseBuilder[PlainText](_.value, PlainText, e => PlainText(e.getMessage), ContentType("anyContentType"))
      .withContent("hello").build
    response.contentString shouldBe "hello"
  }

  it("should set the content correctly given channel buffer") {
    val response: Response = new ResponseBuilder[PlainText](_.value, PlainText, e => PlainText(e.getMessage), ContentType("anyContentType"))
      .withContent(copiedBuffer("hello", defaultCharset())).build
    response.contentString shouldBe "hello"
  }

  it("should set the content correctly when writing to output stream") {
    val response: Response = new ResponseBuilder[PlainText](_.value, PlainText, e => PlainText(e.getMessage), ContentType("anyContentType"))
      .withContent(out => out.write("hello".getBytes)).build
    response.contentString shouldBe "hello"
  }

  it("should set one header correctly") {
    val response: Response = new ResponseBuilder[PlainText](_.value, PlainText, e => PlainText(e.getMessage), ContentType("anyContentType"))
      .withHeaders("content_disposition" -> "attachment; filename=foo.txt").build
    response.headerMap("CONTENT_DISPOSITION") shouldEqual "attachment; filename=foo.txt"
  }

  it("should set multiple headers correctly") {
    val response: Response = new ResponseBuilder[PlainText](_.value, PlainText, e => PlainText(e.getMessage), ContentType("anyContentType"))
      .withHeaders("content_disposition" -> "attachment; filename=foo.txt",
        "authorization" -> "Authorization: Basic").build

    response.headerMap("CONTENT_DISPOSITION") shouldEqual "attachment; filename=foo.txt"
    response.headerMap("authorization") shouldEqual "Authorization: Basic"
  }

}
