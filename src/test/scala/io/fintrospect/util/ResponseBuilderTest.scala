package io.fintrospect.util

import com.twitter.finagle.http.Response
import io.fintrospect.ContentType
import org.scalatest.FunSpec

class ResponseBuilderTest extends FunSpec {

  it("should set one header correctly") {
    val response: Response = new ResponseBuilder[PlainText](_.value, PlainText, e => PlainText(e.getMessage), ContentType("anyContentType"))
      .withHeaders("content_disposition" -> "attachment; filename=foo.txt").build
    assert(response.headers().contains("CONTENT_DISPOSITION", "attachment; filename=foo.txt", false), "headers not set correctly")
  }

  it("should set multiple headers correctly") {
    val response: Response = new ResponseBuilder[PlainText](_.value, PlainText, e => PlainText(e.getMessage), ContentType("anyContentType"))
      .withHeaders("content_disposition" -> "attachment; filename=foo.txt",
                   "content_disposition" -> "attachment; filename=bar.txt",
                   "authorization" -> "Authorization: Basic").build
    assert(response.headers().contains("CONTENT_DISPOSITION", "attachment; filename=foo.txt", false), "headers not set correctly")
    assert(response.headers().contains("CONTENT_DISPOSITION", "attachment; filename=bar.txt", false), "headers not set correctly")
    assert(response.headers().contains("authorization", "Authorization: Basic", false), "headers not set correctly")
  }

}
