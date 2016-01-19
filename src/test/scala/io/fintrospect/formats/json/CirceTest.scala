package io.fintrospect.formats.json

case class CirceStreetAddress(address: String)

object CirceStreetAddress {
}

case class CirceLetter(to: CirceStreetAddress, from: CirceStreetAddress, message: String)

object CirceLetter {
}

class CirceJsonResponseBuilderTest extends JsonResponseBuilderSpec(Circe)

class CirceJsonFormatTest extends JsonFormatSpec(Circe.JsonFormat) {

  describe("Circe.JsonFormat") {
    //    val aLetter = CirceLetter(CirceStreetAddress("my house"), CirceStreetAddress("your house"), "hi there")
    //    it("roundtrips to JSON and back") {
    //      val encoded = Circe.JsonFormat.encode(aLetter)(CirceLetter.Codec)
    //      Circe.JsonFormat.decode[CirceLetter](encoded)(CirceLetter.Codec) shouldEqual aLetter
    //    }
    //
    //    it("invalid extracted JSON throws up") {
    //      intercept[InvalidJsonForDecoding](Circe.JsonFormat.decode[CirceLetter](Circe.JsonFormat.obj()))
    //    }
  }
  override val expectedJson: String = """{"string":"hello","object":{"field1":"aString"},"int":1.0,"long":2,"decimal":1.2,"bigInt":12344,"bool":true,"null":null,"array":["world",true]}"""
}
