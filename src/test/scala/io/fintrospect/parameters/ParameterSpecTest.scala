package io.fintrospect.parameters

import java.time.{LocalDate, LocalDateTime, ZoneId, ZonedDateTime}
import java.util.UUID

import io.fintrospect.formats.json.Argo.JsonFormat.{obj, string}
import io.fintrospect.formats.json.{Argo, Json4s, JsonFormat, Spray}
import org.json4s.JsonAST.{JObject, JString}
import org.scalatest._
import spray.json.{JsObject, JsString}

import scala.util.{Success, Try}

class ParameterSpecTest extends FunSpec with ShouldMatchers {

  val paramName = "name"


  describe("int") {
    it("retrieves a valid value") {
      Try(ParameterSpec.int(paramName, "").deserialize("123")) shouldEqual Success(123)
    }

    it("does not retrieve an invalid value") {
      Try(ParameterSpec.int(paramName, "").deserialize("asd")).isFailure shouldEqual true
    }

    it("does not retrieve an null value") {
      Try(ParameterSpec.int(paramName, "").deserialize(null)).isFailure shouldEqual true
    }

    it("serializes correctly") {
      ParameterSpec.int(paramName, "").serialize(123) shouldEqual "123"
    }
  }

  describe("integer") {
    it("retrieves a valid value") {
      Try(ParameterSpec.integer(paramName, "").deserialize("123")) shouldEqual Success(123)
    }

    it("does not retrieve an invalid value") {
      Try(ParameterSpec.integer(paramName, "").deserialize("asd")).isFailure shouldEqual true
    }

    it("does not retrieve an null value") {
      Try(ParameterSpec.integer(paramName, "").deserialize(null)).isFailure shouldEqual true
    }

    it("serializes correctly") {
      ParameterSpec.integer(paramName, "").serialize(123) shouldEqual "123"
    }
  }

  describe("long") {
    it("retrieves a valid value") {
      Try(ParameterSpec.long(paramName, "").deserialize("123")) shouldEqual Success(123)
    }

    it("does not retrieve an invalid value") {
      Try(ParameterSpec.long(paramName, "").deserialize("asd")).isFailure shouldEqual true
    }

    it("does not retrieve an null value") {
      Try(ParameterSpec.long(paramName, "").deserialize(null)).isFailure shouldEqual true
    }

    it("serializes correctly") {
      ParameterSpec.long(paramName, "").serialize(123) shouldEqual "123"
    }
  }

  describe("bigDecimal") {
    it("retrieves a valid value") {
      Try(ParameterSpec.bigDecimal(paramName, "").deserialize("1.1234")) shouldEqual Success(BigDecimal("1.1234"))
    }

    it("does not retrieve an invalid value") {
      Try(ParameterSpec.bigDecimal(paramName, "").deserialize("asd")).isFailure shouldEqual true
    }

    it("does not retrieve an null value") {
      Try(ParameterSpec.bigDecimal(paramName, "").deserialize(null)).isFailure shouldEqual true
    }

    it("serializes correctly") {
      ParameterSpec.bigDecimal(paramName, "").serialize(BigDecimal("1.1234")) shouldEqual "1.1234"
    }
  }

  describe("boolean") {
    it("retrieves a valid value") {
      Try(ParameterSpec.boolean(paramName, "").deserialize("true")) shouldEqual Success(true)
    }

    it("does not retrieve an invalid value") {
      Try(ParameterSpec.boolean(paramName, "").deserialize("notABool")).isFailure shouldEqual true
    }

    it("does not retrieve an null value") {
      Try(ParameterSpec.boolean(paramName, "").deserialize(null)).isFailure shouldEqual true
    }

    it("serializes correctly") {
      ParameterSpec.boolean(paramName, "").serialize(true) shouldEqual "true"
    }
  }

  describe("string") {
    it("retrieves a valid value") {
      Try(ParameterSpec.string(paramName, "").deserialize("123")) shouldEqual Success("123")
    }

    it("does not retrieve an null value") {
      Try(ParameterSpec.string(paramName, "").deserialize(null)).isFailure shouldEqual true
    }

    it("serializes correctly") {
      ParameterSpec.string(paramName, "").serialize("asdas") shouldEqual "asdas"
    }
  }

