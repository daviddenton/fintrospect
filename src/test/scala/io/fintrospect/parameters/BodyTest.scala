package io.fintrospect.parameters

import com.twitter.finagle.http.Request
import io.fintrospect.util.ArgoUtil._
import org.scalatest.{FunSpec, ShouldMatchers}

class BodyTest extends FunSpec with ShouldMatchers {

  describe("body") {
    it("should retrieve the body value from the request") {
      val bodyJson = obj("field" -> string("value"))
      val request = Request("/")
      request.write(pretty(bodyJson))
      Body.json(Some("description"), obj("field" -> string("value"))).from(request) shouldEqual bodyJson
    }
  }

}
