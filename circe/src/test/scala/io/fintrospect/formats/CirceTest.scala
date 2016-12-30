package io.fintrospect.formats

import com.twitter.finagle.http.{Request, Status}
import io.circe.generic.auto._
import io.fintrospect.formats.Circe.JsonFormat._
import io.fintrospect.formats.Circe._
import io.fintrospect.formats.JsonFormat.InvalidJsonForDecoding
import io.fintrospect.parameters.{Body, BodySpec, Query}

import scala.language.reflectiveCalls


case class CirceStreetAddress(address: String)

case class CirceLetter(to: CirceStreetAddress, from: CirceStreetAddress, message: String)

case class CirceLetterOpt(to: CirceStreetAddress, from: CirceStreetAddress, message: Option[String])

class CirceJsonResponseBuilderTest extends JsonResponseBuilderSpec(Circe)


class CirceFiltersTest extends AutoFiltersSpec(Circe.NuFilters) {

  override def toString(l: Letter): String = encode(l).noSpaces
  override def fromString(s: String): Letter = decode[Letter](parse(s))
  override def bodySpec: BodySpec[Letter] = Circe.bodySpec[Letter]()
  override def toOut() = Circe.NuFilters.tToToOut[Letter]
}

class CirceJsonFormatTest extends JsonFormatSpec(Circe) {

  import io.circe.generic.auto._

  describe("Circe.JsonFormat") {
    val aLetter = CirceLetter(CirceStreetAddress("my house"), CirceStreetAddress("your house"), "hi there")

    it("roundtrips to JSON and back") {
      val encoded = encode(aLetter)
      decode[CirceLetter](encoded) shouldBe aLetter
    }

    it("patchbody modifies original object with a non-null value") {
      val original = CirceLetterOpt(CirceStreetAddress("my house"), CirceStreetAddress("your house"), None)
      val modifier = encode(obj("message" -> string("hi there")))
      val modifyLetter = patcher[CirceLetterOpt](modifier)
      modifyLetter(original) shouldBe CirceLetterOpt(CirceStreetAddress("my house"), CirceStreetAddress("your house"), Option("hi there"))
    }

    // wait for circe 0.6.X, where this bug will be fixed - https://github.com/travisbrown/circe/issues/304
    ignore("patcher modifies original object with a null value") {
      val original = CirceLetterOpt(CirceStreetAddress("my house"), CirceStreetAddress("your house"), Option("hi there"))
      val modifier = encode(obj())
      val modifyLetter = patcher[CirceLetterOpt](modifier)
      modifyLetter(original) shouldBe CirceLetterOpt(CirceStreetAddress("my house"), CirceStreetAddress("your house"), None)
    }

    it("invalid extracted JSON throws up") {
      intercept[InvalidJsonForDecoding](decode[CirceLetter](Circe.JsonFormat.obj()))
    }

    it("body spec decodes content") {
      (Body(bodySpec[CirceLetter]()) <-- Circe.ResponseBuilder.Ok(encode(aLetter)).build()) shouldBe aLetter
    }

    it("patch body can be used to modify an existing case class object") {
      val letterWithNoMessage = CirceLetterOpt(CirceStreetAddress("my house"), CirceStreetAddress("your house"), None)
      val modifiedMessage = encode(obj("message" -> string("hi there")))
      val modifiedLetterWithMessage = CirceLetterOpt(CirceStreetAddress("my house"), CirceStreetAddress("your house"), Some("hi there"))

      val patch = patchBody[CirceLetterOpt](None, modifiedLetterWithMessage) <-- Circe.ResponseBuilder.Ok(modifiedMessage).build()

      patch(letterWithNoMessage) shouldBe modifiedLetterWithMessage
    }

    it("response spec has correct code") {
      Circe.responseSpec[CirceLetter](Status.Ok -> "ok", aLetter).status shouldBe Status.Ok
    }

    it("param spec decodes content") {
      val param = Query.required(parameterSpec[CirceLetter]("name"))
      (param <-- Request("?name=" + encode(aLetter))) shouldBe aLetter
    }
  }
  override val expectedJson: String = """{"string":"hello","object":{"field1":"aString"},"int":1.0,"long":2,"decimal":1.2,"bigInt":12344,"bool":true,"null":null,"array":["world",true]}"""
}
