package io.fintrospect.formats.json

import java.math.BigInteger

import org.scalatest.{FunSpec, ShouldMatchers}

class JsonFormatSpec[X](format: JsonFormat[X, X]) extends FunSpec with ShouldMatchers {

  val expectedJson = """{"string":"hello","object":{"field1":"aString"},"int":1,"long":2,"decimal":1.2,"bigInt":12344,"bool":true,"null":null,"array":["world",true]}"""

  describe(format.getClass.getSimpleName) {
    it("creates JSON objects as expected") {
      format.compact(format.obj(
        "string" -> format.string("hello"),
        "object" -> format.obj(Seq("field1" -> format.string("aString"))),
        "int" -> format.number(1),
        "long" -> format.number(2L),
        "decimal" -> format.number(BigDecimal(1.2)),
        "bigInt" -> format.number(new BigInteger("12344")),
        "bool" -> format.boolean(true),
        "null" -> format.nullNode(),
        "array" -> format.array(format.string("world"), format.boolean(true))
      )) shouldEqual expectedJson
    }
  }

}
