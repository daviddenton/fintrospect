package io.fintrospect.formats

class SprayJsonResponseBuilderTest extends JsonResponseBuilderSpec(Spray)

class SprayJsonFormatTest extends JsonFormatSpec(Spray) {
  override val expectedJson = """{"array":["world",true],"bigInt":12344,"bool":true,"decimal":1.2,"double":1.2,"int":10,"long":2,"null":null,"object":{"field1":"aString"},"string":"hello"}"""
}