package io.fintrospect.parameters

import java.time.{LocalDate, LocalDateTime, ZoneId, ZonedDateTime}
import java.util.UUID

import io.fintrospect.parameters.StringValidations._
import org.scalatest._

import scala.util.{Success, Try}

class ParameterSpecTest extends FunSpec with Matchers {

  describe("int") {
    it("retrieves a valid value") {
      Try(ParameterSpec.int().deserialize("123")) shouldBe Success(123)
    }

    it("does not retrieve an invalid value") {
      Try(ParameterSpec.int().deserialize("asd")).isFailure shouldBe true
    }

    it("does not retrieve an null value") {
      Try(ParameterSpec.int().deserialize(null)).isFailure shouldBe true
    }

    it("serializes correctly") {
      ParameterSpec.int().serialize(123) shouldBe "123"
    }
  }

  describe("integer") {
    it("retrieves a valid value") {
      Try(ParameterSpec.integer().deserialize("123")) shouldBe Success(123)
    }

    it("does not retrieve an invalid value") {
      Try(ParameterSpec.integer().deserialize("asd")).isFailure shouldBe true
    }

    it("does not retrieve an null value") {
      Try(ParameterSpec.integer().deserialize(null)).isFailure shouldBe true
    }

    it("serializes correctly") {
      ParameterSpec.integer().serialize(123) shouldBe "123"
    }
  }

  describe("long") {
    it("retrieves a valid value") {
      Try(ParameterSpec.long().deserialize("123")) shouldBe Success(123)
    }

    it("does not retrieve an invalid value") {
      Try(ParameterSpec.long().deserialize("asd")).isFailure shouldBe true
    }

    it("does not retrieve an null value") {
      Try(ParameterSpec.long().deserialize(null)).isFailure shouldBe true
    }

    it("serializes correctly") {
      ParameterSpec.long().serialize(123) shouldBe "123"
    }
  }

  describe("bigDecimal") {
    it("retrieves a valid value") {
      Try(ParameterSpec.bigDecimal().deserialize("1.1234")) shouldBe Success(BigDecimal("1.1234"))
    }

    it("does not retrieve an invalid value") {
      Try(ParameterSpec.bigDecimal().deserialize("asd")).isFailure shouldBe true
    }

    it("does not retrieve an null value") {
      Try(ParameterSpec.bigDecimal().deserialize(null)).isFailure shouldBe true
    }

    it("serializes correctly") {
      ParameterSpec.bigDecimal().serialize(BigDecimal("1.1234")) shouldBe "1.1234"
    }
  }

  describe("boolean") {
    it("retrieves a valid value") {
      Try(ParameterSpec.boolean().deserialize("true")) shouldBe Success(true)
    }

    it("does not retrieve an invalid value") {
      Try(ParameterSpec.boolean().deserialize("notABool")).isFailure shouldBe true
    }

    it("does not retrieve an null value") {
      Try(ParameterSpec.boolean().deserialize(null)).isFailure shouldBe true
    }

    it("serializes correctly") {
      ParameterSpec.boolean().serialize(true) shouldBe "true"
    }
  }

  describe("string") {
    it("by default empty string is invalid") {
      Try(ParameterSpec.string().deserialize("")).isFailure shouldBe true
    }

    it("retrieves a valid value") {
      Try(ParameterSpec.string(EmptyIsInvalid).deserialize("123")) shouldBe Success("123")
      Try(ParameterSpec.string(EmptyIsValid).deserialize("")) shouldBe Success("")
    }

    it("does not retrieve an invalid value") {
      Try(ParameterSpec.string(EmptyIsValid).deserialize(null)).isFailure shouldBe true
      Try(ParameterSpec.string(EmptyIsInvalid).deserialize("")).isFailure shouldBe true
    }

    it("serializes correctly") {
      ParameterSpec.string().serialize("asdas") shouldBe "asdas"
    }
  }

  describe("uuid") {
    it("retrieves a valid value") {
      Try(ParameterSpec.uuid().deserialize("41fe035f-a948-4d67-a276-1ff79a0a7443")) shouldBe Success(UUID.fromString("41fe035f-a948-4d67-a276-1ff79a0a7443"))
    }

    it("does not retrieve an null value") {
      Try(ParameterSpec.uuid().deserialize(null)).isFailure shouldBe true
    }

    it("serializes correctly") {
      ParameterSpec.uuid().serialize(UUID.fromString("41fe035f-a948-4d67-a276-1ff79a0a7443")) shouldBe "41fe035f-a948-4d67-a276-1ff79a0a7443"
    }
  }

