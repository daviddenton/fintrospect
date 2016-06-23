package io.fintrospect.parameters

import org.scalatest._

class ValidatorTest extends FunSpec with ShouldMatchers {

  describe("validator") {
    it("all Successful") {
      Validator.mk(
        Extracted(1),
        Extracted(2)
      ) { case (first, second) => (first, second) } shouldBe Validated((Some(1), Some(2)))
    }

    it("all Successful or NotProvided") {
      Validator.mk(
        Extracted(1),
        NotProvided
      ) { case (first, second) => (first, second) } shouldBe Validated((Some(1), None))
    }

    it("all NotProvided") {
      Validator.mk(
        NotProvided,
        NotProvided
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
      Validator.mk(NotProvided, NotProvided)
      Validator.mk(NotProvided, NotProvided, NotProvided)
      Validator.mk(NotProvided, NotProvided, NotProvided, NotProvided)
      Validator.mk(NotProvided, NotProvided, NotProvided, NotProvided, NotProvided)
      Validator.mk(NotProvided, NotProvided, NotProvided, NotProvided, NotProvided, NotProvided)
      Validator.mk(NotProvided, NotProvided, NotProvided, NotProvided, NotProvided, NotProvided, NotProvided)
      Validator.mk(NotProvided, NotProvided, NotProvided, NotProvided, NotProvided, NotProvided, NotProvided, NotProvided)
      Validator.mk(NotProvided, NotProvided, NotProvided, NotProvided, NotProvided, NotProvided, NotProvided, NotProvided, NotProvided)
      Validator.mk(NotProvided, NotProvided, NotProvided, NotProvided, NotProvided, NotProvided, NotProvided, NotProvided, NotProvided, NotProvided)
    }
  }
}
