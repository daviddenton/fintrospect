package io.fintrospect.formats.json

import argonaut.Argonaut._
import argonaut._
import com.twitter.finagle.http.Request
import io.fintrospect.formats.json.Argonaut.JsonFormat._
import io.fintrospect.formats.json.JsonFormat.InvalidJsonForDecoding
import io.fintrospect.parameters.{Query, Body}
import scala.language.reflectiveCalls

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
      val encoded = encode(aLetter)(ArgonautLetter.Codec)
      decode[ArgonautLetter](encoded)(ArgonautLetter.Codec) shouldEqual aLetter
    }

    it("invalid extracted JSON throws up") {
      intercept[InvalidJsonForDecoding](decode[ArgonautLetter](obj()))
    }

    it("body spec decodes content") {
      (Body(bodySpec[ArgonautLetter]()) <-- Argonaut.ResponseBuilder.OK(encode(aLetter)).build()) shouldBe aLetter
    }

    it("param spec decodes content") {
      val param = Query.required(parameterSpec[ArgonautLetter]("name"))
      (param <-- Request("?name=" + encode(aLetter))) shouldBe aLetter
    }
  }

  override val expectedJson: String = """{"string":"hello","null":null,"bigInt":12344,"object":{"field1":"aString"},"decimal":1.2,"array":["world",true],"long":2,"bool":true,"int":1}"""
}
