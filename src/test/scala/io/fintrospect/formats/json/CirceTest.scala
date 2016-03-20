package io.fintrospect.formats.json

import com.twitter.finagle.http.Request
import io.fintrospect.formats.json.Circe.JsonFormat.{bodySpec, encode, parameterSpec}
import io.fintrospect.formats.json.JsonFormat.InvalidJsonForDecoding
import io.fintrospect.parameters.{Body, Query}

import scala.language.reflectiveCalls

case class CirceStreetAddress(address: String)

case class CirceLetter(to: CirceStreetAddress, from: CirceStreetAddress, message: String)

class CirceJsonResponseBuilderTest extends JsonResponseBuilderSpec(Circe)

class CirceJsonFormatTest extends JsonFormatSpec(Circe.JsonFormat) {

  import io.circe._
  import io.circe.generic.auto._
  import io.circe.parser._
  import io.circe.syntax._

  describe("Circe.JsonFormat") {
    val aLetter = CirceLetter(CirceStreetAddress("my house"), CirceStreetAddress("your house"), "hi there")

    it("roundtrips to JSON and back") {
      val encoded = Circe.JsonFormat.encode(aLetter)
      Circe.JsonFormat.decode[CirceLetter](encoded) shouldEqual aLetter
    }

    it("invalid extracted JSON throws up") {
      intercept[InvalidJsonForDecoding](Circe.JsonFormat.decode[CirceLetter](Circe.JsonFormat.obj()))
    }

    it("body spec decodes content") {
      (Body(bodySpec[CirceLetter]()) <-- Circe.ResponseBuilder.OK(encode(aLetter)).build()) shouldBe aLetter
    }

    it("param spec decodes content") {
      val param = Query.required(parameterSpec[CirceLetter]("name"))
      (param <-- Request("?name=" + encode(aLetter))) shouldBe aLetter
    }
  }
  override val expectedJson: String = """{"string":"hello","object":{"field1":"aString"},"int":1.0,"long":2,"decimal":1.2,"bigInt":12344,"bool":true,"null":null,"array":["world",true]}"""
}
