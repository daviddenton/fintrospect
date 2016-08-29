package io.fintrospect

import com.twitter.finagle.Service
import com.twitter.finagle.http.Status.{Ok, Unauthorized}
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Await.result
import com.twitter.util.Future
import io.fintrospect.formats.PlainText.ResponseBuilder.implicits.statusToResponseBuilderConfig
import io.fintrospect.parameters.Query
import io.fintrospect.util.HttpRequestResponseUtil.statusAndContentFrom
import org.scalatest.{FunSpec, Matchers}

class SecurityTest extends FunSpec with Matchers {

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
