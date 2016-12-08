package io.fintrospect.parameters

import org.scalatest.{FunSpec, Matchers}

class ValidationsTest extends FunSpec with Matchers {

  describe("Validations") {
    val validations = new Validations[String] {}
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
