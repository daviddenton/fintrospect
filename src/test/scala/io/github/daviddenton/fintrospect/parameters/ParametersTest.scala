
package io.github.daviddenton.fintrospect.parameters

import org.joda.time.{LocalDate, DateTime}
import org.scalatest.{FunSpec, ShouldMatchers}

abstract class ParametersTest[T[_] <: Parameter[_]](parameters: Parameters[T]) extends FunSpec with ShouldMatchers {

  describe("int") {
    it("unapplies a valid value") {
      parameters.int("name").unapply("123") should be === Some(123)
    }

    it("does not unapply an invalid value") {
      parameters.int("name").unapply("notANumber") should be === None
    }
  }

  describe("integer") {
    it("unapplies a valid value") {
      parameters.integer("name").unapply("123") should be === Some(123)
    }

    it("does not unapply an invalid value") {
      parameters.integer("name").unapply("notANumber") should be === None
    }
  }

  describe("long") {
    it("unapplies a valid value") {
      parameters.long("name").unapply("123") should be === Some(123L)
    }

    it("does not unapply an invalid value") {
      parameters.long("name").unapply("notANumber") should be === None
    }
  }

  describe("boolean") {
    it("unapplies a valid value") {
      parameters.boolean("name").unapply("true") should be === Some(true)
    }

    it("does not unapply an invalid value") {
      parameters.boolean("name").unapply("notABoolean") should be === None
    }
  }

  describe("string") {
    it("unapplies a valid value") {
      parameters.string("name").unapply("123") should be === Some("123")
    }
  }

  describe("dateTime") {
    it("unapplies a valid value") {
      parameters.dateTime("name").unapply("1970-01-01T00:00:00+00:00") should be === Some(new DateTime(0))
    }

    it("does not unapply an invalid value") {
      parameters.dateTime("name").unapply("notADateTime") should be === None
    }
  }

  describe("date") {
    it("unapplies a valid value") {
      parameters.localDate("name").unapply("1970-01-01") should be === Some(new LocalDate(1970, 1, 1))
    }

    it("does not unapply an invalid value") {
      parameters.localDate("name").unapply("notADate") should be === None
    }
  }
}