  describe("dateTime") {
    it("retrieves a valid value") {
      Try(ParameterSpec.dateTime().deserialize("1970-01-01T00:00:00")) shouldBe Success(LocalDateTime.of(1970, 1, 1, 0, 0, 0))
    }

    it("does not retrieve an invalid value") {
      Try(ParameterSpec.dateTime().deserialize("notADateTime")).isFailure shouldBe true
    }

    it("does not retrieve an null value") {
      Try(ParameterSpec.dateTime().deserialize(null)).isFailure shouldBe true
    }

    it("serializes correctly") {
      ParameterSpec.dateTime().serialize(LocalDateTime.of(1970, 1, 1, 2, 3, 4)) shouldBe "1970-01-01T02:03:04"
    }
  }

  describe("zonedDateTime") {
    it("retrieves a valid value") {
      Try(ParameterSpec.zonedDateTime().deserialize("1970-01-01T00:00:00-01:00")).get.toEpochSecond shouldBe 3600
    }

    it("does not retrieve an invalid value") {
      Try(ParameterSpec.zonedDateTime().deserialize("notADateTime")).isFailure shouldBe true
    }

    it("does not retrieve an null value") {
      Try(ParameterSpec.zonedDateTime().deserialize(null)).isFailure shouldBe true
    }

    it("serializes correctly") {
      ParameterSpec.zonedDateTime().serialize(ZonedDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC"))) shouldBe "1970-01-01T00:00:00Z[UTC]"
    }
  }

  describe("date") {
    it("retrieves a valid value") {
      Try(ParameterSpec.localDate().deserialize("1970-01-01")) shouldBe Success(LocalDate.of(1970, 1, 1))
    }

    it("does not retrieve an invalid value") {
      Try(ParameterSpec.localDate().deserialize("notADateTime")).isFailure shouldBe true
    }

    it("does not retrieve an null value") {
      Try(ParameterSpec.localDate().deserialize(null)).isFailure shouldBe true
    }

    it("serializes correctly") {
      ParameterSpec.localDate().serialize(LocalDate.of(1970, 1, 1)) shouldBe "1970-01-01"
    }
  }

  describe("xml") {
    val expected = <field>value</field>

    it("retrieves a valid value") {
      Try(ParameterSpec.xml().deserialize(expected.toString())) shouldBe Success(expected)
    }

    it("does not retrieve an invalid value") {
      Try(ParameterSpec.xml().deserialize("notXml")).isFailure shouldBe true
    }

    it("does not retrieve an null value") {
      Try(ParameterSpec.xml().deserialize(null)).isFailure shouldBe true
    }

    it("serializes correctly") {
      ParameterSpec.xml().serialize(expected) shouldBe """<field>value</field>"""
    }
  }

  describe("custom") {
    it("retrieves a valid value for custom type") {
      val spec = ParameterSpec.int().as[MyCustomType]
      Try(spec.deserialize("123")) shouldBe Success(MyCustomType(123))
      Try(spec.serialize(MyCustomType(123))) shouldBe Success(123)
    }

    it("retrieves a valid value") {
      Try(MyCustomType.spec.deserialize("123")) shouldBe Success(MyCustomType(123))
    }

    it("does not retrieve an invalid value") {
      Try(MyCustomType.spec.deserialize("notAnInt")).isFailure shouldBe true
    }

    it("does not retrieve an null value") {
      Try(MyCustomType.spec.deserialize(null)).isFailure shouldBe true
    }

    it("serializes correctly") {
      MyCustomType.spec.serialize(MyCustomType(123)) shouldBe "123"
    }
  }

  describe("Map to another ParameterSpec") {

    case class IClass(value: Int)

    it("can map with just read") {
      ParameterSpec.int().map(IClass).deserialize("123") shouldBe IClass(123)
    }

    it("can map with read and show") {
      ParameterSpec.int().map[IClass](IClass, (i: IClass) => i.value + i.value).serialize(IClass(100)) shouldBe "200"
    }
  }
}
