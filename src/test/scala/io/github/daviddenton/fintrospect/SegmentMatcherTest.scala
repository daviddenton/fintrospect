package io.github.daviddenton.fintrospect

import io.github.daviddenton.fintrospect.SegmentMatchers._
import org.joda.time.DateTime
import org.scalatest.{FunSpec, ShouldMatchers}

class SegmentMatcherTest extends FunSpec with ShouldMatchers {

  describe("int") {
    it("unapplies a valid value") {
      int("name").unapply("123") should be === Some(123)
    }

    it("does not unapply an invalid value") {
      int("name").unapply("notANumber") should be === None
    }
  }

  describe("integer") {
    it("unapplies a valid value") {
      integer("name").unapply("123") should be === Some(123)
    }

    it("does not unapply an invalid value") {
      integer("name").unapply("notANumber") should be === None
    }
  }

  describe("long") {
    it("unapplies a valid value") {
      long("name").unapply("123") should be === Some(123L)
    }

    it("does not unapply an invalid value") {
      long("name").unapply("notANumber") should be === None
    }
  }

  describe("boolean") {
    it("unapplies a valid value") {
      boolean("name").unapply("true") should be === Some(true)
    }

    it("does not unapply an invalid value") {
      boolean("name").unapply("notABoolean") should be === None
    }
  }

  describe("string") {
    it("unapplies a valid value") {
      string("name").unapply("123") should be === Some("123")
    }
  }

  describe("dateTime") {
    it("unapplies a valid value") {
      dateTime("name").unapply("1970-01-01T00:00:00+00:00") should be === Some(new DateTime(0))
    }
  }
}
