package io.github.daviddenton.fintrospect.parameters

import org.joda.time.{DateTime, LocalDate}
import org.scalatest.{FunSpec, ShouldMatchers}

import scala.language.{higherKinds, implicitConversions}

abstract class ParametersTest[T[_] <: Parameter[_]](parameters: Parameters[T]) extends FunSpec with ShouldMatchers {

  val paramName = "name"

  def from[X](param: T[X], value: String): Option[X]

  describe("int") {
    it("retrieves a valid value") {
      from(parameters.int(paramName), "123") shouldEqual Some(123)
    }

    it("does not retrieve an invalid value") {
      from(parameters.int(paramName), "notANumber") shouldEqual None
    }
  }

  describe("integer") {
    it("retrieves a valid value") {
      from(parameters.integer(paramName), "123") shouldEqual Some(123)
    }

    it("does not retrieve an invalid value") {
      from(parameters.integer(paramName), "notANumber") shouldEqual None
    }
  }

  describe("long") {
    it("retrieves a valid value") {
      from(parameters.long(paramName), "123") shouldEqual Some(123L)
    }

    it("does not retrieve an invalid value") {
      from(parameters.long(paramName), "notANumber") shouldEqual None
    }
  }

  describe("bigDecimal") {
    it("retrieves a valid value") {
      from(parameters.bigDecimal(paramName), "1.234") shouldEqual Some(BigDecimal("1.234"))
    }

    it("does not retrieve an invalid value") {
      from(parameters.bigDecimal(paramName), "notANumber") shouldEqual None
    }
  }

  describe("boolean") {
    it("retrieves a valid value") {
      from(parameters.boolean(paramName), "true") shouldEqual Some(true)
    }

    it("does not retrieve an invalid value") {
      from(parameters.boolean(paramName), "notABoolean") shouldEqual None
    }
  }

  describe("string") {
    it("retrieves a valid value") {
      from(parameters.string(paramName), "123") shouldEqual Some("123")
    }
  }

  describe("dateTime") {
    it("retrieves a valid value") {
      from(parameters.dateTime(paramName), "1970-01-01T00:00:00+00:00") shouldEqual Some(new DateTime(0))
    }

    it("does not retrieve an invalid value") {
      from(parameters.dateTime(paramName), "notADateTime") shouldEqual None
    }
  }

  describe("date") {
    it("retrieves a valid value") {
      from(parameters.localDate(paramName), "1970-01-01") shouldEqual Some(new LocalDate(1970, 1, 1))
    }

    it("does not retrieve an invalid value") {
      from(parameters.localDate(paramName), "notADate") shouldEqual None
    }
  }
}
