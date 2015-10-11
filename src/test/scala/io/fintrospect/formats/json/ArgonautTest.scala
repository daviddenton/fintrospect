package io.fintrospect.formats.json

import java.math.BigInteger

import org.scalatest.{FunSpec, ShouldMatchers}

class ArgonautJsonResponseBuilderTest extends JsonResponseBuilderSpec(Argonaut)

class ArgonautJsonFormatTest extends FunSpec with ShouldMatchers {
  private val format = Argonaut.JsonFormat

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
      )) shouldEqual """{"string":"hello","null":null,"bigInt":12344,"object":{"field1":"aString"},"decimal":1.2,"array":["world",true],"long":2,"bool":true,"int":1}"""
    }
  }
}
