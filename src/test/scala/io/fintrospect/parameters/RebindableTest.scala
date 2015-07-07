package io.fintrospect.parameters

import com.twitter.finagle.http.Request
import org.jboss.netty.handler.codec.http.HttpMethod
import org.scalatest.{FunSpec, ShouldMatchers}

class RebindableTest extends FunSpec with ShouldMatchers {

  describe("Rebinding") {
    describe("Mandatory") {
      it("can rebind") {
        val inRequest = Request()
        inRequest.headers().add("field", "123")
        val bindings = Header.required.int("field") <-> inRequest
        val outRequest = bindings.foldLeft(RequestBuild()) { (requestBuild, next) => next(requestBuild) }.build(HttpMethod.GET)
        outRequest.headers().get("field") shouldEqual "123"
      }
    }

    describe("Optional") {
      it("can rebind present value") {
        val inRequest = Request()
        inRequest.headers().add("field", "123")
        val bindings = Header.optional.int("field") <-> inRequest
        val outRequest = bindings.foldLeft(RequestBuild()) { (requestBuild, next) => next(requestBuild) }.build(HttpMethod.GET)
        outRequest.headers().get("field") shouldEqual "123"
      }

      it("does not rebind missing value") {
        val inRequest = Request()
        val bindings = Header.optional.int("field") <-> inRequest
        val outRequest = bindings.foldLeft(RequestBuild()) { (requestBuild, next) => next(requestBuild) }.build(HttpMethod.GET)
        outRequest.headers().get("field") shouldEqual null
      }
    }
  }
}