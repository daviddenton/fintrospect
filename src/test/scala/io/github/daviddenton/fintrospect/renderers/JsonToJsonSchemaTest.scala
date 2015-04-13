package io.github.daviddenton.fintrospect.renderers

import io.github.daviddenton.fintrospect.util.ArgoUtil._
import org.scalatest.{FunSpec, ShouldMatchers}

import scala.io.Source.fromInputStream

class JsonToJsonSchemaTest extends FunSpec with ShouldMatchers {

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
      actual.node shouldEqual parse(fromInputStream(getClass.getResourceAsStream(s"JsonToJsonSchema_main.json")).mkString)
//      println(pretty(obj(actual.definitions: _*)))
      obj(actual.definitions: _*) shouldEqual parse(fromInputStream(getClass.getResourceAsStream(s"JsonToJsonSchema_definitions.json")).mkString)
    }
  }
}
