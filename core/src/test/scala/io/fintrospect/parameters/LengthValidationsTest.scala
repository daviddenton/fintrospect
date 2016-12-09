package io.fintrospect.parameters

import org.scalatest.{FunSpec, Matchers}

class LengthValidationsTest extends FunSpec with Matchers {

  describe("Validations") {
    val validations = new LengthValidations[String] {}
    it("EmptyIsValid") {
      validations.EmptyIsValid("") shouldBe ""
      validations.EmptyIsValid("asd") shouldBe "asd"
    }
    it("EmptyIsInvalid") {
      intercept[Exception](validations.EmptyIsInvalid(""))
      intercept[Exception](validations.EmptyIsInvalid(null))
    }
  }
}
