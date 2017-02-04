package io.fintrospect.util

import io.fintrospect.parameters.Query
import org.scalatest._

class ValidatorTest extends FunSpec with Matchers {

  describe("validator") {
    it("all Successful") {
      Validator.mk(
        Extracted(1),
        Extracted(2)
      ) { case (first, second) => (first, second) } shouldBe Validated((Some(1), Some(2)))
    }

    it("collects Errors") {
      val ip1 = ExtractionError(Query.required.string("first"), "invalid1")
      val ip2 = ExtractionError(Query.required.string("second"), "invalid2")
      Validator.mk(
        ExtractionFailed(ip1),
        ExtractionFailed(ip2)
      ) { case (first, second) => (first, second) } shouldBe ValidationFailed(Seq(ip1, ip2))
    }

    it("can make a validator with tuple types") {
      Validator.mk(Extracted(1), Extracted(1)) { case t => }
      Validator.mk(Extracted(1), Extracted(1), Extracted(1)) { case t => }
      Validator.mk(Extracted(1), Extracted(1), Extracted(1), Extracted(1)) { case t => }
      Validator.mk(Extracted(1), Extracted(1), Extracted(1), Extracted(1), Extracted(1)) { case t => }
      Validator.mk(Extracted(1), Extracted(1), Extracted(1), Extracted(1), Extracted(1), Extracted(1)) { case t => }
      Validator.mk(Extracted(1), Extracted(1), Extracted(1), Extracted(1), Extracted(1), Extracted(1), Extracted(1)) { case t => }
      Validator.mk(Extracted(1), Extracted(1), Extracted(1), Extracted(1), Extracted(1), Extracted(1), Extracted(1), Extracted(1)) { case t => }
      Validator.mk(Extracted(1), Extracted(1), Extracted(1), Extracted(1), Extracted(1), Extracted(1), Extracted(1), Extracted(1), Extracted(1)) { case t => }
      Validator.mk(Extracted(1), Extracted(1), Extracted(1), Extracted(1), Extracted(1), Extracted(1), Extracted(1), Extracted(1), Extracted(1), Extracted(1)) { case t => }
    }
  }
}
