package io.fintrospect.parameters

import com.twitter.finagle.Service
import com.twitter.finagle.http.Status._
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Await._
import com.twitter.util.Future
import io.fintrospect.formats.PlainText.ResponseBuilder._
import io.fintrospect.util.HttpRequestResponseUtil._
import org.scalatest.{FunSpec, ShouldMatchers}

class SecurityTest extends FunSpec with ShouldMatchers {

  describe("ApiKey") {
    val paramName = "name"
    val param = Query.required.int(paramName)
    val next = Service.mk[Request, Response](r => Ok("hello"))

    it("valid API key is granted access and result carried through") {
      val (status, content) =
        result(ApiKey(param, (i: Int) => Future.value(true)).filter(Request(paramName -> "1"), next)
          .map(statusAndContentFrom))

      status should be(Ok)
      content should be("hello")
    }

    it("missing API key is unauthorized") {
      val (status, content) =
        result(ApiKey(param, (i: Int) => Future.value(true)).filter(Request(), next)
          .map(statusAndContentFrom))

      status should be(Unauthorized)
      content should be("")
    }

    it("bad API key is unauthorized") {
      val (status, content) =
        result(ApiKey(param, (i: Int) => Future.value(true)).filter(Request(paramName -> "notAnInt"), next)
          .map(statusAndContentFrom))

      status should be(Unauthorized)
      content should be("")
    }

    it("unknown API key is unauthorized") {
      val (status, content) =
        result(ApiKey(param, (i: Int) => Future.value(false)).filter(Request(paramName -> "1"), next)
          .map(statusAndContentFrom))

      status should be(Unauthorized)
      content should be("")
    }

    it("failed API key lookup is rethrown") {
      val e = new RuntimeException("boom")
      val caught = intercept[RuntimeException](result(ApiKey(param, (i: Int) => Future.exception(e)).filter(Request(paramName -> "1"), next)))
      caught should be(e)
    }
  }
}
