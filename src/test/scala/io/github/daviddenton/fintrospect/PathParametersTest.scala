package io.github.daviddenton.fintrospect

import io.github.daviddenton.fintrospect.parameters.Path
import org.joda.time.DateTime
import org.scalatest.{FunSpec, ShouldMatchers}

class PathParametersTest extends FunSpec with ShouldMatchers {

  describe("int") {
    it("unapplies a valid value") {
      Path.int("name").unapply("123") should be === Some(123)
    }

    it("does not unapply an invalid value") {
      Path.int("name").unapply("notANumber") should be === None
    }
  }

  describe("integer") {
    it("unapplies a valid value") {
      Path.integer("name").unapply("123") should be === Some(123)
    }

    it("does not unapply an invalid value") {
      Path.integer("name").unapply("notANumber") should be === None
    }
  }

  describe("long") {
    it("unapplies a valid value") {
      Path.long("name").unapply("123") should be === Some(123L)
    }

    it("does not unapply an invalid value") {
      Path.long("name").unapply("notANumber") should be === None
    }
  }

  describe("boolean") {
    it("unapplies a valid value") {
      Path.boolean("name").unapply("true") should be === Some(true)
    }

    it("does not unapply an invalid value") {
      Path.boolean("name").unapply("notABoolean") should be === None
    }
  }

  describe("string") {
    it("unapplies a valid value") {
      Path.string("name").unapply("123") should be === Some("123")
    }
  }

  describe("dateTime") {
    it("unapplies a valid value") {
      Path.dateTime("name").unapply("1970-01-01T00:00:00+00:00") should be === Some(new DateTime(0))
    }
  }
}
