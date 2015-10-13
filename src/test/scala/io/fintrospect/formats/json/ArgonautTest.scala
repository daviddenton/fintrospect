package io.fintrospect.formats.json

import argonaut.Argonaut._
import argonaut._
import io.fintrospect.formats.json.JsonFormat.InvalidJsonForDecoding

case class ArgonautStreetAddress(address: String)

object ArgonautStreetAddress {
  implicit def Codec: CodecJson[ArgonautStreetAddress]= casecodec1(ArgonautStreetAddress.apply, ArgonautStreetAddress.unapply)("address")
}

case class ArgonautLetter(to: ArgonautStreetAddress, from: ArgonautStreetAddress, message: String)

object ArgonautLetter {
  implicit def Codec: CodecJson[ArgonautLetter]= casecodec3(ArgonautLetter.apply, ArgonautLetter.unapply)("to", "from", "message")
}

class ArgonautJsonResponseBuilderTest extends JsonResponseBuilderSpec(Argonaut)

class ArgonautJsonFormatTest extends JsonFormatSpec(Argonaut.JsonFormat) {

  describe("Argonaut.JsonFormat") {
    val aLetter = ArgonautLetter(ArgonautStreetAddress("my house"), ArgonautStreetAddress("your house"), "hi there")
    it("roundtrips to JSON and back") {
      val encoded = Argonaut.JsonFormat.encode(aLetter)(ArgonautLetter.Codec)
      Argonaut.JsonFormat.decode[ArgonautLetter](encoded)(ArgonautLetter.Codec) shouldEqual aLetter
    }

    it("invalid extracted JSON throws up") {
      intercept[InvalidJsonForDecoding](Argonaut.JsonFormat.decode[ArgonautLetter](Argonaut.JsonFormat.obj()))
    }
  }

  override val expectedJson: String = """{"string":"hello","null":null,"bigInt":12344,"object":{"field1":"aString"},"decimal":1.2,"array":["world",true],"long":2,"bool":true,"int":1}"""
}
