package io.fintrospect.parameters

import org.scalatest._

class ValidatorTest extends FunSpec with ShouldMatchers {

  describe("validator") {
    it("all Successful") {
      Validator.mk(
        Extracted(Some(1)),
        Extracted(Some(2))
      ) { case (first, second) => (first, second) } shouldBe Validated((Some(1), Some(2)))
    }

    it("all Successful or NotProvided") {
      Validator.mk(
        Extracted(Some(1)),
        Extracted(None)
      ) { case (first, second) => (first, second) } shouldBe Validated((Some(1), None))
    }

    it("all NotProvided") {
      Validator.mk(
        Extracted(None),
        Extracted(None)
      ) { case (first, second) => (first, second) } shouldBe Validated((None, None))
    }

    it("collects Errors") {
      val ip1 = InvalidParameter(Query.required.string("first"), "invalid1")
      val ip2 = InvalidParameter(Query.required.string("second"), "invalid2")
      Validator.mk(
        ExtractionFailed(ip1),
        ExtractionFailed(ip2)
      ) { case (first, second) => (first, second) } shouldBe ValidationFailed(Seq(ip1, ip2))
    }

    it("can make a validator with tuple types") {
      Validator.mk(Extracted(Some(1)), Extracted(Some(1))) { case t => }
      Validator.mk(Extracted(Some(1)), Extracted(Some(1)), Extracted(Some(1))) { case t => }
      Validator.mk(Extracted(Some(1)), Extracted(Some(1)), Extracted(Some(1)), Extracted(Some(1))) { case t => }
      Validator.mk(Extracted(Some(1)), Extracted(Some(1)), Extracted(Some(1)), Extracted(Some(1)), Extracted(Some(1))) { case t => }
      Validator.mk(Extracted(Some(1)), Extracted(Some(1)), Extracted(Some(1)), Extracted(Some(1)), Extracted(Some(1)), Extracted(Some(1))) { case t => }
      Validator.mk(Extracted(Some(1)), Extracted(Some(1)), Extracted(Some(1)), Extracted(Some(1)), Extracted(Some(1)), Extracted(Some(1)), Extracted(Some(1))) { case t => }
      Validator.mk(Extracted(Some(1)), Extracted(Some(1)), Extracted(Some(1)), Extracted(Some(1)), Extracted(Some(1)), Extracted(Some(1)), Extracted(Some(1)), Extracted(Some(1))) { case t => }
      Validator.mk(Extracted(Some(1)), Extracted(Some(1)), Extracted(Some(1)), Extracted(Some(1)), Extracted(Some(1)), Extracted(Some(1)), Extracted(Some(1)), Extracted(Some(1)), Extracted(Some(1))) { case t => }
      Validator.mk(Extracted(Some(1)), Extracted(Some(1)), Extracted(Some(1)), Extracted(Some(1)), Extracted(Some(1)), Extracted(Some(1)), Extracted(Some(1)), Extracted(Some(1)), Extracted(Some(1)), Extracted(Some(1))) { case t => }
    }
  }
}
