package io.fintrospect.util

import com.twitter.finagle.Service
import com.twitter.finagle.http.Status.{NotAcceptable, Ok}
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Await.result
import com.twitter.util.Future
import io.fintrospect.ContentType
import io.fintrospect.ContentTypes.{APPLICATION_ATOM_XML, APPLICATION_JSON, TEXT_HTML}
import io.fintrospect.formats.ResponseBuilder
import org.scalatest.{FunSpec, Matchers}

class StrictContentTypeNegotiationTest extends FunSpec with Matchers {

  describe("StrictContentTypeNegotiation") {
    it("on no match, return 406") {
      val r = result(StrictContentTypeNegotiation(serviceForType(APPLICATION_ATOM_XML))(requestWithAcceptHeaders(APPLICATION_JSON.value)))
      r.status shouldBe NotAcceptable
    }

    it("when there are no accept header set, just chooses the first type") {
      val r = result(StrictContentTypeNegotiation(serviceForType(APPLICATION_ATOM_XML), serviceForType(APPLICATION_JSON))(requestWithAcceptHeaders()))
      r.status shouldBe Ok
      r.headerMap("Content-Type") should startWith(APPLICATION_ATOM_XML.value)
    }

    it("when there is a wildcard set, just chooses the first type") {
      val r = result(StrictContentTypeNegotiation(serviceForType(APPLICATION_ATOM_XML), serviceForType(APPLICATION_JSON))(requestWithAcceptHeaders("*/*")))
      r.status shouldBe Ok
      r.headerMap("Content-Type") should startWith(APPLICATION_ATOM_XML.value)
    }

    it("when there is an exact (case insensitive) match on a content type, use that") {
      val r = result(StrictContentTypeNegotiation(serviceForType(APPLICATION_ATOM_XML), serviceForType(APPLICATION_JSON))(requestWithAcceptHeaders("apPliCation/jSoN")))
      r.status shouldBe Ok
      r.headerMap("Content-Type") should startWith(APPLICATION_JSON.value)
    }

    it("ignores q values and levels") {
      val r = result(StrictContentTypeNegotiation(serviceForType(TEXT_HTML), serviceForType(APPLICATION_JSON))
      (requestWithAcceptHeaders("text/*;q=0.3, text/html;q=0.7, text/html;level=1,application/bob;level=2;q=0.4, */*;q=0.5")))
      r.status shouldBe Ok
      r.headerMap("Content-Type") should startWith(TEXT_HTML.value)
    }
  }

  private def serviceForType(contentType: ContentType): (ContentType, Service[Request, Response]) =
    contentType -> Service.mk[Request, Response] { r => Future.value(ResponseBuilder.HttpResponse(contentType).build()) }


  private def requestWithAcceptHeaders(headers: String*): Request = {
    val request = Request()
    headers.foreach(value => request.headerMap.add("Accept", value))
    request
  }
}
