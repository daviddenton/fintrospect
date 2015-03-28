package io.github.daviddenton.fintrospect.renderers

import argo.jdom.JsonNodeFactories
import io.github.daviddenton.fintrospect.util.ArgoUtil._
import org.scalatest.{FunSpec, ShouldMatchers}

import scala.io.Source.fromInputStream

class JsonToJsonSchemaTest extends FunSpec with ShouldMatchers {
  var id = 0

  def idGen(): String = {
    val nextId = "definition" + id
    id = id + 1
    nextId
  }

  describe("JsonToJsonSchema") {
    it("renders all different types of json value as expected") {
      val model = obj(
        "aString" -> string("aStringValue"),
        "aNumber" -> number(new java.math.BigDecimal(1.9)),
        "aBooleanTrue" -> boolean(true),
        "aBooleanFalse" -> boolean(false),
        "anArray" -> array(obj("anotherString" -> string("yetAnotherString"))),
        "anObject" -> obj("anotherNumber" -> number(1))
      )

      val actual = new JsonToJsonSchema(model, idGen).toSchemaAndDefinitions()
      actual._1 should be === parse(fromInputStream(getClass.getResourceAsStream(s"JsonToJsonSchema_main.json")).mkString)
      JsonNodeFactories.`object`(actual._2: _*) should be === parse(fromInputStream(getClass.getResourceAsStream(s"JsonToJsonSchema_definitions.json")).mkString)
    }
  }
}
