package io.fintrospect.formats.json

import java.math.BigInteger

import io.fintrospect.formats.json.Json4s.Json4sFormat
import org.scalatest.{FunSpec, ShouldMatchers}

class JsonFormatSpec[T](format: Json4sFormat[T]) extends FunSpec with ShouldMatchers {

  describe(format.getClass.getSimpleName) {
    it("creates JSON objects as expected") {
      format.compact(format.obj(
        "string" -> format.string("hello"),
        "int" -> format.number(1),
        "long" -> format.number(2L),
        "decimal" -> format.number(BigDecimal(1.2)),
        "bigInt" -> format.number(new BigInteger("12344")),
        "bool" -> format.boolean(true),
        "null" -> format.nullNode(),
        "array" -> format.array(format.string("world"), format.boolean(true))
      )) shouldEqual """[{"string":"hello"},{"int":1},{"long":2},{"decimal":1.2},{"bigInt":12344},{"bool":true},{"null":null},{"array":["world",true]}]"""
    }
  }

}
