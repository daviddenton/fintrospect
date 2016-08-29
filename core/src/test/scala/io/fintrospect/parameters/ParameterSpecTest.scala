package io.fintrospect.parameters

import java.time.{LocalDate, LocalDateTime, ZoneId, ZonedDateTime}
import java.util.UUID

import io.fintrospect.parameters.StringValidation.{EmptyIsInvalid, EmptyIsValid}
import org.scalatest._

import scala.util.{Success, Try}

class ParameterSpecTest extends FunSpec with Matchers {

  val paramName = "name"

  describe("int") {
    it("retrieves a valid value") {
      Try(ParameterSpec.int(paramName, "").deserialize("123")) shouldBe Success(123)
    }

    it("does not retrieve an invalid value") {
      Try(ParameterSpec.int(paramName, "").deserialize("asd")).isFailure shouldBe true
    }

    it("does not retrieve an null value") {
      Try(ParameterSpec.int(paramName, "").deserialize(null)).isFailure shouldBe true
    }

    it("serializes correctly") {
      ParameterSpec.int(paramName, "").serialize(123) shouldBe "123"
    }
  }

  describe("integer") {
    it("retrieves a valid value") {
      Try(ParameterSpec.integer(paramName, "").deserialize("123")) shouldBe Success(123)
    }

    it("does not retrieve an invalid value") {
      Try(ParameterSpec.integer(paramName, "").deserialize("asd")).isFailure shouldBe true
    }

    it("does not retrieve an null value") {
      Try(ParameterSpec.integer(paramName, "").deserialize(null)).isFailure shouldBe true
    }

    it("serializes correctly") {
      ParameterSpec.integer(paramName, "").serialize(123) shouldBe "123"
    }
  }

  describe("long") {
    it("retrieves a valid value") {
      Try(ParameterSpec.long(paramName, "").deserialize("123")) shouldBe Success(123)
    }

    it("does not retrieve an invalid value") {
      Try(ParameterSpec.long(paramName, "").deserialize("asd")).isFailure shouldBe true
    }

    it("does not retrieve an null value") {
      Try(ParameterSpec.long(paramName, "").deserialize(null)).isFailure shouldBe true
    }

    it("serializes correctly") {
      ParameterSpec.long(paramName, "").serialize(123) shouldBe "123"
    }
  }

  describe("bigDecimal") {
    it("retrieves a valid value") {
      Try(ParameterSpec.bigDecimal(paramName, "").deserialize("1.1234")) shouldBe Success(BigDecimal("1.1234"))
    }

    it("does not retrieve an invalid value") {
      Try(ParameterSpec.bigDecimal(paramName, "").deserialize("asd")).isFailure shouldBe true
    }

    it("does not retrieve an null value") {
      Try(ParameterSpec.bigDecimal(paramName, "").deserialize(null)).isFailure shouldBe true
    }

    it("serializes correctly") {
      ParameterSpec.bigDecimal(paramName, "").serialize(BigDecimal("1.1234")) shouldBe "1.1234"
    }
  }

  describe("boolean") {
    it("retrieves a valid value") {
      Try(ParameterSpec.boolean(paramName, "").deserialize("true")) shouldBe Success(true)
    }

    it("does not retrieve an invalid value") {
      Try(ParameterSpec.boolean(paramName, "").deserialize("notABool")).isFailure shouldBe true
    }

    it("does not retrieve an null value") {
      Try(ParameterSpec.boolean(paramName, "").deserialize(null)).isFailure shouldBe true
    }

    it("serializes correctly") {
      ParameterSpec.boolean(paramName, "").serialize(true) shouldBe "true"
    }
  }

  describe("string") {
    it("retrieves a valid value") {
      Try(ParameterSpec.string(paramName, "", EmptyIsInvalid).deserialize("123")) shouldBe Success("123")
      Try(ParameterSpec.string(paramName, "", EmptyIsValid).deserialize("")) shouldBe Success("")
    }

    it("does not retrieve an invalid value") {
      Try(ParameterSpec.string(paramName, "", EmptyIsValid).deserialize(null)).isFailure shouldBe true
      Try(ParameterSpec.string(paramName, "", EmptyIsInvalid).deserialize("")).isFailure shouldBe true
    }

    it("serializes correctly") {
      ParameterSpec.string(paramName, "").serialize("asdas") shouldBe "asdas"
    }
  }

