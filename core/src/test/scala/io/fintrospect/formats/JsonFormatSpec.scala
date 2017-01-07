package io.fintrospect.formats

import java.math.BigInteger

import io.fintrospect.parameters.ParameterSpec
import org.scalatest.{FunSpec, Matchers}

import scala.util.{Success, Try}

abstract class JsonFormatSpec[X <: Y, Y](val jsonLib: JsonLibrary[X, Y]) extends FunSpec with Matchers {

  val format = jsonLib.JsonFormat

  val expectedJson = """{"string":"hello","object":{"field1":"aString"},"int":10,"long":2,"double":1.2,"decimal":1.2,"bigInt":12344,"bool":true,"null":null,"array":["world",true]}"""

  val expected = format.obj("field" -> format.string("value"))

  describe(format.getClass.getSimpleName) {
    it("creates JSON objects as expected") {
      format.compact(format.objSym(
        'string -> format.string("hello"),
        'object -> format.obj("field1" -> format.string("aString")),
        'int -> format.number(10),
        'long -> format.number(2L),
        'double -> format.number(1.2),
        'decimal -> format.number(BigDecimal(1.2)),
        'bigInt -> format.number(new BigInteger("12344")),
        'bool -> format.boolean(true),
        'null -> format.nullNode(),
        'array -> format.array(format.string("world"), format.boolean(true))
      )) shouldEqual expectedJson
    }

    describe("Parse blows up when invalid") {
      it("parse blows up when invalid") {
        Try(format.parse("<12312>")).isFailure shouldBe true
      }
    }

    val paramName = "name"

    describe("ParameterSpec json") {
      describe(getClass.getName + " Json") {
        it("retrieves a valid value") {
          Try(ParameterSpec.json(paramName, "", jsonLib).deserialize(format.compact(expected))) shouldBe Success(expected)
        }

        it("does not retrieve an invalid value") {
          Try(ParameterSpec.json(paramName, "", jsonLib).deserialize("notJson")).isFailure shouldBe true
        }

        it("does not retrieve an null value") {
          Try(ParameterSpec.json(paramName, "", jsonLib).deserialize(null)).isFailure shouldBe true
        }

        it("serializes correctly") {
          ParameterSpec.json(paramName, "", jsonLib).serialize(expected) shouldBe """{"field":"value"}"""
        }
      }
    }
  }
}