  describe("uuid") {
    println(UUID.randomUUID().toString)
    it("retrieves a valid value") {
      Try(ParameterSpec.uuid(paramName, "").deserialize("41fe035f-a948-4d67-a276-1ff79a0a7443")) shouldEqual Success(UUID.fromString("41fe035f-a948-4d67-a276-1ff79a0a7443"))
    }

    it("does not retrieve an null value") {
      Try(ParameterSpec.uuid(paramName, "").deserialize(null)).isFailure shouldEqual true
    }

    it("serializes correctly") {
      ParameterSpec.uuid(paramName, "").serialize(UUID.fromString("41fe035f-a948-4d67-a276-1ff79a0a7443")) shouldEqual "41fe035f-a948-4d67-a276-1ff79a0a7443"
    }
  }

  describe("dateTime") {
    it("retrieves a valid value") {
      Try(ParameterSpec.dateTime(paramName, "").deserialize("1970-01-01T00:00:00")) shouldEqual Success(LocalDateTime.of(1970, 1, 1, 0, 0, 0))
    }

    it("does not retrieve an invalid value") {
      Try(ParameterSpec.dateTime(paramName, "").deserialize("notADateTime")).isFailure shouldEqual true
    }

    it("does not retrieve an null value") {
      Try(ParameterSpec.dateTime(paramName, "").deserialize(null)).isFailure shouldEqual true
    }

    it("serializes correctly") {
      ParameterSpec.dateTime(paramName, "").serialize(LocalDateTime.of(1970, 1, 1, 2, 3, 4)) shouldEqual "1970-01-01T02:03:04"
    }
  }

