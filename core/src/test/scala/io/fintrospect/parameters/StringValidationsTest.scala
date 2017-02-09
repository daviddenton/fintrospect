package io.fintrospect.parameters

import org.scalatest.{FunSpec, Matchers}

class StringValidationsTest extends FunSpec with Matchers {

  describe("Validations") {
    it("EmptyIsValid") {
      StringValidations.EmptyIsValid("") shouldBe ""
      StringValidations.EmptyIsValid("asd") shouldBe "asd"
    }
    it("EmptyIsInvalid") {
      intercept[Exception](StringValidations.EmptyIsInvalid(""))
      intercept[Exception](StringValidations.EmptyIsInvalid(null))
    }
  }
}
