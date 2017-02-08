package io.fintrospect.parameters

import com.twitter.finagle.http.Request
import io.fintrospect.util.ExtractionError.Missing
import io.fintrospect.util.{Extracted, ExtractionFailed}
import org.scalatest.{FunSpec, Matchers}

import scala.language.reflectiveCalls

class ExtractedRouteRequestTest extends FunSpec with Matchers {

  describe("ExtractedRouteRequest") {
    it("uses pre-extracted value if present") {
      val bob = FormField.required.string("bob")
      val body = Body.form(bob)
      ExtractedRouteRequest(Request(), Map(body -> Extracted(Form()))).get(body, (_: Request) => ExtractionFailed(Missing(bob))) shouldBe Extracted(Form())
    }

    it("converts missing parameter to Extraction failed") {
      val bob = FormField.required.string("bob")
      val bill = FormField.required.string("bill")
      val body = Body.form(bob, bill)
      ExtractedRouteRequest(Request(), Map()).get(body, body.extract) shouldBe ExtractionFailed(Seq(Missing(bob), Missing(bill)))
    }
  }

}