  describe("zonedDateTime") {
    it("retrieves a valid value") {
      Try(ParameterSpec.zonedDateTime(paramName, "").deserialize("1970-01-01T00:00:00-01:00")).get.toEpochSecond shouldEqual 3600
    }

    it("does not retrieve an invalid value") {
      Try(ParameterSpec.zonedDateTime(paramName, "").deserialize("notADateTime")).isFailure shouldEqual true
    }

    it("does not retrieve an null value") {
      Try(ParameterSpec.zonedDateTime(paramName, "").deserialize(null)).isFailure shouldEqual true
    }

    it("serializes correctly") {
      ParameterSpec.zonedDateTime(paramName, "").serialize(ZonedDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC"))) shouldEqual "1970-01-01T00:00:00Z[UTC]"
    }
  }

  describe("date") {
    it("retrieves a valid value") {
      Try(ParameterSpec.localDate(paramName, "").deserialize("1970-01-01")) shouldEqual Success(LocalDate.of(1970, 1, 1))
    }

    it("does not retrieve an invalid value") {
      Try(ParameterSpec.localDate(paramName, "").deserialize("notADateTime")).isFailure shouldEqual true
    }

    it("does not retrieve an null value") {
      Try(ParameterSpec.localDate(paramName, "").deserialize(null)).isFailure shouldEqual true
    }

    it("serializes correctly") {
      ParameterSpec.localDate(paramName, "").serialize(LocalDate.of(1970, 1, 1)) shouldEqual "1970-01-01"
    }
  }

  private def describeJson[T](name: String, expected: T, jsonFormat: JsonFormat[T, _]): Unit = {
    describe(name + " Json") {
      it("retrieves a valid value") {
        Try(ParameterSpec.json(paramName, "", jsonFormat).deserialize(jsonFormat.compact(expected))) shouldEqual Success(expected)
      }

      it("does not retrieve an invalid value") {
        Try(ParameterSpec.json(paramName, "", jsonFormat).deserialize("notJson")).isFailure shouldEqual true
      }

      it("does not retrieve an null value") {
        Try(ParameterSpec.json(paramName, "", jsonFormat).deserialize(null)).isFailure shouldEqual true
      }

      it("serializes correctly") {
        ParameterSpec.json(paramName, "", jsonFormat).serialize(expected) shouldEqual """{"field":"value"}"""
      }
    }
  }

  describeJson("argo", obj("field" -> string("value")), Argo.JsonFormat)
  describeJson("json4s native", JObject("field" -> JString("value")), Json4s.Native.JsonFormat)
  describeJson("json4s jackson", JObject("field" -> JString("value")), Json4s.Jackson.JsonFormat)
  describeJson("json4s native - double", JObject("field" -> JString("value")), Json4s.NativeDoubleMode.JsonFormat)
  describeJson("json4s jackson - double", JObject("field" -> JString("value")), Json4s.JacksonDoubleMode.JsonFormat)
  describeJson("spray", JsObject("field" -> JsString("value")), Spray.JsonFormat)

  describe("xml") {
    val expected = <field>value</field>

    it("retrieves a valid value") {
      Try(ParameterSpec.xml(paramName, "").deserialize(expected.toString())) shouldEqual Success(expected)
    }

    it("does not retrieve an invalid value") {
      Try(ParameterSpec.xml(paramName, "").deserialize("notXml")).isFailure shouldEqual true
    }

    it("does not retrieve an null value") {
      Try(ParameterSpec.xml(paramName, "").deserialize(null)).isFailure shouldEqual true
    }

    it("serializes correctly") {
      ParameterSpec.xml(paramName, "").serialize(expected) shouldEqual """<field>value</field>"""
    }
  }

  describe("custom") {

    it("retrieves a valid value") {
      Try(MyCustomType.spec.deserialize("123")) shouldEqual Success(MyCustomType(123))
    }

    it("does not retrieve an invalid value") {
      Try(MyCustomType.spec.deserialize("notAnInt")).isFailure shouldEqual true
    }

    it("does not retrieve an null value") {
      Try(MyCustomType.spec.deserialize(null)).isFailure shouldEqual true
    }

    it("serializes correctly") {
      MyCustomType.spec.serialize(MyCustomType(123)) shouldEqual "123"
    }
  }

  describe("Generating ParameterSpec instances from AnyVals marked up with ParameterSpecVal for") {
    it("int") {
      case class IClass(value: Int) extends ParameterSpecVal[Int]
      ParameterSpec.specVal.int(IClass, paramName).deserialize("123") shouldEqual IClass(123)
    }

    it("integer") {
      case class IClass(value: Integer) extends ParameterSpecVal[Integer]
      ParameterSpec.specVal.integer(IClass, paramName).deserialize("123") shouldEqual IClass(123)
    }

    it("long") {
      case class IClass(value: Long) extends ParameterSpecVal[Long]
      ParameterSpec.specVal.long(IClass, paramName).deserialize("123") shouldEqual IClass(123)
    }

    it("bigDecimal") {
      case class IClass(value: BigDecimal) extends ParameterSpecVal[BigDecimal]
      ParameterSpec.specVal.bigDecimal(IClass, paramName).deserialize("1.1234") shouldEqual IClass(BigDecimal("1.1234"))
    }

    it("boolean") {
      case class IClass(value: Boolean) extends ParameterSpecVal[Boolean]
      ParameterSpec.specVal.boolean(IClass, paramName).deserialize("true") shouldEqual IClass(true)
    }

    it("string") {
      case class IClass(value: String) extends ParameterSpecVal[String]
      ParameterSpec.specVal.string(IClass, paramName).deserialize("123") shouldEqual IClass("123")
    }

    it("uuid") {
      case class IClass(value: UUID) extends ParameterSpecVal[UUID]
      ParameterSpec.specVal.uuid(IClass, paramName).deserialize("41fe035f-a948-4d67-a276-1ff79a0a7443") shouldEqual IClass(UUID.fromString("41fe035f-a948-4d67-a276-1ff79a0a7443"))
    }
    it("dateTime") {
      case class IClass(value: LocalDateTime) extends ParameterSpecVal[LocalDateTime]
      ParameterSpec.specVal.dateTime(IClass, paramName).deserialize("1970-01-01T00:00:00") shouldEqual IClass(LocalDateTime.of(1970, 1, 1, 0, 0, 0))
    }
    it("zonedDateTime") {
      case class IClass(value: ZonedDateTime) extends ParameterSpecVal[ZonedDateTime]
      ParameterSpec.specVal.zonedDateTime[IClass](IClass, paramName).deserialize("1970-01-01T00:00:00-01:00").value.toEpochSecond shouldEqual 3600
    }
    it("localDate") {
      case class IClass(value: LocalDate) extends ParameterSpecVal[LocalDate]
      ParameterSpec.specVal.localDate(IClass, paramName).deserialize("1970-01-01") shouldEqual IClass(LocalDate.of(1970, 1, 1))
    }
  }
}
