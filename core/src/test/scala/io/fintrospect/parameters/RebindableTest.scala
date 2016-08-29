package io.fintrospect.parameters

import com.twitter.finagle.http.Method.Get
import com.twitter.finagle.http.Request
import io.fintrospect.RequestBuilder
import org.scalatest.{FunSpec, Matchers}

class RebindableTest extends FunSpec with Matchers {

  describe("Rebinding") {
    describe("Mandatory") {
      it("can rebind") {
        val inRequest = Request()
        inRequest.headerMap.add("field", "123")
        val bindings = Header.required.int("field") <-> inRequest
        val outRequest = bindings.foldLeft(RequestBuilder(Get)) { (requestBuild, next) => next(requestBuild) }.build()
        outRequest.headerMap("field") shouldBe "123"
      }
    }

    describe("Optional") {
      it("can rebind present value") {
        val inRequest = Request()
        inRequest.headerMap.add("field", "123")
        val bindings = Header.optional.int("field") <-> inRequest
        val outRequest = bindings.foldLeft(RequestBuilder(Get)) { (requestBuild, next) => next(requestBuild) }.build()
        outRequest.headerMap("field") shouldBe "123"
      }

      it("does not rebind missing value") {
        val inRequest = Request()
        val bindings = Header.optional.int("field") <-> inRequest
        val outRequest = bindings.foldLeft(RequestBuilder(Get)) { (requestBuild, next) => next(requestBuild) }.build()
        outRequest.headerMap.get("field") shouldBe None
      }
    }
  }
}