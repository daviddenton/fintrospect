package io.fintrospect.parameters

import java.time.{LocalDate, LocalDateTime, ZoneId, ZonedDateTime}

import org.scalatest.{FunSpec, ShouldMatchers}

import scala.language.{higherKinds, implicitConversions}
import scala.util.{Success, Try}

abstract class ParametersTest[T[_] <: Parameter[_], R[_] <: Retrieval[_]](parameters: Parameters[T, R]) extends FunSpec with ShouldMatchers {

  val paramName = "name"

  def attemptFrom[X](methodUnderTest: (String, String) => T[X] with R[X], value: Option[String]): Option[Try[X]]

  def to[X](methodUnderTest: (String, String) => T[X] with R[X], value: X): ParamBinding[X]


  describe("int") {
    it("retrieves a valid value") {
      attemptFrom(parameters.int, Some("123")) shouldEqual Some(Success(123))
    }

    it("does not retrieve an invalid value") {
      attemptFrom(parameters.int, Some("notANumber")).get.isFailure shouldEqual true
    }

    it("does not retrieve an null int value") {
      attemptFrom(parameters.int, None) shouldEqual None
    }

    it("serializes an int correctly") {
      to(parameters.int, 123).value shouldEqual "123"
    }
  }

  describe("integer") {
    it("retrieves a valid value") {
      attemptFrom(parameters.integer, Some("123")) shouldEqual Some(Success(123))
    }

    it("does not retrieve an invalid value") {
      attemptFrom(parameters.integer, Some("notANumber")).get.isFailure shouldEqual true
    }

    it("does not retrieve an null integer value") {
      attemptFrom(parameters.integer, None) shouldEqual None
    }

    it("serializes an integer correctly") {
      to(parameters.integer, new Integer(123)).value shouldEqual "123"
    }
  }

  describe("long") {
    it("retrieves a valid value") {
      attemptFrom(parameters.long, Some("123")) shouldEqual Some(Success(123L))
    }

    it("does not retrieve an invalid value") {
      attemptFrom(parameters.long, Some("notANumber")).get.isFailure shouldEqual true
    }

    it("does not retrieve an null long value") {
      attemptFrom(parameters.long, None) shouldEqual None
    }

    it("serializes a long correctly") {
      to(parameters.long, 123L).value shouldEqual "123"
    }
  }

  describe("bigDecimal") {
    it("retrieves a valid value") {
      attemptFrom(parameters.bigDecimal, Some("1.234")) shouldEqual Some(Success(BigDecimal("1.234")))
    }

    it("does not retrieve an invalid value") {
      attemptFrom(parameters.bigDecimal, Some("notANumber")).get.isFailure shouldEqual true
    }

    it("does not retrieve an null bigDecimal value") {
      attemptFrom(parameters.bigDecimal, None) shouldEqual None
    }

    it("serializes a bigDecimal correctly") {
      to(parameters.bigDecimal, BigDecimal("1.234")).value shouldEqual "1.234"
    }
  }

  describe("boolean") {
    it("retrieves a valid value") {
      attemptFrom(parameters.boolean, Some("true")) shouldEqual Some(Success(true))
    }

    it("does not retrieve an invalid value") {
      attemptFrom(parameters.boolean, Some("notABoolean")).get.isFailure shouldEqual true
    }

    it("does not retrieve an null boolean value") {
      attemptFrom(parameters.boolean, None) shouldEqual None
    }

    it("serializes a boolean correctly") {
      to(parameters.boolean, true).value shouldEqual "true"
    }
  }

  describe("string") {
    it("retrieves a valid value") {
      attemptFrom(parameters.string, Some("123")) shouldEqual Some(Success("123"))
    }

    it("does not retrieve an null string value") {
      attemptFrom(parameters.string, None) shouldEqual None
    }

    it("serializes a string correctly") {
      to(parameters.string, "123").value shouldEqual "123"
    }
  }

  describe("dateTime") {
    it("retrieves a valid value") {
      attemptFrom(parameters.dateTime, Some("1970-01-01T00:00:00")) shouldEqual Some(Success(LocalDateTime.of(1970, 1, 1, 0, 0, 0)))
    }

    it("does not retrieve an invalid value") {
      attemptFrom(parameters.dateTime, Some("notADateTime")).get.isFailure shouldEqual true
    }

    it("does not retrieve an null dateTime value") {
      attemptFrom(parameters.dateTime, None) shouldEqual None
    }

    it("serializes a dateTime correctly") {
      to(parameters.dateTime, LocalDateTime.of(1970, 1, 1, 2, 3, 4)).value shouldEqual "1970-01-01T02:03:04"
    }
  }

  describe("zonedDateTime") {
    it("retrieves a valid value") {
      attemptFrom(parameters.zonedDateTime, Some("1970-01-01T00:00:00-01:00")).get.get.toEpochSecond shouldEqual 3600
    }

    it("does not retrieve an invalid value") {
      attemptFrom(parameters.zonedDateTime, Some("notADateTime")).get.isFailure shouldEqual true
    }

    it("does not retrieve an null zonedDateTime value") {
      attemptFrom(parameters.zonedDateTime, None) shouldEqual None
    }

    it("serializes a zonedDateTime correctly") {
      to(parameters.zonedDateTime, ZonedDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC"))).value shouldEqual "1970-01-01T00:00:00Z[UTC]"
    }
  }

  describe("date") {
    it("retrieves a valid value") {
      attemptFrom(parameters.localDate, Some("1970-01-01")) shouldEqual Some(Success(LocalDate.of(1970, 1, 1)))
    }

    it("does not retrieve an invalid value") {
      attemptFrom(parameters.localDate, Some("notADate")).get.isFailure shouldEqual true
    }

    it("does not retrieve an null date value") {
      attemptFrom(parameters.localDate, None) shouldEqual None
    }

    it("serializes a date correctly") {
      to(parameters.localDate, LocalDate.of(1970, 1, 1)).value shouldEqual "1970-01-01"
    }
  }

  case class MyCustomType(value: Int)

  describe("custom") {

    def myCustomParameter(name: String, unused: String) = {
      parameters(ParameterSpec[MyCustomType](name, None, StringParamType, s => MyCustomType(s.toInt), ct => ct.value.toString))
    }

    it("retrieves a valid custom value") {
      attemptFrom(myCustomParameter, Some("123")) shouldEqual Some(Success(MyCustomType(123)))
    }

    it("does not retrieve an invalid custom value") {
      attemptFrom(myCustomParameter, Some("BOB")).get.isFailure shouldEqual true
    }

    it("does not retrieve an null custom value") {
      attemptFrom(myCustomParameter, None) shouldEqual None
    }

    it("serializes a custom value correctly") {
      to(myCustomParameter, MyCustomType(123)).value shouldEqual "123"
    }
  }
}
