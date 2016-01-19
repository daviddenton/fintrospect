package io.fintrospect.formats.json

import io.fintrospect.formats.json.JsonFormat.InvalidJsonForDecoding

case class CirceStreetAddress(address: String)

case class CirceLetter(to: CirceStreetAddress, from: CirceStreetAddress, message: String)

class CirceJsonResponseBuilderTest extends JsonResponseBuilderSpec(Circe)

class CirceJsonFormatTest extends JsonFormatSpec(Circe.JsonFormat) {

  describe("Circe.JsonFormat") {
    val aLetter = CirceLetter(CirceStreetAddress("my house"), CirceStreetAddress("your house"), "hi there")

    import io.circe._
    import io.circe.generic.auto._
    import io.circe.parse._
    import io.circe.syntax._

    it("roundtrips to JSON and back") {
      val encoded = Circe.JsonFormat.encode(aLetter)
      Circe.JsonFormat.decode[CirceLetter](encoded) shouldEqual aLetter
    }

    it("invalid extracted JSON throws up") {
      intercept[InvalidJsonForDecoding](Circe.JsonFormat.decode[CirceLetter](Circe.JsonFormat.obj()))
    }
  }
  override val expectedJson: String = """{"string":"hello","object":{"field1":"aString"},"int":1.0,"long":2,"decimal":1.2,"bigInt":12344,"bool":true,"null":null,"array":["world",true]}"""
}
