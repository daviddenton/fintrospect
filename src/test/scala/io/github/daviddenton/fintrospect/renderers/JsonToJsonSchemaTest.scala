package io.github.daviddenton.fintrospect.renderers

import argo.jdom.JsonNodeFactories._
import io.github.daviddenton.fintrospect.util.ArgoUtil._
import org.scalatest.{FunSpec, ShouldMatchers}

import scala.io.Source.fromInputStream

class JsonToJsonSchemaTest extends FunSpec with ShouldMatchers {
  describe("JsonToJsonSchema") {
    it("renders all different types of json value as expected") {
      val model = obj(
        "aString" -> string("aStringValue"),
        "aNumber" -> number(new java.math.BigDecimal(1.9)),
        "aBooleanTrue" -> booleanNode(true),
        "aBooleanFalse" -> booleanNode(false),
        "array" -> array(string("anotherString"))
      )

      val actual = JsonToJsonSchema.toSchema(model)
      println(pretty(actual))
      actual should be === parse(fromInputStream(getClass.getResourceAsStream(s"JsonToJsonSchema.json")).mkString)
    }
  }
}
