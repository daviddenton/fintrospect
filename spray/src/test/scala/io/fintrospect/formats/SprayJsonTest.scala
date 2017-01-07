package io.fintrospect.formats

class SprayJsonResponseBuilderTest extends JsonResponseBuilderSpec(Spray)

class SprayJsonFormatTest extends JsonFormatSpec(Spray) {
  override val expectedJson = """{"string":"hello","null":null,"bigInt":12344,"object":{"field1":"aString"},"decimal":1.2,"double":1.2,"array":["world",true],"long":2,"bool":true,"int":1}"""
}