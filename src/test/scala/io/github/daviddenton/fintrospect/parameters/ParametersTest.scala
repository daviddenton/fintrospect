
package io.github.daviddenton.fintrospect.parameters

import io.github.daviddenton.fintrospect.util.ArgoUtil._
import org.joda.time.{DateTime, LocalDate}
import org.scalatest.{FunSpec, ShouldMatchers}

abstract class ParametersTest[T[_] <: Parameter[_]](parameters: Parameters[T]) extends FunSpec with ShouldMatchers {

  val paramName = "name"

  def from[X](param: T[X], value: String): Option[X]

  describe("int") {
    it("retrieves a valid value") {
      from(parameters.int(paramName), "123") should be === Some(123)
    }

    it("does not retrieve an invalid value") {
      from(parameters.int(paramName), "notANumber") should be === None
    }
  }

  describe("integer") {
    it("retrieves a valid value") {
      from(parameters.integer(paramName), "123") should be === Some(123)
    }

    it("does not retrieve an invalid value") {
      from(parameters.integer(paramName), "notANumber") should be === None
    }
  }

  describe("long") {
    it("retrieves a valid value") {
      from(parameters.long(paramName), "123") should be === Some(123L)
    }

    it("does not retrieve an invalid value") {
      from(parameters.long(paramName), "notANumber") should be === None
    }
  }

  describe("boolean") {
    it("retrieves a valid value") {
      from(parameters.boolean(paramName), "true") should be === Some(true)
    }

    it("does not retrieve an invalid value") {
      from(parameters.boolean(paramName), "notABoolean") should be === None
    }
  }

  describe("string") {
    it("retrieves a valid value") {
      from(parameters.string(paramName), "123") should be === Some("123")
    }
  }

  describe("dateTime") {
    it("retrieves a valid value") {
      from(parameters.dateTime(paramName), "1970-01-01T00:00:00+00:00") should be === Some(new DateTime(0))
    }

    it("does not retrieve an invalid value") {
      from(parameters.dateTime(paramName), "notADateTime") should be === None
    }
  }

  describe("date") {
    it("retrieves a valid value") {
      from(parameters.localDate(paramName), "1970-01-01") should be === Some(new LocalDate(1970, 1, 1))
    }

    it("does not retrieve an invalid value") {
      from(parameters.localDate(paramName), "notADate") should be === None
    }
  }

  describe("json") {
    it("retrieves a valid value") {
      val expected = obj("field" -> string("value"))
      from(parameters.json(paramName), compact(expected)) should be === Some(expected)
    }

    it("does not retrieve an invalid value") {
      from(parameters.json(paramName), "notJson") should be === None
    }
  }

}
