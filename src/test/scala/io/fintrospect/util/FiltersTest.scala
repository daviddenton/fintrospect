package io.fintrospect.util

import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Await.result
import com.twitter.util.Future
import io.fintrospect.configuration.{Authority, Credentials, Host, Port}
import io.fintrospect.util.Filters.{addAuthorityHost, addBasicAuthorization}
import org.scalatest.{FunSpec, ShouldMatchers}

class FiltersTest extends FunSpec with ShouldMatchers {

  describe("Add authority host header") {
    it("works") {
      val authority = Authority(Host.localhost, Port(9865))

      result(addAuthorityHost(authority)(Request(), Service.mk { req => Future.value(req.headerMap("Host")) })) shouldBe authority.toString
    }
  }

  describe("Add basic authorization header") {
    it("works") {
      result(addBasicAuthorization(Credentials("hello", "kitty"))(Request(), Service.mk { req => Future.value(req.headerMap("Authorization")) })) shouldBe "Basic aGVsbG86a2l0dHk="
    }
  }

  describe("Add eTag") {
    it("passes through when predicate succeeds") {
      val response: Response = Response()
      response.contentString = "bob"
      result(Filters.addETag[Request]()(Request(), Service.mk { req => Future.value(response) })).headerMap("ETag") shouldBe "9f9d51bc70ef21ca5c14f307980a29d8"
    }
  }

}
