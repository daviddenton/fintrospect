package io.fintrospect.renderers.util

import io.fintrospect.formats.Argo.JsonFormat.{array, boolean, number, obj, parse, string}
import org.scalatest.{FunSpec, Matchers}

import scala.io.Source.fromInputStream

class JsonToJsonSchemaTest extends FunSpec with Matchers {

  describe("JsonToJsonSchema") {
    it("renders all different types of json value as expected") {
      val model = obj(
        "aString" -> string("aStringValue"),
        "aNumber" -> number(new java.math.BigDecimal(1.9)),
        "aBooleanTrue" -> boolean(true),
        "aBooleanFalse" -> boolean(false),
        "anArray" -> array(obj("anotherString" -> string("yetAnotherString"))),
        "anObject" -> obj("anInteger" -> number(1))
      )

      val actual = new JsonToJsonSchema().toSchema(model)
      actual.node shouldBe parse(fromInputStream(getClass.getResourceAsStream(s"JsonToJsonSchema_main.json")).mkString)
      obj(actual.definitions: _*) shouldBe parse(fromInputStream(getClass.getResourceAsStream(s"JsonToJsonSchema_definitions.json")).mkString)
    }
  }
}