  describe("uuid") {
    it("retrieves a valid value") {
      Try(ParameterSpec.uuid(paramName, "").deserialize("41fe035f-a948-4d67-a276-1ff79a0a7443")) shouldBe Success(UUID.fromString("41fe035f-a948-4d67-a276-1ff79a0a7443"))
    }

    it("does not retrieve an null value") {
      Try(ParameterSpec.uuid(paramName, "").deserialize(null)).isFailure shouldBe true
    }

    it("serializes correctly") {
      ParameterSpec.uuid(paramName, "").serialize(UUID.fromString("41fe035f-a948-4d67-a276-1ff79a0a7443")) shouldBe "41fe035f-a948-4d67-a276-1ff79a0a7443"
    }
  }

  describe("dateTime") {
    it("retrieves a valid value") {
      Try(ParameterSpec.dateTime(paramName, "").deserialize("1970-01-01T00:00:00")) shouldBe Success(LocalDateTime.of(1970, 1, 1, 0, 0, 0))
    }

    it("does not retrieve an invalid value") {
      Try(ParameterSpec.dateTime(paramName, "").deserialize("notADateTime")).isFailure shouldBe true
    }

    it("does not retrieve an null value") {
      Try(ParameterSpec.dateTime(paramName, "").deserialize(null)).isFailure shouldBe true
    }

    it("serializes correctly") {
      ParameterSpec.dateTime(paramName, "").serialize(LocalDateTime.of(1970, 1, 1, 2, 3, 4)) shouldBe "1970-01-01T02:03:04"
    }
  }

  describe("zonedDateTime") {
    it("retrieves a valid value") {
      Try(ParameterSpec.zonedDateTime(paramName, "").deserialize("1970-01-01T00:00:00-01:00")).get.toEpochSecond shouldBe 3600
    }

    it("does not retrieve an invalid value") {
      Try(ParameterSpec.zonedDateTime(paramName, "").deserialize("notADateTime")).isFailure shouldBe true
    }

    it("does not retrieve an null value") {
      Try(ParameterSpec.zonedDateTime(paramName, "").deserialize(null)).isFailure shouldBe true
    }

    it("serializes correctly") {
      ParameterSpec.zonedDateTime(paramName, "").serialize(ZonedDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC"))) shouldBe "1970-01-01T00:00:00Z[UTC]"
    }
  }

  describe("date") {
    it("retrieves a valid value") {
      Try(ParameterSpec.localDate(paramName, "").deserialize("1970-01-01")) shouldBe Success(LocalDate.of(1970, 1, 1))
    }

    it("does not retrieve an invalid value") {
      Try(ParameterSpec.localDate(paramName, "").deserialize("notADateTime")).isFailure shouldBe true
    }

    it("does not retrieve an null value") {
      Try(ParameterSpec.localDate(paramName, "").deserialize(null)).isFailure shouldBe true
    }

    it("serializes correctly") {
      ParameterSpec.localDate(paramName, "").serialize(LocalDate.of(1970, 1, 1)) shouldBe "1970-01-01"
    }
  }

  describe("xml") {
    val expected = <field>value</field>

    it("retrieves a valid value") {
      Try(ParameterSpec.xml(paramName, "").deserialize(expected.toString())) shouldBe Success(expected)
    }

    it("does not retrieve an invalid value") {
      Try(ParameterSpec.xml(paramName, "").deserialize("notXml")).isFailure shouldBe true
    }

    it("does not retrieve an null value") {
      Try(ParameterSpec.xml(paramName, "").deserialize(null)).isFailure shouldBe true
    }

    it("serializes correctly") {
      ParameterSpec.xml(paramName, "").serialize(expected) shouldBe """<field>value</field>"""
    }
  }

  describe("custom") {

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
      ParameterSpec.int(paramName, "").map(IClass).deserialize("123") shouldBe IClass(123)
    }

    it("can map with read and show") {
      ParameterSpec.int(paramName, "").map[IClass](IClass, (i:IClass) => i.value + i.value).serialize(IClass(100)) shouldBe "200"
    }
  }
}
