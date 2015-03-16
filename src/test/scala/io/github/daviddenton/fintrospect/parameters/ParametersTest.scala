
package io.github.daviddenton.fintrospect.parameters

import org.joda.time.{LocalDate, DateTime}
import org.scalatest.{FunSpec, ShouldMatchers}

abstract class ParametersTest[S, T[_] <: Parameter[_, S]](parameters: Parameters[T]) extends FunSpec with ShouldMatchers {

  val paramName = "name"

  def embed(param: String): S

  describe("int") {
    it("unapplies a valid value") {
      parameters.int(paramName).unapply(embed("123")) should be === Some(123)
    }

    it("does not unapply an invalid value") {
      parameters.int(paramName).unapply(embed("notANumber")) should be === None
    }
  }

  describe("integer") {
    it("unapplies a valid value") {
      parameters.integer(paramName).unapply(embed("123")) should be === Some(123)
    }

    it("does not unapply an invalid value") {
      parameters.integer(paramName).unapply(embed("notANumber")) should be === None
    }
  }

  describe("long") {
    it("unapplies a valid value") {
      parameters.long(paramName).unapply(embed("123")) should be === Some(123L)
    }

    it("does not unapply an invalid value") {
      parameters.long(paramName).unapply(embed("notANumber")) should be === None
    }
  }

  describe("boolean") {
    it("unapplies a valid value") {
      parameters.boolean(paramName).unapply(embed("true")) should be === Some(true)
    }

    it("does not unapply an invalid value") {
      parameters.boolean(paramName).unapply(embed("notABoolean")) should be === None
    }
  }

  describe("string") {
    it("unapplies a valid value") {
      parameters.string(paramName).unapply(embed("123")) should be === Some("123")
    }
  }

  describe("dateTime") {
    it("unapplies a valid value") {
      parameters.dateTime(paramName).unapply(embed("1970-01-01T00:00:00+00:00")) should be === Some(new DateTime(0))
    }

    it("does not unapply an invalid value") {
      parameters.dateTime(paramName).unapply(embed("notADateTime")) should be === None
    }
  }

  describe("date") {
    it("unapplies a valid value") {
      parameters.localDate(paramName).unapply(embed("1970-01-01")) should be === Some(new LocalDate(1970, 1, 1))
    }

    it("does not unapply an invalid value") {
      parameters.localDate(paramName).unapply(embed("notADate")) should be === None
    }
  }
}
