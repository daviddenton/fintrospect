package io.fintrospect.util

import com.twitter.finagle.Service
import com.twitter.finagle.http.Status.Ok
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.util.Await.result
import com.twitter.util.Future
import io.fintrospect.ContentType
import io.fintrospect.ContentTypes.APPLICATION_JSON
import io.fintrospect.parameters.Body
import org.scalatest.{FunSpec, Matchers}

class MultiBodyTypeTest extends FunSpec with Matchers {

  val xmlBody = Body.xml(None)
  val xmlAccepting = MultiBodyType(xmlBody -> Service.mk { req: Request => Future.value(Response(Ok)) })

  describe("MultiBodyType") {
    it("on no match, reject with 415") {
      val r = result(xmlAccepting(requestFromContentType(APPLICATION_JSON)))
      r.status shouldBe Status.UnsupportedMediaType
    }

    it("when there are no content-type header set, reject with 415") {
      val r = result(xmlAccepting(requestFromContentType()))
      r.status shouldBe Status.UnsupportedMediaType
    }

    it("when there is an exact (case insensitive) match on a content type, use that service") {
      val r = Request()
      r.headerMap("Content-Type") = "application/xml"
      r.contentString = "<xml/>"
      val resp = result(xmlAccepting(r))
      resp.status shouldBe Ok
    }
  }

  def requestFromContentType(headers: ContentType*): Request = {
    val request = Request()
    headers.foreach(value => request.headerMap.add("Content-Type", value.value))
    request
  }
}
