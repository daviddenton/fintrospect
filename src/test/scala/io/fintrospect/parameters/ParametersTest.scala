package io.fintrospect.parameters

import java.time.{LocalDate, LocalDateTime, ZoneId, ZonedDateTime}

import org.scalatest.{FunSpec, ShouldMatchers}

import scala.language.{higherKinds, implicitConversions}

abstract class ParametersTest[T[_] <: Parameter[_], R[_] <: Retrieval[_]](parameters: Parameters[T, R]) extends FunSpec with ShouldMatchers {

  val paramName = "name"

  def from[X](methodUnderTest: (String, String) => T[X] with R[X], value: Option[String]): Option[X]

  def to[X](methodUnderTest: (String, String) => T[X] with R[X], value: X): ParamBinding[X]


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

    it("serializes an int correctly") {
      to(parameters.int, 123).value shouldEqual "123"
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

    it("serializes an integer correctly") {
      to(parameters.integer, new Integer(123)).value shouldEqual "123"
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

    it("serializes a long correctly") {
      to(parameters.long, 123L).value shouldEqual "123"
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

    it("serializes a bigDecimal correctly") {
      to(parameters.bigDecimal, BigDecimal("1.234")).value shouldEqual "1.234"
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

    it("serializes a boolean correctly") {
      to(parameters.boolean, true).value shouldEqual "true"
    }
  }

  describe("string") {
    it("retrieves a valid value") {
      from(parameters.string, Some("123")) shouldEqual Some("123")
    }

    it("does not retrieve an null string value") {
      from(parameters.string, None) shouldEqual None
    }

    it("serializes a string correctly") {
      to(parameters.string, "123").value shouldEqual "123"
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

    it("serializes a dateTime correctly") {
      to(parameters.dateTime, LocalDateTime.of(1970, 1, 1, 2, 3, 4)).value shouldEqual "1970-01-01T02:03:04"
    }
  }

  describe("zonedDateTime") {
    it("retrieves a valid value") {
      from(parameters.zonedDateTime, Some("1970-01-01T00:00:00-01:00")).map(_.toEpochSecond) shouldEqual Some(3600)
    }

    it("does not retrieve an invalid value") {
      from(parameters.zonedDateTime, Some("notADateTime")) shouldEqual None
    }

    it("does not retrieve an null zonedDateTime value") {
      from(parameters.zonedDateTime, None) shouldEqual None
    }

    it("serializes a zonedDateTime correctly") {
      to(parameters.zonedDateTime, ZonedDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC"))).value shouldEqual "1970-01-01T00:00:00Z[UTC]"
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

    it("serializes a date correctly") {
      to(parameters.localDate, LocalDate.of(1970, 1, 1)).value shouldEqual "1970-01-01"
    }
  }

  case class MyCustomType(value: Int)

  describe("custom") {
    def myCustomParameter(name: String, unused: String) = parameters.custom[MyCustomType](name, s => MyCustomType(s.toInt), ct => ct.value.toString)

    it("retrieves a valid custom value") {
      from(myCustomParameter, Some("123")) shouldEqual Some(MyCustomType(123))
    }

    it("does not retrieve an invalid custom value") {
      from(myCustomParameter, Some("BOB")) shouldEqual None
    }

    it("does not retrieve an null custom value") {
      from(myCustomParameter, None) shouldEqual None
    }

    it("serializes a custom value correctly") {
      to(myCustomParameter, MyCustomType(123)).value shouldEqual "123"
    }
  }
}
