package io.github.daviddenton.fintrospect.parameters

import java.time.{LocalDateTime, LocalDate}

import org.scalatest.{FunSpec, ShouldMatchers}

import scala.language.{higherKinds, implicitConversions}

abstract class ParametersTest[T[_] <: Parameter[_]](parameters: Parameters[T]) extends FunSpec with ShouldMatchers {

  val paramName = "name"

  def from[X](methodUnderTest: (String, String) => T[X], value: Option[String]): Option[X]

  describe("int") {
    it("retrieves a valid value") {
      from(parameters.int, Some("123")) shouldEqual Some(123)
    }

    it("does not retrieve an invalid value") {
      from(parameters.int, Some("notANumber")) shouldEqual None
    }

    it("does not retrieve an null int value") {
      from(parameters.int, None) shouldEqual None
    }
  }

  describe("integer") {
    it("retrieves a valid value") {
      from(parameters.integer, Some("123")) shouldEqual Some(123)
    }

    it("does not retrieve an invalid value") {
      from(parameters.integer, Some("notANumber")) shouldEqual None
    }

    it("does not retrieve an null integer value") {
      from(parameters.integer, None) shouldEqual None
    }
  }

  describe("long") {
    it("retrieves a valid value") {
      from(parameters.long, Some("123")) shouldEqual Some(123L)
    }

    it("does not retrieve an invalid value") {
      from(parameters.long, Some("notANumber")) shouldEqual None
    }

    it("does not retrieve an null long value") {
      from(parameters.long, None) shouldEqual None
    }
  }

  describe("bigDecimal") {
    it("retrieves a valid value") {
      from(parameters.bigDecimal, Some("1.234")) shouldEqual Some(BigDecimal("1.234"))
    }

    it("does not retrieve an invalid value") {
      from(parameters.bigDecimal, Some("notANumber")) shouldEqual None
    }

    it("does not retrieve an null bigDecimal value") {
      from(parameters.bigDecimal, None) shouldEqual None
    }
  }

  describe("boolean") {
    it("retrieves a valid value") {
      from(parameters.boolean, Some("true")) shouldEqual Some(true)
    }

    it("does not retrieve an invalid value") {
      from(parameters.boolean, Some("notABoolean")) shouldEqual None
    }

    it("does not retrieve an null boolean value") {
      from(parameters.boolean, None) shouldEqual None
    }
  }

  describe("string") {
    it("retrieves a valid value") {
      from(parameters.string, Some("123")) shouldEqual Some("123")
    }

    it("does not retrieve an null string value") {
      from(parameters.string, None) shouldEqual None
    }
  }

  describe("dateTime") {
    it("retrieves a valid value") {
      from(parameters.dateTime, Some("1970-01-01T00:00:00")) shouldEqual Some(LocalDateTime.of(1970, 1, 1, 0, 0, 0))
    }

    it("does not retrieve an invalid value") {
      from(parameters.dateTime, Some("notADateTime")) shouldEqual None
    }

    it("does not retrieve an null dateTime value") {
      from(parameters.dateTime, None) shouldEqual None
    }
  }

  describe("zonedDateTime") {
    it("retrieves a valid value") {
      from(parameters.zonedDateTime, Some("1970-01-01T00:00:00-01:00")).map(_.toEpochSecond) shouldEqual Some(3600)
    }

    it("does not retrieve an invalid value") {
      from(parameters.zonedDateTime, Some("notADateTime")) shouldEqual None
    }

    it("does not retrieve an null dateTime value") {
      from(parameters.zonedDateTime, None) shouldEqual None
    }
  }

  describe("date") {
    it("retrieves a valid value") {
      from(parameters.localDate, Some("1970-01-01")) shouldEqual Some(LocalDate.of(1970, 1, 1))
    }

    it("does not retrieve an invalid value") {
      from(parameters.localDate, Some("notADate")) shouldEqual None
    }

    it("does not retrieve an null date value") {
      from(parameters.localDate, None) shouldEqual None
    }
  }
}
