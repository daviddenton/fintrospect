package io.fintrospect.parameters

import com.twitter.finagle.http.Request
import io.fintrospect.util.ExtractionError.Missing
import io.fintrospect.util.ExtractionFailed
import org.scalatest.{FunSpec, Matchers}

import scala.language.reflectiveCalls

class ExtractedRouteRequestTest extends FunSpec with Matchers {

  describe("ExtractedRouteRequest") {
    it("converts missing body to Extraction failed") {
      val bob = FormField.required.string("bob")
      val bill = FormField.required.string("bill")
      val body = Body.form(bob, bill)
      ExtractedRouteRequest(Request(), Map()).get(body) shouldBe ExtractionFailed(Seq(Missing(bob), Missing(bill)))
    }
    it("converts missing parameter to Extraction failed") {
      val bob = Query.required.string("bob")
      ExtractedRouteRequest(Request(), Map()).get(bob) shouldBe ExtractionFailed(Seq(Missing(bob)))
    }
  }

}
