package io.fintrospect.parameters

import java.time.{LocalDate, LocalDateTime, ZoneId, ZonedDateTime}

import io.fintrospect.util.json.Argo.JsonFormat._
import io.fintrospect.util.json.{Argo, Json4s, JsonFormat, SprayJson}
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
  describeJson("spray", JsObject("field" -> JsString("value")), SprayJson.JsonFormat)

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

  case class MyCustomType(value: Int)

  describe("custom") {

    val spec = ParameterSpec[MyCustomType](paramName, None, StringParamType, s => MyCustomType(s.toInt), ct => ct.value.toString)

    it("retrieves a valid value") {
      Try(spec.deserialize("123")) shouldEqual Success(MyCustomType(123))
    }

    it("does not retrieve an invalid value") {
      Try(spec.deserialize("notAnInt")).isFailure shouldEqual true
    }

    it("does not retrieve an null value") {
      Try(spec.deserialize(null)).isFailure shouldEqual true
    }

    it("serializes correctly") {
      spec.serialize(MyCustomType(123)) shouldEqual "123"
    }
  }
}
